package com.example.dps.record;


import java.math.BigDecimal;

public record PriceChangeEvent(Object source, Integer productId, BigDecimal newPrice,BigDecimal oldPrice, BigDecimal basePrice, String triggeredBy) {
}
