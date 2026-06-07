package com.example.dps.service;

import com.example.dps.entity.Rules;
import com.example.dps.repository.RulesRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleService {
    private final RulesRepo repo;

    public RuleService(RulesRepo repo) {
        this.repo = repo;
    }

    public List<Rules> findActiveRules(){
        return repo.findByIsActiveTrue();
    }
}
