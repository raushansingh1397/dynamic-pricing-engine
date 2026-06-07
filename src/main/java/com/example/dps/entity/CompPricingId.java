package com.example.dps.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompPricingId implements Serializable {
    @Column(name = "prod_id")
    private Integer prodId;
    @Column(name = "comp_id")
    private Integer compId;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CompPricingId obj = (CompPricingId) o;
        return Objects.equals(prodId, obj.prodId) && Objects.equals(compId, obj.compId);
    }

    public int hashCode(){
        return Objects.hash(prodId,compId);
    }
}
