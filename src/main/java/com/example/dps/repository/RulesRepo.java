package com.example.dps.repository;

import com.example.dps.entity.Rules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RulesRepo extends JpaRepository<Rules,Integer> {
    List<Rules> findByIsActiveTrue();
}
