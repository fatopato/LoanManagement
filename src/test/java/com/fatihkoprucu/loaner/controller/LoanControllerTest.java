package com.fatihkoprucu.loaner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fatihkoprucu.loaner.config.SecurityConfig;
import com.fatihkoprucu.loaner.dto.LoanRequest;
import com.fatihkoprucu.loaner.dto.LoanResponse;
import com.fatihkoprucu.loaner.service.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
@Import(SecurityConfig.class)
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateLoan_Admin_ShouldCreateLoan() throws Exception {
        // Arrange
        LoanRequest loanRequest = new LoanRequest(1L, BigDecimal.valueOf(1000.0), 0.3, 12);
        LoanResponse loanResponse = new LoanResponse(
            1L, 1L, "John Doe", 
            BigDecimal.valueOf(1000.0), 
            BigDecimal.valueOf(1300.0), 
            12, 
            LocalDate.now(), 
            false,
            Collections.emptyList()
        );
        
        when(loanService.createLoan(any(LoanRequest.class))).thenReturn(loanResponse);

        // Act & Assert
        mockMvc.perform(post("/loans/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.loanAmount").value(1000.0))
                .andExpect(jsonPath("$.totalAmount").value(1300.0))
                .andExpect(jsonPath("$.numberOfInstallments").value(12))
                .andExpect(jsonPath("$.paid").value(false));

        verify(loanService, times(1)).createLoan(any(LoanRequest.class));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testCreateLoan_Customer_ShouldForbidden() throws Exception {
        // Arrange
        LoanRequest loanRequest = new LoanRequest(1L, BigDecimal.valueOf(1000.0), 0.3, 12);

        // Act & Assert
        mockMvc.perform(post("/loans/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isForbidden());

        verify(loanService, never()).createLoan(any(LoanRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetLoan_Admin_ShouldReturnLoan() throws Exception {
        // Arrange
        Long loanId = 1L;
        LoanResponse loanResponse = new LoanResponse(
            loanId, 1L, "John Doe", 
            BigDecimal.valueOf(1000.0), 
            BigDecimal.valueOf(1300.0), 
            12, 
            LocalDate.now(), 
            false,
            Collections.emptyList()
        );
        
        when(loanService.getLoanById(loanId)).thenReturn(loanResponse);

        // Act & Assert
        mockMvc.perform(get("/loans/" + loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.loanAmount").value(1000.0))
                .andExpect(jsonPath("$.totalAmount").value(1300.0))
                .andExpect(jsonPath("$.numberOfInstallments").value(12))
                .andExpect(jsonPath("$.paid").value(false));

        verify(loanService, times(1)).getLoanById(loanId);
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testGetLoan_Customer_ShouldForbidden() throws Exception {
        mockMvc.perform(get("/loans/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetLoanList_Admin_ShouldReturnLoans() throws Exception {
        mockMvc.perform(get("/loans/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testGetLoanList_Customer_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/loans/"))
                .andExpect(status().isForbidden());
    }
}
