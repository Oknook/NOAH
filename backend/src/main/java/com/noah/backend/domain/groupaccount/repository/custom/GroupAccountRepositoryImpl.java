package com.noah.backend.domain.groupaccount.repository.custom;

import com.noah.backend.domain.groupaccount.dto.responseDto.GroupAccountInfoDto;
import com.noah.backend.domain.groupaccount.entity.GroupAccount;
import com.noah.backend.domain.groupaccount.repository.GroupAccountRepository;
import com.noah.backend.domain.memberTravel.dto.Response.GetTravelListResDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.noah.backend.domain.groupaccount.entity.QGroupAccount.groupAccount;
import static com.noah.backend.domain.account.entity.QAccount.account;
import static com.noah.backend.domain.travel.entity.QTravel.travel;
import static com.noah.backend.domain.memberTravel.entity.QMemberTravel.memberTravel;

@RequiredArgsConstructor
public class GroupAccountRepositoryImpl implements GroupAccountRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Optional<GroupAccountInfoDto> getGroupAccountInfo(Long groupAccountId) {
        return Optional.ofNullable(query.select(Projections.constructor(
                        GroupAccountInfoDto.class,
                        groupAccount.id.as("groupAccountId"),
                        groupAccount.travel.id,
                        groupAccount.travel.title.as("title"),
                        groupAccount.account.bankName.as("bankName"),
                        groupAccount.account.accountNumber.as("accountNumber"),
                        groupAccount.account.amount.as("amount"),
                        groupAccount.usedAmount,
                        groupAccount.targetAmount,
                        groupAccount.targetDate,
                        groupAccount.perAmount,
                        groupAccount.paymentDate
                ))
                .from(groupAccount)
                .leftJoin(travel).on(travel.id.eq(groupAccount.travel.id))
                .leftJoin(account).on(account.id.eq(groupAccount.account.id))
                .where(groupAccount.id.eq(groupAccountId)
                        .and(groupAccount.isDeleted.eq(false))
                        .and(account.isDeleted.eq(false))
                        .and(travel.isDeleted.eq(false)))
                .fetchOne());
    }

    @Override
    public Optional<List<GroupAccountInfoDto>> getGroupAccountListByMemberId(Long memberId) {
        List<Long> travelIds = query.select(memberTravel.travel.id)
                .from(memberTravel)
                .where(memberTravel.member.id.eq(memberId))
                .fetch();

        return Optional.ofNullable(query.select(Projections.constructor(
                        GroupAccountInfoDto.class,
                        groupAccount.id.as("groupAccountId"),
                        groupAccount.travel.id,
                        groupAccount.travel.title.as("title"),
                        groupAccount.account.bankName.as("bankName"),
                        groupAccount.account.accountNumber.as("accountNumber"),
                        groupAccount.account.amount.as("amount"),
                        groupAccount.usedAmount,
                        groupAccount.targetAmount,
                        groupAccount.targetDate,
                        groupAccount.perAmount,
                        groupAccount.paymentDate
                ))
                .from(groupAccount)
                .leftJoin(travel).on(travel.id.eq(groupAccount.travel.id))
                .leftJoin(account).on(account.id.eq(groupAccount.account.id))
                .where(groupAccount.travel.id.in(travelIds)
                        .and(groupAccount.isDeleted.eq(false))
                        .and(account.isDeleted.eq(false))
                        .and(travel.isDeleted.eq(false)))
                .fetch());
    }

    @Override
    public Optional<List<Long>> getGroupAccountIdsByMemberId(Long memberId) {
        List<Long> travelIds = query.select(memberTravel.travel.id)
                .from(memberTravel)
                .where(memberTravel.member.id.eq(memberId))
                .fetch();

        List<Long> accountIds = query.select(groupAccount.account.id)
                .from(groupAccount)
                .leftJoin(travel).on(travel.id.eq(groupAccount.travel.id))
                .leftJoin(account).on(account.id.eq(groupAccount.account.id))
                .where(groupAccount.travel.id.in(travelIds)
                        .and(groupAccount.isDeleted.eq(false))
                        .and(account.isDeleted.eq(false))
                        .and(travel.isDeleted.eq(false)))
                .fetch();
        return Optional.ofNullable(accountIds);
    }

}