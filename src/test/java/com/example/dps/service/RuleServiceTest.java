package com.example.dps.service;

import com.example.dps.entity.Rules;
import com.example.dps.repository.RulesRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RulesRepo rulesRepo;

    @InjectMocks
    private RuleService ruleService;

    @Test
    void testFindActiveRules_success() {
        // Arrange
        List<Rules> activeRules = new ArrayList<>();
        
        Rules rule1 = new Rules();
        rule1.setRuleId(1);
        rule1.setRuleName("High Demand Rule");
        rule1.setIsActive(true);
        activeRules.add(rule1);

        Rules rule2 = new Rules();
        rule2.setRuleId(2);
        rule2.setRuleName("Low Stock Rule");
        rule2.setIsActive(true);
        activeRules.add(rule2);

        when(rulesRepo.findByIsActiveTrue()).thenReturn(activeRules);

        // Act
        List<Rules> result = ruleService.findActiveRules();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("High Demand Rule", result.get(0).getRuleName());
        assertEquals("Low Stock Rule", result.get(1).getRuleName());
        verify(rulesRepo, times(1)).findByIsActiveTrue();
    }

    @Test
    void testFindActiveRules_empty() {
        // Arrange
        when(rulesRepo.findByIsActiveTrue()).thenReturn(new ArrayList<>());

        // Act
        List<Rules> result = ruleService.findActiveRules();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(rulesRepo, times(1)).findByIsActiveTrue();
    }

    @Test
    void testFindActiveRules_singleRule() {
        // Arrange
        List<Rules> activeRules = new ArrayList<>();
        
        Rules rule = new Rules();
        rule.setRuleId(1);
        rule.setRuleName("Single Rule");
        rule.setIsActive(true);
        activeRules.add(rule);

        when(rulesRepo.findByIsActiveTrue()).thenReturn(activeRules);

        // Act
        List<Rules> result = ruleService.findActiveRules();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Single Rule", result.get(0).getRuleName());
        assertTrue(result.get(0).getIsActive());
        verify(rulesRepo, times(1)).findByIsActiveTrue();
    }

    @Test
    void testFindActiveRules_multipleRules() {
        // Arrange
        List<Rules> activeRules = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Rules rule = new Rules();
            rule.setRuleId(i);
            rule.setRuleName("Rule " + i);
            rule.setIsActive(true);
            activeRules.add(rule);
        }

        when(rulesRepo.findByIsActiveTrue()).thenReturn(activeRules);

        // Act
        List<Rules> result = ruleService.findActiveRules();

        // Assert
        assertNotNull(result);
        assertEquals(10, result.size());
        for (int i = 0; i < activeRules.size(); i++) {
            assertEquals(activeRules.get(i).getRuleId(), result.get(i).getRuleId());
            assertTrue(result.get(i).getIsActive());
        }
        verify(rulesRepo, times(1)).findByIsActiveTrue();
    }

    @Test
    void testFindActiveRules_verifyCorrectStatus() {
        // Arrange
        List<Rules> activeRules = new ArrayList<>();
        
        Rules activeRule = new Rules();
        activeRule.setRuleId(1);
        activeRule.setRuleName("Active Rule");
        activeRule.setIsActive(true);
        activeRules.add(activeRule);

        when(rulesRepo.findByIsActiveTrue()).thenReturn(activeRules);

        // Act
        List<Rules> result = ruleService.findActiveRules();

        // Assert
        assertNotNull(result);
        assertTrue(result.stream().allMatch(Rules::getIsActive));
        verify(rulesRepo, times(1)).findByIsActiveTrue();
    }

    @Test
    void testFindActiveRules_ruleProperties() {
        // Arrange
        List<Rules> activeRules = new ArrayList<>();
        
        Rules rule = new Rules();
        rule.setRuleId(1);
        rule.setRuleName("Complex Rule");
        rule.setIsActive(true);
        rule.setRuleCondition("{\"demand\": \"high\", \"stock\": \"low\"}");
        rule.setActionName("INCREASE_PRICE");
        rule.setActionValue(new BigDecimal("1.2"));
        activeRules.add(rule);

        when(rulesRepo.findByIsActiveTrue()).thenReturn(activeRules);

        // Act
        List<Rules> result = ruleService.findActiveRules();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Rules retrievedRule = result.get(0);
        assertEquals("Complex Rule", retrievedRule.getRuleName());
        assertEquals("{\"demand\": \"high\", \"stock\": \"low\"}", retrievedRule.getRuleCondition());
        assertEquals("INCREASE_PRICE", retrievedRule.getActionName());
        assertEquals(new BigDecimal("1.2"), retrievedRule.getActionValue());
    }

    @Test
    void testFindActiveRules_repoCalledOnce() {
        // Arrange
        when(rulesRepo.findByIsActiveTrue()).thenReturn(new ArrayList<>());

        // Act
        ruleService.findActiveRules();
        ruleService.findActiveRules();

        // Assert
        verify(rulesRepo, times(2)).findByIsActiveTrue();
    }

    @Test
    void testFindActiveRules_nullableFields() {
        // Arrange
        List<Rules> activeRules = new ArrayList<>();
        
        Rules rule = new Rules();
        rule.setRuleId(1);
        rule.setRuleName("Basic Rule");
        rule.setRuleCondition(null);
        rule.setActionName(null);
        rule.setActionValue(null);
        rule.setIsActive(true);
        activeRules.add(rule);

        when(rulesRepo.findByIsActiveTrue()).thenReturn(activeRules);

        // Act
        List<Rules> result = ruleService.findActiveRules();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getRuleCondition());
        assertNull(result.get(0).getActionName());
        assertNull(result.get(0).getActionValue());
    }
}

