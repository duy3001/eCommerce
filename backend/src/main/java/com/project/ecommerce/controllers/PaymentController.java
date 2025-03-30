package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.PaymentDTO;
import com.project.ecommerce.dtos.PaymentQueryDTO;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.services.payment.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/payments")
public class PaymentController {

    private final IVNPayService vnPayService;

    @PostMapping("/create_payment_url")
    public ResponseEntity<ResponseObject> createPayment(@RequestBody PaymentDTO paymentDTO, HttpServletRequest request) {
        try{
            String paymentUrl = vnPayService.createPaymentUrl(paymentDTO, request);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Payment URL generated successfully.")
                    .data(paymentUrl)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Error generating payment URL: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/query")
    public ResponseEntity<ResponseObject> queryTransaction(@RequestBody PaymentQueryDTO queryDTO, HttpServletRequest request) {
        try{
            String queryUrl = vnPayService.queryTransaction(queryDTO, request);
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Successfully")
                    .data(queryUrl)
                    .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Error query transaction : " + e.getMessage())
                            .build());
        }
    }
}
