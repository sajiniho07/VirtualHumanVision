package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;
import org.vhv.virtualhumanvision.configuration.MediaConfig;
import org.vhv.virtualhumanvision.dto.request.ParkingSlotDetail;
import org.vhv.virtualhumanvision.dto.request.ParkingTrainingRequest;
import org.vhv.virtualhumanvision.dto.request.PointInfo;
import org.vhv.virtualhumanvision.entity.Media;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final MediaConfig mediaConfig;
    private final MediaService mediaService;
    private final String TRAIN_FILE_NAME_PREFIX = "ParkingSlotTrain_";

    public File generateTrainArffFile(ParkingTrainingRequest request, String mediaOriginalPath) {
        Long mediaId = request.getMediaId();
        disablePrevArffFileIfExists(mediaId);
        Mat image = loadImage(mediaOriginalPath);
        List<Attribute> attributes = createAttributes();
        Instances data = initializeInstances(attributes);

        for (ParkingSlotDetail slotDetail : request.getSlotDetails()) {
            processSlotDetail(slotDetail, image, data);
        }

        return saveArffFile(mediaId, data);
    }

    private void disablePrevArffFileIfExists(Long mediaId) {
        Optional<Media> mediaByRefId = mediaService.getMediaByRefId(mediaId);
        mediaByRefId.ifPresent(media -> mediaService.disableMedia(media.getId()));
    }

    private Mat loadImage(String mediaOriginalPath) {
        Mat image = Imgcodecs.imread(mediaOriginalPath, Imgcodecs.IMREAD_GRAYSCALE);
        if (image.empty()) {
            throw new RuntimeException("Failed to load image.");
        }
        return image;
    }

    private List<Attribute> createAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("NW"));
        attributes.add(new Attribute("N"));
        attributes.add(new Attribute("NE"));
        attributes.add(new Attribute("W"));
        attributes.add(new Attribute("C"));
        attributes.add(new Attribute("E"));
        attributes.add(new Attribute("SW"));
        attributes.add(new Attribute("S"));
        attributes.add(new Attribute("SE"));
        attributes.add(new Attribute("isEmpty", Arrays.asList("true", "false")));
        return attributes;
    }

    private Instances initializeInstances(List<Attribute> attributes) {
        Instances data = new Instances("ParkingSlotTrain", new ArrayList<>(attributes), 0);
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    private void processSlotDetail(ParkingSlotDetail slotDetail, Mat image, Instances data) {
        List<Point> points = extractPoints(slotDetail);
        Mat croppedImage = cropImage(image, points);

        double[][] matrix = convertImageToMatrix(croppedImage);
        String[] principalComponents = performPCA(matrix);

        addInstanceToData(data, principalComponents, slotDetail);
    }

    private double[][] convertImageToMatrix(Mat croppedImage) {
        int rows = roundToNearestMultipleOfThree(croppedImage.rows());
        int cols = roundToNearestMultipleOfThree(croppedImage.cols());

        double[][] matrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = croppedImage.get(i, j)[0];
            }
        }

        return matrix;
    }

    private void addInstanceToData(Instances data, String[] principalComponents, ParkingSlotDetail slotDetail) {
        int numAttributes = data.numAttributes();
        Instance instance = new DenseInstance(numAttributes);
        instance.setDataset(data);

        for (int i = 0; i < principalComponents.length; i++) {
            if (i < numAttributes - 1) {
                instance.setValue(data.attribute(i), Double.parseDouble(principalComponents[i]));
            }
        }

        String isEmptyValue = Boolean.TRUE.equals(slotDetail.getIsEmpty()) ? "true" : "false";
        instance.setValue(data.attribute(numAttributes - 1), isEmptyValue);

        data.add(instance);
    }

    private File saveArffFile(Long mediaId, Instances data) {
        try {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            File outputFile = new File(mediaConfig.getTrainModelBaseDir() + File.separator +
                    TRAIN_FILE_NAME_PREFIX + mediaId + ".arff");
            saver.setFile(outputFile);
            saver.writeBatch();
            return outputFile;

        } catch (Exception e) {
            log.error("Error saving ARFF file: {}", e.getMessage());
            return null;
        }
    }

    private List<Point> extractPoints(ParkingSlotDetail slotDetail) {
        List<Point> points = new ArrayList<>();
        for (PointInfo point : slotDetail.getPoints()) {
            points.add(new Point(point.getRealX().intValue(), point.getRealY().intValue()));
        }
        return points;
    }

    private String[] performPCA(double[][] mainMatrix) {
        int numSubMatrices = 3 * 3;
        String[] principalComponents = new String[numSubMatrices];

        int rows = mainMatrix.length;
        int cols = mainMatrix[0].length;

        int subRows = rows / 3;
        int subCols = cols / 3;

        int index = -1;
        for (int mainRow = 0; mainRow < rows; mainRow += subRows) {
            for (int mainCol = 0; mainCol < cols; mainCol += subCols) {
                index++;
                double[][] subMatrixData = new double[subRows][subCols];
                for (int r = 0; r < subRows && mainRow + r < rows; r++) {
                    for (int c = 0; c < subCols && mainCol + c < cols; c++) {
                        subMatrixData[r][c] = mainMatrix[mainRow + r][mainCol + c];
                    }
                }
                if (index >= numSubMatrices) {
                    log.error("wrong index : " + index);
                } else {
                    principalComponents[index] = extractPrincipalComponent(subMatrixData);
                }
            }
        }
        return principalComponents;
    }

    private String extractPrincipalComponent(double[][] data) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);

        centralizeMatrix(matrix);

        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        RealMatrix uMatrix = svd.getU();

        double meanValue = calculateMean(uMatrix);
        return formatMean(meanValue);
    }

    private String formatMean(double mean) {
        DecimalFormat df = new DecimalFormat("#.#######");
        return df.format(mean);
    }

    private void centralizeMatrix(RealMatrix matrix) {
        int columnDimension = matrix.getColumnDimension();
        int rowDimension = matrix.getRowDimension();

        double[] columnMeans = new double[columnDimension];
        for (int i = 0; i < columnDimension; i++) {
            columnMeans[i] = matrix.getColumnVector(i).getL1Norm() / rowDimension;
        }

        for (int i = 0; i < rowDimension; i++) {
            for (int j = 0; j < columnDimension; j++) {
                matrix.setEntry(i, j, matrix.getEntry(i, j) - columnMeans[j]);
            }
        }
    }

    private double calculateMean(RealMatrix uMatrix) {
        double sum = 0.0;
        int uRows = uMatrix.getRowDimension();
        int uCols = uMatrix.getColumnDimension();

        for (int i = 0; i < uRows; i++) {
            for (int j = 0; j < uCols; j++) {
                sum += uMatrix.getEntry(i, j);
            }
        }

        return sum / (uRows * uCols);
    }

    private Mat cropImage(Mat image, List<Point> points) {
        int startX = (int) Math.min(Math.min(points.get(0).x, points.get(1).x), Math.min(points.get(2).x, points.get(3).x));
        int startY = (int) Math.min(Math.min(points.get(0).y, points.get(1).y), Math.min(points.get(2).y, points.get(3).y));
        int endX = (int) Math.max(Math.max(points.get(0).x, points.get(1).x), Math.max(points.get(2).x, points.get(3).x));
        int endY = (int) Math.max(Math.max(points.get(0).y, points.get(1).y), Math.max(points.get(2).y, points.get(3).y));

        Rect rectCrop = new Rect(startX, startY, endX - startX, endY - startY);
        return new Mat(image, rectCrop);
    }

    public static int roundToNearestMultipleOfThree(int num) {
        return (num / 3) * 3;
    }

}
