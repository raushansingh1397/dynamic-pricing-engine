package com.example.dps.service;

import com.example.dps.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PricingEngineTest {
    @Mock
    private TrackDemandService trackDemandService;
    @Mock
    private DemandService demandService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private CompPricingService compPricingService;
    @Mock
    private ProductService productService;
    @Mock
    private RuleService ruleService;

    @InjectMocks
    private PricingEngine pricingEngine;

    @Test
    void testCalculateDynamicPrice_withNoRules() {
        // Arrange: Create real Product object
        Product product = new Product();
        product.setProdId(1);
        product.setBasePrice(new BigDecimal("100.00"));
        product.setDiscountedPrice(new BigDecimal("80.00"));

        // Mock all dependencies used by buildRuleContext
        when(compPricingService.findAllCompPrices(1)).thenReturn(Collections.emptyList());
        when(trackDemandService.getProdDemands(1)).thenReturn(Collections.emptyList());
        when(inventoryService.getStockCount(1)).thenReturn(50);
        when(ruleService.findActiveRules()).thenReturn(Collections.emptyList());

        // Act
        BigDecimal dynamicPrice = pricingEngine.calculateDynamicPrice(product);

        // Assert
        // effectiveBase = min(100, 80) = 80
        // No rules applied, so price stays 80
        // applyPriceBound(80, 100) = 80 (within bounds 80-150)
        assertEquals(new BigDecimal("80.00"), dynamicPrice);
    }

    @Test
    void testCalculateDynamicPrice_withRuleApplied() {
        // Arrange: Create real Product object
        Product product = new Product();
        product.setProdId(1);
        product.setBasePrice(new BigDecimal("100.00"));
        product.setDiscountedPrice(new BigDecimal("80.00"));

        // Create a rule that will apply (demandScore > 50)
        Rules rule = new Rules();
        rule.setRuleCondition("{\"field\":\"demandScore\",\"operator\":\"GT\",\"value\":50}");
        rule.setActionName("MULTIPLY");
        rule.setActionValue(new BigDecimal("1.10"));
        rule.setPriority(1);
        rule.setIsActive(true);

        // Mock all dependencies
        when(compPricingService.findAllCompPrices(1)).thenReturn(Collections.emptyList());
        when(trackDemandService.getProdDemands(1)).thenReturn(Collections.emptyList());
        when(inventoryService.getStockCount(1)).thenReturn(50);
        when(ruleService.findActiveRules()).thenReturn(List.of(rule));

        // Act
        BigDecimal dynamicPrice = pricingEngine.calculateDynamicPrice(product);

        // Assert
        // effectiveBase = min(100, 80) = 80
        // Rule condition: demandScore (0) > 50 = FALSE, rule not applied
        // price stays 80
        // applyPriceBound(80, 100) = 80
        assertEquals(new BigDecimal("80.00"), dynamicPrice);
    }

    @Test
    void testCalculateDynamicPrice_ruleAppliesMultiply() {
        // Arrange
        Product product = new Product();
        product.setProdId(1);
        product.setBasePrice(new BigDecimal("100.00"));
        product.setDiscountedPrice(new BigDecimal("80.00"));

        // Mock demand service to return high score
        when(trackDemandService.getProdDemands(1)).thenReturn(Collections.emptyList());
        when(inventoryService.getStockCount(1)).thenReturn(5);  // Low stock

        // Rule: if stockCount < 10, multiply by 1.20
        Rules rule = new Rules();
        rule.setRuleCondition("{\"field\":\"stockCount\",\"operator\":\"LT\",\"value\":10}");
        rule.setActionName("MULTIPLY");
        rule.setActionValue(new BigDecimal("1.20"));
        rule.setPriority(1);

        when(compPricingService.findAllCompPrices(1)).thenReturn(Collections.emptyList());
        when(ruleService.findActiveRules()).thenReturn(List.of(rule));

        // Act
        BigDecimal dynamicPrice = pricingEngine.calculateDynamicPrice(product);

        // Assert
        // effectiveBase = min(100, 80) = 80
        // Rule: stockCount (5) < 10 = TRUE, apply MULTIPLY 1.20
        // price = 80 * 1.20 = 96
        // applyPriceBound(96, 100) = 96 (within 80-150)
        assertEquals(new BigDecimal("96.0000"), dynamicPrice);
    }

    @Test
    void testCalculateDynamicPrice_ruleAppliesAdd() {
        // Arrange
        Product product = new Product();
        product.setProdId(1);
        product.setBasePrice(new BigDecimal("100.00"));
        product.setDiscountedPrice(new BigDecimal("80.00"));

        // Mock demand service to return high score
        when(trackDemandService.getProdDemands(1)).thenReturn(Collections.emptyList());
        when(inventoryService.getStockCount(1)).thenReturn(5);  // Low stock

        // Rule: if stockCount < 10, multiply by 1.20
        Rules rule = new Rules();
        rule.setRuleCondition("{\"field\":\"stockCount\",\"operator\":\"LT\",\"value\":10}");
        rule.setActionName("ADD");
        rule.setActionValue(new BigDecimal("1.20"));
        rule.setPriority(1);

        when(compPricingService.findAllCompPrices(1)).thenReturn(Collections.emptyList());
        when(ruleService.findActiveRules()).thenReturn(List.of(rule));

        // Act
        BigDecimal dynamicPrice = pricingEngine.calculateDynamicPrice(product);

        // Assert
        // effectiveBase = min(100, 80) = 80
        // Rule: stockCount (5) < 10 = TRUE, apply ADD 1.20
        // price = 80 + 1.20 = 81.20
        // applyPriceBound(81.20, 100) = 81.20 (within 80-150)
        assertEquals(new BigDecimal("81.20"), dynamicPrice);
    }
    @Test
    void testCalculateDynamicPrice_ruleAppliesSet() {
        // Arrange
        Product product = new Product();
        product.setProdId(1);
        product.setBasePrice(new BigDecimal("100.00"));
        product.setDiscountedPrice(new BigDecimal("80.00"));

        // Mock demand service to return high score
        when(trackDemandService.getProdDemands(1)).thenReturn(Collections.emptyList());
        when(inventoryService.getStockCount(1)).thenReturn(5);  // Low stock

        // Rule: if stockCount < 10, set price to 1.20
        Rules rule = new Rules();
        rule.setRuleCondition("{\"field\":\"stockCount\",\"operator\":\"LT\",\"value\":10}");
        rule.setActionName("SET");
        rule.setActionValue(new BigDecimal("1.20"));
        rule.setPriority(1);

        when(compPricingService.findAllCompPrices(1)).thenReturn(Collections.emptyList());
        when(ruleService.findActiveRules()).thenReturn(List.of(rule));

        // Act
        BigDecimal dynamicPrice = pricingEngine.calculateDynamicPrice(product);

        // Assert
        // effectiveBase = min(100, 80) = 80
        // Rule: stockCount (5) < 10 = TRUE, apply SET 1.20
        // price = 1.20
        // applyPriceBound(1.20, 100) floor=80, ceil=150 => max(1.20, 80) = 80
        assertEquals(new BigDecimal("80.000"), dynamicPrice);
    }

    @Test
    void testCalculateDynamicPrice_ruleAppliesMultiply_compDiff() {
        // Arrange
        Product product = new Product();
        product.setProdId(1);
        product.setBasePrice(new BigDecimal("100.00"));
        product.setDiscountedPrice(new BigDecimal("80.00"));

        // Mock demand service to return high score
        when(trackDemandService.getProdDemands(1)).thenReturn(Collections.emptyList());
        when(inventoryService.getStockCount(1)).thenReturn(5);  // Low stock

        // Rule: if competitorDiff < 10, multiply by 1.20
        Rules rule = new Rules();
        rule.setRuleCondition("{\"field\":\"competitorDiff\",\"operator\":\"LT\",\"value\":10}");
        rule.setActionName("MULTIPLY");
        rule.setActionValue(new BigDecimal("1.20"));
        rule.setPriority(1);

        when(compPricingService.findAllCompPrices(1)).thenReturn(Collections.emptyList());
        when(ruleService.findActiveRules()).thenReturn(List.of(rule));

        // Act
        BigDecimal dynamicPrice = pricingEngine.calculateDynamicPrice(product);

        // Assert
        // effectiveBase = min(100, 80) = 80
        // Rule: competitorDiff (0) < 10 = TRUE, apply MULTIPLY 1.20
        // price = 80 * 1.20 = 96.00
        // applyPriceBound(96.00, 100) = 96.00 (within 80-150)
        assertEquals(new BigDecimal("96.0000"), dynamicPrice);
    }


    @Test
    void testCalculateDynamicPrice_priceBoundedToMaximum() {
        // Arrange
        Product product = new Product();
        product.setProdId(1);
        product.setBasePrice(new BigDecimal("100.00"));
        product.setDiscountedPrice(new BigDecimal("80.00"));

        Rules rule = new Rules();
        rule.setRuleCondition("{\"field\":\"stockCount\",\"operator\":\"LT\",\"value\":10}");
        rule.setActionName("MULTIPLY");
        rule.setActionValue(new BigDecimal("5.00"));  // Excessive multiplier
        rule.setPriority(1);

        when(compPricingService.findAllCompPrices(1)).thenReturn(Collections.emptyList());
        when(trackDemandService.getProdDemands(1)).thenReturn(Collections.emptyList());
        when(inventoryService.getStockCount(1)).thenReturn(5);
        when(ruleService.findActiveRules()).thenReturn(List.of(rule));

        // Act
        BigDecimal dynamicPrice = pricingEngine.calculateDynamicPrice(product);

        // Assert
        // effectiveBase = 80
        // Rule applies: 80 * 5.00 = 400
        // applyPriceBound(400, 100) = 150 (capped at max 150% of base: 150)
        assertEquals(new BigDecimal("150.000"), dynamicPrice);
    }
}