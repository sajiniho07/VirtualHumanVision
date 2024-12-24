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
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.dto.request.ParkingSlotDetail;
import org.vhv.virtualhumanvision.dto.request.ParkingTrainingRequest;
import org.vhv.virtualhumanvision.dto.request.PointInfo;
import org.vhv.virtualhumanvision.entity.CoordinatesInfo;
import org.vhv.virtualhumanvision.entity.Media;
import org.vhv.virtualhumanvision.entity.ParkingSlotInfo;
import org.vhv.virtualhumanvision.repository.MediaRepository;
import org.vhv.virtualhumanvision.repository.ParkingSlotInfoRepository;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingSlotService {

    private final MediaRepository mediaRepository;
    private final ParkingSlotInfoRepository parkingSlotInfoRepository;

    @Transactional
    public void saveAndTrainModel(ParkingTrainingRequest request) {
        Long mediaId = request.getMediaId();
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

//        saveParkingSlotInfos(request, mediaId, media); todo

        Mat imread = Imgcodecs.imread(media.getOriginalPath(), Imgcodecs.IMREAD_GRAYSCALE);
        if (imread.empty()) {
            throw new RuntimeException("Failed to load image.");
        }
        generateTrainArffFile(request, imread);
    }

    private void generateTrainArffFile(ParkingTrainingRequest request, Mat imread) {
        try {
            ArrayList<Attribute> attributes = new ArrayList<>();
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

            Instances data = new Instances("ParkingSlotTrain", attributes, 0);
            data.setClassIndex(data.numAttributes() - 1);

            List<ParkingSlotDetail> slotDetails = request.getSlotDetails();
            for (ParkingSlotDetail slotDetail : slotDetails) {
                List<Point> points = new ArrayList<>();
                for (PointInfo point : slotDetail.getPoints()) {
                    points.add(new Point(point.getRealX().intValue(), point.getRealY().intValue()));
                }
                Mat croppedImage = cropImage(imread, points);

                int rows = roundToNearestMultipleOfThree(croppedImage.rows());
                int cols = roundToNearestMultipleOfThree(croppedImage.cols());
                double[][] matrix = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        matrix[i][j] = croppedImage.get(i, j)[0];
                    }
                }

                String[] principalComponents = performPCA(matrix);

                Instance inst = new DenseInstance(attributes.size());
                inst.setDataset(data);

                for (int i = 0; i < principalComponents.length; i++) {
                    if (i < attributes.size() - 1) {
                        inst.setValue(attributes.get(i), Double.parseDouble(principalComponents[i]));
                    }
                }
                String value = Boolean.TRUE.equals(slotDetail.getIsEmpty()) ? "true" : "false";
                inst.setValue(attributes.get(attributes.size() - 1), value);
                data.add(inst);
            }

            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File("ParkingSlotTrain.arff"));
            saver.writeBatch();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String[] performPCA(double[][] mainMatrix) {
        int numSubMatrices = (3 * 3);

        String[] principalComponents = new String[numSubMatrices];

        int rows = mainMatrix.length;
        int cols = mainMatrix[1].length;

        int subRows = rows / 3;
        int subCols = cols / 3;

        int index = -1;
        for (int i = 0; i < rows; i += subRows) {
            for (int j = 0; j < cols; j += subCols) {
                index++;
                double[][] subMatrixData = new double[subRows][subCols];
                for (int r = 0; r < subRows && i + r < rows; r++) {
                    for (int c = 0; c < subCols && j + c < cols; c++) {
                        subMatrixData[r][c] = mainMatrix[i + r][j + c];
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

        int columnDimension = matrix.getColumnDimension();
        int rowDimension = matrix.getRowDimension();

        double[] columnMeans = new double[columnDimension];
        for (int i = 0; i < columnDimension; i++) {
            columnMeans[i] = matrix.getColumnVector(i).getL1Norm() / rowDimension;
        }

        // centralizing matrix each datas
        for (int i = 0; i < rowDimension; i++) {
            for (int j = 0; j < columnDimension; j++) {
                matrix.setEntry(i, j, matrix.getEntry(i, j) - columnMeans[j]);
            }
        }

        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        RealMatrix uMatrix = svd.getU();

        double sum = 0.0;
        int uRows = uMatrix.getRowDimension();
        int uCols = uMatrix.getColumnDimension();

        for (int i = 0; i < uRows; i++) {
            for (int j = 0; j < uCols; j++) {
                sum += uMatrix.getEntry(i, j);
            }
        }

        DecimalFormat df = new DecimalFormat("#.#######");
        double mean = sum / (uRows * uCols);
        return df.format(mean);
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

    private void saveParkingSlotInfos(ParkingTrainingRequest request, Long mediaId, Media media) {
        parkingSlotInfoRepository.deleteByMediaIdAndIsTrain(mediaId);
        ZonedDateTime currentDatetime = ZonedDateTime.now();

        List<ParkingSlotInfo> parkingSlots = request.getSlotDetails().stream()
                .map(slotDetail -> createParkingSlotInfo(slotDetail, media, currentDatetime))
                .toList();

        parkingSlotInfoRepository.saveAll(parkingSlots);
    }

    private ParkingSlotInfo createParkingSlotInfo(ParkingSlotDetail slotDetail, Media media, ZonedDateTime createdAt) {
        ParkingSlotInfo parkingSlotInfo = ParkingSlotInfo.builder()
                .slotIndex(slotDetail.getIndex())
                .isTrain(true)
                .isEmpty(slotDetail.getIsEmpty())
                .createdAt(createdAt)
                .media(media)
                .build();

        List<CoordinatesInfo> coordinatesInfos = slotDetail.getPoints().stream()
                .map(point -> createCoordinatesInfo(point, parkingSlotInfo))
                .toList();

        parkingSlotInfo.setCoordinatesInfos(coordinatesInfos);
        return parkingSlotInfo;
    }

    private CoordinatesInfo createCoordinatesInfo(PointInfo point, ParkingSlotInfo parkingSlotInfo) {
        return CoordinatesInfo.builder()
                .pointOrder(point.getId())
                .x(point.getX())
                .y(point.getY())
                .realX(point.getRealX())
                .realY(point.getRealY())
                .parkingSlotInfo(parkingSlotInfo)
                .build();
    }

}
