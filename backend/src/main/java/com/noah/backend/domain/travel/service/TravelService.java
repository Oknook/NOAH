package com.noah.backend.domain.travel.service;

import com.noah.backend.domain.travel.dto.requestDto.TravelGetDto;
import com.noah.backend.domain.travel.dto.requestDto.TravelGetListDto;
import com.noah.backend.domain.travel.dto.responseDto.TravelPostDto;
import com.noah.backend.domain.travel.dto.responseDto.TravelUpdateDto;
import com.noah.backend.domain.travel.entity.Travel;

import java.util.List;

public interface TravelService {

    List<TravelGetListDto> getTravelList();

    TravelGetDto getTravelSelect(Long travelId);

    Long createTravel(TravelPostDto travelDto);

    Long createTravelTest(TravelPostDto travelDto, Long memberId);

    Long updateTravel(Long travelId, TravelUpdateDto travelDto);

    void deleteTravel(Long travelId);
}