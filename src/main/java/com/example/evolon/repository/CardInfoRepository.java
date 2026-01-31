package com.example.evolon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evolon.entity.CardInfo;
import com.example.evolon.entity.Item;

public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {

	Optional<CardInfo> findByItem(Item item);
}
