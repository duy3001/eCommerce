package com.project.shopapp.services;

import com.project.shopapp.dtos.PaymentDTO;
import com.project.shopapp.dtos.PaymentQueryDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface IVNPayService {

    String createPaymentUrl(PaymentDTO paymentRequest, HttpServletRequest request);
    String queryTransaction(PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) throws IOException;
//    String refundTransaction(PaymentRefundDTO refundDTO) throws IOException;
}
