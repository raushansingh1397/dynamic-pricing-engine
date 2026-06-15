package com.example.dps.repository;

import com.example.dps.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceAlertRepo extends JpaRepository<PriceAlert, Integer> {
}
