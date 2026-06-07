package com.example.dps.repository;

import com.example.dps.entity.TrackProdDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TrackProductDemandRepo extends JpaRepository<TrackProdDemand, Integer> {
    List<TrackProdDemand> findByProductProdId(Integer prodId);
    @Query("SELECT r FROM TrackProdDemand r WHERE r.product.prodId =:prodId AND r.demand.demandId = :demandId AND DATE(r.recordedAt) = CURRENT_DATE")
    Optional<TrackProdDemand> findTodayRecord(@Param("prodId") Integer prodId, @Param("demandId") Integer demandId);
    @Query("SELECT d FROM TrackProdDemand d WHERE d.product.prodId = :prodId AND DATE(d.recordedAt)= CURRENT_DATE")
    List<TrackProdDemand> findTodayDemands(@Param("prodId") Integer prodId);
}
