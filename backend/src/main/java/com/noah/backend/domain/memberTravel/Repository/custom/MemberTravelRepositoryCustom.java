package com.noah.backend.domain.memberTravel.Repository.custom;

import com.noah.backend.domain.memberTravel.dto.Response.GetTravelListResDto;
import com.noah.backend.domain.memberTravel.dto.Response.MemberTravelGetDto;
import com.noah.backend.domain.memberTravel.dto.Response.MemberTravelListGetDto;
import com.noah.backend.domain.memberTravel.entity.MemberTravel;

import java.util.List;
import java.util.Optional;

public interface MemberTravelRepositoryCustom {

    Optional<List<MemberTravelListGetDto>> findByTravelId(Long travelId);

    Optional<MemberTravelGetDto> getSelect(Long memberTravelId);

    Optional<Long> getMemberTravelByTravelIdAndMemberId(Long travelId, Long memberId);

    Optional<List<MemberTravel>> getAutoTransfer(int todayDate);

	Optional<Integer> totalPeople(Long travelId);

    Optional<MemberTravel> findByTravelIdAndMemberId(Long memberId, Long travelId);

    Optional<Integer> getTotalPeople(Long travelId);

    Optional<List<Long>> findByMemberId(Long memberId);

    Optional<MemberTravel> isAutoTransfer(Long memberId, Long travelId, Long accountId);

    Optional<List<MemberTravel>> findMemberTravelList(Long travelId);
}
