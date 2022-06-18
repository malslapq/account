package com.zero.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("사용자가 존재하지 않습니다."),
    USER_MIS_MATCH("계좌 사용자가 일치하지 않습니다."),
    NOT_HAVE_ACCOUNT("보유한 계좌가 없습니다."),
    ACCOUNT_NOT_FOUND("이미 해지된 계좌거나 없는 계좌입니다."),
    EXIST_BALANCE("계좌의 잔액이 존재 합니다."),
    TO_MANY_ACCOUNT("계좌 한도수에 도달했습니다."),
    CANCEL_ACCOUNT("이미 해지된 계좌입니다."),
    INSUFFICIENT_BALANCE("계좌의 잔액이 부족합니다."),
    USE_BALANCE_MINIMUM_OUT_OF_RANGE("최소 거래 금액 미만입니다."),
    USE_BALANCE_MAXIMUM_OUT_OF_RANGE("최대 거래 금액을 초과했습니다."),
    TRANSACTION_NOT_FOUND("거래가 존재하지 않습니다."),
    TRANSACTION_ACCOUNT_NUMBER_MIS_MATCH("해당 거래의 계좌 번호가 일치하지 않습니다."),
    TRANSACTION_AMOUNT_MIS_MATCH("거래 금액이 일치하지 않습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),
    ACCOUNT_TRANSACTION_LOCK("현재 요청한 계좌는 사용중입니다.");

    private String description;

}
