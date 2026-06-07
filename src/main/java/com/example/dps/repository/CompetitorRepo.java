package com.example.dps.repository;

import com.example.dps.entity.Competitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitorRepo extends JpaRepository<Competitor,Integer> {
}
