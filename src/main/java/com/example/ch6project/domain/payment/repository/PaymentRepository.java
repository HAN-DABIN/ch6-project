package com.example.ch6project.domain.payment.repository;

import com.example.ch6project.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
