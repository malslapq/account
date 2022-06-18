package com.zero.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.account.dto.TransactionDto;
import com.zero.account.dto.TransactionInfo;
import com.zero.account.exception.AccountException;
import com.zero.account.service.TransactionService;
import com.zero.account.type.ErrorCode;
import com.zero.account.type.TransactionResultStatus;
import com.zero.account.type.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @DisplayName("금액 사용 거래 성공")
    @Test
    void successTransactionUse() throws Exception {
        // given
        given(transactionService.transactionUse(any(), any(), any())).willReturn(
                TransactionDto.builder()
                        .transactionId(1L)
                        .accountNumber("12345")
                        .transactionStatus(TransactionStatus.APPROVAL)
                        .transactionResultStatus(TransactionResultStatus.SUCCEED)
                        .transactionAmount(5000L)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        Map<String, Object> input = new HashMap<>();
        input.put("userId", 1L);
        input.put("accountNumber", "12345");
        input.put("transactionAmount", 5000L);

        // when

        // then
        mockMvc.perform(post("/transaction/use.do")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.transactionAmount")
                        .value(5000L))
                .andExpect(jsonPath("$.accountNumber")
                        .value("12345"))
                .andExpect(jsonPath("$.transactionResultStatus")
                        .value("SUCCEED"))
                .andExpect(status().isOk());
    }

    @DisplayName("사용 거래 취소")
    @Test
    void successTransactionCancel() throws Exception {
        // given
        given(transactionService.transactionCancel(any(), any(), any())).willReturn(
                TransactionDto.builder()
                        .transactionId(1L)
                        .accountNumber("12345")
                        .transactionStatus(TransactionStatus.CANCEL)
                        .transactionResultStatus(TransactionResultStatus.SUCCEED)
                        .transactionAmount(5000L)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        Map<String, Object> input = new HashMap<>();
        input.put("transactionId", 1L);
        input.put("accountNumber", "12345");
        input.put("transactionAmount", 5000L);

        // when

        // then
        mockMvc.perform(post("/transaction/cancel.do")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(jsonPath("$.transactionId").value(1L))
                .andExpect(jsonPath("$.transactionAmount")
                        .value(5000L))
                .andExpect(jsonPath("$.accountNumber")
                        .value("12345"))
                .andExpect(jsonPath("$.transactionResultStatus")
                        .value("SUCCEED"))
                .andExpect(status().isOk());
    }

    @DisplayName("거래 조회")
    @Test
    void successSelectTransaction() throws Exception {
        // given
        given(transactionService.selectTransaction(any())).willReturn(
                TransactionInfo.builder()
                        .transactionId(1L)
                        .accountNumber("12345")
                        .transactionStatus(TransactionStatus.CANCEL)
                        .transactionResultStatus(TransactionResultStatus.FAILED)
                        .transactionAmount(5000L)
                        .registeredAt(LocalDateTime.now())
                        .build()

        );

        // when

        // then
        mockMvc.perform(get("/transactions/1"))
                .andDo(print())
                .andExpect(jsonPath("$.transactionId").value(1L))
                .andExpect(jsonPath("$.transactionStatus")
                        .value("CANCEL"))
                .andExpect(jsonPath("$.transactionAmount")
                        .value(5000L))
                .andExpect(jsonPath("$.accountNumber")
                        .value("12345"))
                .andExpect(jsonPath("$.transactionResultStatus")
                        .value("FAILED"))
                .andExpect(status().isOk());
    }


}