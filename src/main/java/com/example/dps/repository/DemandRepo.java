package com.example.dps.repository;

import com.example.dps.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandRepo extends JpaRepository<Demand, Integer> {
}
