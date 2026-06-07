package com.example.dps.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ruleId;
    private String ruleName;
    @Column(columnDefinition = "json")
    private String ruleCondition;
    private String actionName;
    @Column(precision = 10, scale = 2)
    private BigDecimal actionValue;
    private Integer priority;
    private Boolean isActive;
}
