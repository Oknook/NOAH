package com.noah.backend.domain.groupaccount.service.impl;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.noah.backend.domain.account.entity.Account;
import com.noah.backend.domain.account.repository.AccountRepository;
import com.noah.backend.domain.account.service.AccountService;
import com.noah.backend.domain.bank.dto.requestDto.BankAccountBalanceCheckReqDto;
import com.noah.backend.domain.bank.dto.requestDto.BankAccountTransferReqDto;
import com.noah.backend.domain.bank.service.BankService;
import com.noah.backend.domain.groupaccount.dto.requestDto.DepositReqDto;
import com.noah.backend.domain.groupaccount.dto.requestDto.GroupAccountPostDto;
import com.noah.backend.domain.groupaccount.dto.requestDto.GroupAccountUpdateDto;
import com.noah.backend.domain.groupaccount.dto.responseDto.GroupAccountInfoDto;
import com.noah.backend.domain.groupaccount.entity.GroupAccount;
import com.noah.backend.domain.groupaccount.repository.GroupAccountRepository;
import com.noah.backend.domain.groupaccount.service.GroupAccountService;
import com.noah.backend.domain.member.entity.Member;
import com.noah.backend.domain.member.repository.MemberRepository;
import com.noah.backend.domain.member.service.member.MemberService;
import com.noah.backend.domain.memberTravel.Repository.MemberTravelRepository;
import com.noah.backend.domain.memberTravel.dto.Response.GetTravelListResDto;
import com.noah.backend.domain.memberTravel.dto.Response.MemberTravelListGetDto;
import com.noah.backend.domain.memberTravel.entity.MemberTravel;
import com.noah.backend.domain.trade.entity.Trade;
import com.noah.backend.domain.trade.repository.TradeRepository;
import com.noah.backend.domain.travel.entity.Travel;
import com.noah.backend.domain.travel.repository.TravelRepository;
import com.noah.backend.global.exception.account.AccountNotFoundException;
import com.noah.backend.global.exception.groupaccount.GroupAccountAccessDeniedException;
import com.noah.backend.global.exception.groupaccount.GroupAccountNotFoundException;
import com.noah.backend.global.exception.member.MemberNotFoundException;
import com.noah.backend.global.exception.membertravel.MemberTravelAccessException;
import com.noah.backend.global.exception.membertravel.MemberTravelNotFoundException;
import com.noah.backend.global.exception.travel.TravelMemberNotFoundException;
import com.noah.backend.global.exception.travel.TravelNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupAccountServiceImpl implements GroupAccountService {

    private final GroupAccountRepository groupAccountRepository;
    private final AccountRepository accountRepository;
    private final TravelRepository travelRepository;
    private final MemberTravelRepository memberTravelRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final BankService bankService;
    private final AccountService accountService;
    private final TradeRepository tradeRepository;

    @Transactional
    @Override
    public List<GroupAccountInfoDto> getGroupAccountListByMemberId(Long memberId) throws IOException {
        // accountId List 받아오기
        List<Long> accountIds = groupAccountRepository.getGroupAccountIdsByMemberId(memberId).orElseThrow(GroupAccountNotFoundException::new);
        for (Long accountId : accountIds) {
            Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
            String userKey = account.getMember().getUserKey();
            Map<String, String> bankCodeMap = Map.of(
                    "한국은행", "001",
                    "산업은행", "002",
                    "기업은행", "003",
                    "국민은행", "004"
            );
            String bankCode = bankCodeMap.get(account.getBankName());

            // 은행에서 잔액조회, 금액 최신화
            BankAccountBalanceCheckReqDto bankAccountBalanceCheckReqDto = BankAccountBalanceCheckReqDto.builder()
                    .userKey(userKey)
                    .bankCode(bankCode)
                    .accountNo(account.getAccountNumber())
                    .build();
            int amount = bankService.bankAccountBalanceCheck(bankAccountBalanceCheckReqDto).getAccountBalance();
            account.setAmount(amount);
//            accountRepository.save(account);
        }
        // 통장정보 반환
        return groupAccountRepository.getGroupAccountListByMemberId(memberId).orElseThrow(GroupAccountNotFoundException::new);
    }

    @Transactional
    @Override
    public Long createGroupAccount(GroupAccountPostDto groupAccountPostDto) {
        Account account = accountRepository.findById(groupAccountPostDto.getAccountId()).orElseThrow(AccountNotFoundException::new);
        Travel travel = travelRepository.findById(groupAccountPostDto.getTravelId()).orElseThrow(TravelNotFoundException::new);
        GroupAccount groupAccount = GroupAccount.builder()
                .account(account)
                .travel(travel)
                .build();
        groupAccountRepository.save(groupAccount);
        return groupAccount.getId();
    }

    @Override
    public GroupAccountInfoDto groupAccountInfo(String email, Long groupAccountId) {

        /* 접근권한 */
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        GroupAccount groupAccount = groupAccountRepository.findById(groupAccountId).orElseThrow(GroupAccountNotFoundException::new);
        MemberTravel memberTravel = memberTravelRepository.findByTravelIdAndMemberId(member.getId(), groupAccount.getTravel().getId()).orElseThrow(
            MemberTravelAccessException::new);
        /* ------ */

        GroupAccountInfoDto groupAccountInfoDto = groupAccountRepository.getGroupAccountInfo(groupAccountId).orElseThrow(GroupAccountNotFoundException::new);

        return groupAccountInfoDto;
    }
    @Override
    public GroupAccountInfoDto groupAccountInfoByTravelId(String email, Long travelId) {

        /* 접근권한 */
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Travel travel = travelRepository.findById(travelId).orElseThrow(TravelNotFoundException::new);
        GroupAccount groupAccount = groupAccountRepository.findById(travel.getGroupAccount().getId()).orElseThrow(GroupAccountNotFoundException::new);
        MemberTravel memberTravel = memberTravelRepository.findByTravelIdAndMemberId(member.getId(), groupAccount.getTravel().getId()).orElseThrow(
            MemberTravelAccessException::new);
        /* ------ */

        GroupAccountInfoDto groupAccountInfoDto = groupAccountRepository.getGroupAccountInfoByTravelId(travelId).orElseThrow(GroupAccountNotFoundException::new);

        return groupAccountInfoDto;
    }

    @Override
    public Long updateGroupAccount(Long memberId, GroupAccountUpdateDto groupAccountUpdateDto) {
        GroupAccount groupAccount = groupAccountRepository.findById(groupAccountUpdateDto.getGroupAccountId()).orElseThrow(GroupAccountNotFoundException::new);
        if (!groupAccount.getAccount().getMember().getId().equals(memberId)) {
            throw new GroupAccountAccessDeniedException();
        }
        if (groupAccountUpdateDto.getTargetAmount() != 0) {
            groupAccount.setTargetAmount(groupAccountUpdateDto.getTargetAmount());
        }
        if (groupAccountUpdateDto.getTargetDate() != 0) {
            groupAccount.setTargetDate(groupAccountUpdateDto.getTargetDate());
        }
        if (groupAccountUpdateDto.getPerAmount() != 0) {
            groupAccount.setPerAmount(groupAccountUpdateDto.getPerAmount());
        }
        if (groupAccountUpdateDto.getPaymentDate() != 0) {
            groupAccount.setPaymentDate(groupAccountUpdateDto.getPaymentDate());
        }

        groupAccountRepository.save(groupAccount);
        return groupAccount.getId();
    }

    @Override
    public int getTotalPay(String email, Long travelId) {

        /* 접근권한 */
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        MemberTravel memberTravel = memberTravelRepository.findByTravelIdAndMemberId(member.getId(), travelId).orElseThrow(
            MemberTravelAccessException::new);
        /* ------ */

        Travel travel = travelRepository.findById(travelId).orElseThrow(TravelNotFoundException::new);
        GroupAccount groupAccount = groupAccountRepository.findById(travel.getGroupAccount().getId()).orElseThrow(GroupAccountNotFoundException::new);
        LocalDate today = LocalDate.now();
        LocalDate createdAt = groupAccount.getCreatedAt().toLocalDate();

        int monthsBetween = (int) ChronoUnit.MONTHS.between(createdAt, today);

        // 현재 달의 납부일이 지났는지 확인
        if (today.getDayOfMonth() >= groupAccount.getPaymentDate()) {
            // 현재 달의 납부일이 지났다면, 해당 월도 납부 대상 월로 포함
            monthsBetween++;
        }
        int totalDue = monthsBetween * groupAccount.getPerAmount();
        return totalDue;
    }

    @Override
    public List<MemberTravelListGetDto> getGroupAccountMembers(String email, Long travelId) {

        /* 접근권한 */
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        MemberTravel memberTravel = memberTravelRepository.findByTravelIdAndMemberId(member.getId(), travelId).orElseThrow(
            MemberTravelAccessException::new);
        /* ------ */

        List<MemberTravelListGetDto> result = memberTravelRepository.findByTravelId(travelId).orElseThrow(TravelMemberNotFoundException::new);
        return result;
    }

    @Transactional
    @Override
    public void depositIntoGroupAccount(String email, DepositReqDto depositReqDto) throws IOException {

        /* 접근권한 */
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        MemberTravel memberTravel = memberTravelRepository.findByTravelIdAndMemberId(member.getId(), depositReqDto.getTravelId()).orElseThrow(
            MemberTravelAccessException::new);
        /* ------ */

        /* 돈 보내는 사람 정보 */
        String userName = member.getName();
        String userKey = member.getUserKey();

        Account account = accountRepository.findById(depositReqDto.getAccountId()).orElseThrow(AccountNotFoundException::new);
        Map<String, String> bankCodeMap = Map.of(
                "한국은행", "001",
                "산업은행", "002",
                "기업은행", "003",
                "국민은행", "004"
        );
        String depositBankCode = bankCodeMap.get(account.getBankName());

        /* 돈 받는 사람(모임통장) 정보 */
        Long travelId = depositReqDto.getTravelId();
        Travel travel = travelRepository.findById(travelId).orElseThrow(TravelNotFoundException::new);
        GroupAccountInfoDto groupAccountInfoDto = groupAccountRepository.getGroupAccountInfo(travel.getGroupAccount().getId()).orElseThrow(GroupAccountNotFoundException::new);
        String withDrawBankCode = bankCodeMap.get(groupAccountInfoDto.getBankName());

        int amount = depositReqDto.getAmount();

        // Bank서비스에 맞게 dto 생성
        BankAccountTransferReqDto bankAccountTransferReqDto = BankAccountTransferReqDto.builder()
                .userKey(userKey)
                .depositBankCode(withDrawBankCode)
                .depositAccountNo(groupAccountInfoDto.getAccountNumber())
                .transactionBalance(amount)
                .withdrawalBankCode(depositBankCode)
                .withdrawalAccountNo(account.getAccountNumber())
                .depositTransactionSummary(userName)
                .withdrawalTransactionSummary(userName)
                .build();
        bankService.bankAccountTransfer(bankAccountTransferReqDto);

        // memberTravel의 payment 부분에 최신화를 시켜놓을거임, 기존금액 + 입금액 => 총 납부금액
        int previous = memberTravel.getPayment_amount();
        int total = previous + amount;
        memberTravel.setPayment_amount(total);
        memberTravelRepository.save(memberTravel);


        // ====================================================
        // 거래내역 바로 생성

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

        String createdDate = now.format(dateTimeFormatter);
        String createdTime = now.format(timeFormatter);

        Account aaa = accountRepository.findAccountBytravelId(depositReqDto.getTravelId()).orElseThrow(AccountNotFoundException::new);
        Trade trade = Trade.builder()
            .type(1)
            .name(member.getName())
            .member(member)
            .date(createdDate)
            .time(createdTime)
            .cost(depositReqDto.getAmount())
            .amount(aaa.getAmount()+depositReqDto.getAmount())
            .consumeType("입금")
            .account(aaa)
            .build();
        tradeRepository.save(trade);

        // ====================================================
    }

    // 자동계좌이체
    @Scheduled(cron = "${schedule.auto_pay}")
    @Override
    public void autoTransferGroupAccount() throws IOException {

        int todayDate = LocalDate.now().getDayOfMonth();
        List<MemberTravel> memberTravelList = memberTravelRepository.getAutoTransfer(todayDate).orElse(null);

        if(memberTravelList == null) return;

        for(int i = 0;i<memberTravelList.size();i++){

            Member member = memberTravelList.get(i).getMember();

            DepositReqDto depositReqDto = DepositReqDto.builder()
                .accountId(memberTravelList.get(i).getAccount().getId())
                .travelId(memberTravelList.get(i).getTravel().getId())
                .amount(memberTravelList.get(i).getTravel().getGroupAccount().getPerAmount()).build();
            depositIntoGroupAccount(member.getEmail(), depositReqDto);

        }

    }

}
