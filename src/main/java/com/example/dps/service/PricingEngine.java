package com.example.dps.service;

import com.example.dps.dto.RuleContext;
import com.example.dps.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PricingEngine {
    private final TrackDemandService trackDemandService;
    private final DemandService demandService;
    private final InventoryService inventoryService;
    private final CompPricingService compPricingService;
    private final ProductService productService;
    private final RuleService ruleService;

    @Autowired
    public PricingEngine(TrackDemandService trackDemandService, DemandService demandService, InventoryService inventoryService, CompPricingService compPricingService,ProductService productService, RuleService ruleService) {
        this.trackDemandService = trackDemandService;
        this.demandService = demandService;
        this.inventoryService = inventoryService;
        this.compPricingService = compPricingService;
        this.productService = productService;
        this.ruleService = ruleService;
    }

    public BigDecimal calculateDynamicPrice(Product product){
        BigDecimal effectiveBase = product.getBasePrice().min(product.getDiscountedPrice());
        RuleContext context = buildRuleContext(product.getProdId());
        List<Rules> activeRules = ruleService.findActiveRules();
        BigDecimal price = applyRules(effectiveBase,context,activeRules);
        return applyPriceBound(price,product.getBasePrice());
    }

    public BigDecimal applyRules(BigDecimal base, RuleContext context,List<Rules> activeRules){
        List<Rules> sortableRules = new ArrayList<>(activeRules);
        BigDecimal price = base;
        sortableRules.sort(Comparator.comparing(Rules::getPriority));

        for(Rules rule: sortableRules){
            if(evaluateCondition(rule,context)){
                price = applyAction(rule,price);
            }
        }
        return  price;
    }

    private BigDecimal applyPriceBound(BigDecimal price, BigDecimal basePrice){
        BigDecimal floor = basePrice.multiply(BigDecimal.valueOf(0.80));
        BigDecimal ceil = basePrice.multiply(BigDecimal.valueOf(1.50));
        return price.max(floor).min(ceil);
    }
    private BigDecimal applyAction(Rules rule, BigDecimal price){
        return switch (rule.getActionName()){
            case "MULTIPLY" -> price.multiply(rule.getActionValue());
            case "ADD" -> price.add(rule.getActionValue());
            case "SET" -> rule.getActionValue();
            default -> price;
        };
    }

    public boolean evaluateCondition(Rules rule, RuleContext ruleContext){
        // {"field":"demandScore","operator":"GT","value":300}
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode condition = mapper.readTree(rule.getRuleCondition());
            String field = condition.get("field").asString();
            String operator = condition.get("operator").asString();
            double value = condition.get("value").asDouble();

            double contextValue = switch (field) {
                case "demandScore" -> ruleContext.getDemandScore();
                case "stockCount" -> ruleContext.getStockCount();
                case "competitorDiff" -> ruleContext.getCompetitorPriceDiff();
                default -> throw new RuntimeException("Unknown field: " + field);
            };


            return switch (operator) {
                case "GT" -> contextValue > value;
                case "LT" -> contextValue < value;
                case "EQ" -> contextValue == value;
                default -> false;
            };
        } catch (Exception e){
            throw  new RuntimeException("Failed to parse rule condition ", e);
        }
    }


    private RuleContext buildRuleContext(int prodId){
        RuleContext ruleContext = new RuleContext();
        double score = 0;
        double avgCompPrice = compPricingService.findAllCompPrices(prodId).stream()
                .mapToDouble(cp->cp.getPrice().doubleValue())
                .average()
                .orElse(0.0);
        List<TrackProdDemand> prodDemandList = trackDemandService.getProdDemands(prodId);
        for(TrackProdDemand prod : prodDemandList){
            Integer demandId = prod.getDemand().getDemandId();
            score += demandService.getDemandScore(demandId) * prod.getDemandCount();

        }
        double compDiff = 0.0;
        if(avgCompPrice > 0) {
            compDiff = ((productService.getProductById(prodId).getDiscountedPrice().doubleValue() - avgCompPrice) / avgCompPrice) * 100;
        }
        ruleContext.setDemandScore(score);
        ruleContext.setStockCount(inventoryService.getStockCount(prodId));
        ruleContext.setCompetitorPriceDiff(compDiff);
        return ruleContext;
    }
}
