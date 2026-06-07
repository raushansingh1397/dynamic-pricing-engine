package com.example.dps.repository;

import com.example.dps.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepo extends JpaRepository<PriceHistory, Integer> {

    List<PriceHistory> findByProductProdId(int prodId);

}
