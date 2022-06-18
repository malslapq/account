package com.zero.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.account.dto.AccountDto;
import com.zero.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("계좌 생성")
    @Test
    void createAccount() throws Exception {
        // given
        given(accountService.createAccount(anyLong(), any()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("123")
                        .build());
        Map<String, Object> input = new HashMap<>();
        input.put("userId", 1L);
        input.put("initialBalance", 1000L);

        // when

        // then
        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.accountNumber").value("123"))
                .andExpect(status().isOk());

    }

    @DisplayName("계좌 해지")
    @Test
    void patchAccount() throws Exception {
        // given
        given(accountService.updateAccountStatus(anyLong(), any()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("123")
                        .build());
        Map<String, Long> input = new HashMap<>();
        input.put("userId", 2L);
        input.put("accountNumber", 123L);

        // when

        // then
        mockMvc.perform(patch("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.accountNumber").value("123"))
                .andExpect(status().isOk());
    }

    @DisplayName("계좌 조회")
    @Test
    void SelectAccounts() throws Exception {
        // given
        given(accountService.selectAccounts(anyLong()))
                .willReturn(Arrays.asList(
                        AccountDto.builder()
                                .accountNumber("123")
                                .balance(500L)
                                .build(),
                        AccountDto.builder()
                                .accountNumber("12345")
                                .balance(5000L)
                                .build()));

        // when
//        List<AccountInfo> accountInfos = accountService.selectAccounts(1L)
//                .stream().map(accountDto ->
//                        AccountInfo.builder()
//                                .accountNumber(accountDto.getAccountNumber())
//                                .balance(accountDto.getBalance())
//                                .build())
//                .collect(Collectors.toList());


        // then
        mockMvc.perform(get("/accounts?userId=1"))
                .andDo(print())
                .andExpect(jsonPath("$[0].accountNumber").value("123"))
                .andExpect(jsonPath("$[0].balance").value(500L))
                .andExpect(jsonPath("$[1].accountNumber").value("12345"))
                .andExpect(jsonPath("$[1].balance").value(5000L))
                .andExpect(status().isOk());
    }

}