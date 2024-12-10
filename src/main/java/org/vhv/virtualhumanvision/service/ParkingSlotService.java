package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.dto.request.ParkingSlotDetail;
import org.vhv.virtualhumanvision.dto.request.ParkingTrainingRequest;
import org.vhv.virtualhumanvision.dto.request.PointInfo;
import org.vhv.virtualhumanvision.entity.CoordinatesInfo;
import org.vhv.virtualhumanvision.entity.Media;
import org.vhv.virtualhumanvision.entity.ParkingSlotInfo;
import org.vhv.virtualhumanvision.repository.CoordinatesInfoRepository;
import org.vhv.virtualhumanvision.repository.MediaRepository;
import org.vhv.virtualhumanvision.repository.ParkingSlotInfoRepository;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingSlotService {

    private final MediaRepository mediaRepository;
    private final ParkingSlotInfoRepository parkingSlotInfoRepository;
    private final CoordinatesInfoRepository coordinatesInfoRepository;

    @Transactional
    public void saveAndTrainModel(ParkingTrainingRequest request) {
        Long mediaId = request.getMediaId();
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

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
