package com.nhnacademy.illuwa.domain.payment.repository;

import com.nhnacademy.illuwa.domain.payment.entity.CardInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardInfoEntityRepository extends JpaRepository<CardInfoEntity, Long> {
}
