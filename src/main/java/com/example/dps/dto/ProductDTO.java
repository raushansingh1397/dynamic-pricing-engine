package com.example.dps.dto;

import com.example.dps.entity.Inventory;
import com.example.dps.entity.PriceHistory;
import com.example.dps.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private int prodId;
    private String prodName;
    private String prodCategory;
    private String prodCompanyName;
    private BigDecimal currentDynamicPrice;
    private BigDecimal discountedPrice;

    private int prodCount;
    private List<HistoryDTO> priceHistoryList;

    public static ProductDTO createProdObj(Product prod,Inventory inventory){
        List<PriceHistory> histories = prod.getPriceHistories();
        if(histories != null) {
            return new ProductDTO(
                    prod.getProdId(),
                    prod.getProdName(),
                    prod.getProdCategory(),
                    prod.getProdCompanyName(),
                    prod.getCurrentDynamicPrice(),
                    prod.getDiscountedPrice(),
                    inventory.getProdCount(),
                    createHistoryListObj(histories)
            );
        }
        return new ProductDTO(
                prod.getProdId(),
                prod.getProdName(),
                prod.getProdCategory(),
                prod.getProdCompanyName(),
                prod.getCurrentDynamicPrice(),
                prod.getDiscountedPrice(),
                inventory.getProdCount(),
                new ArrayList<>());
    }
    public static List<HistoryDTO> createHistoryListObj(List<PriceHistory> priceHistories) {
        List<HistoryDTO> dtoList = new ArrayList<>();
        for(PriceHistory priceHistory: priceHistories){
            dtoList.add(HistoryDTO.createHistoryDTO(priceHistory));
        }
        return dtoList;
    }

}
