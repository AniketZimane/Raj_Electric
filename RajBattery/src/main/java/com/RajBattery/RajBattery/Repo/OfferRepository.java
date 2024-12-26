package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer,Long> {

    @Query("SELECT o FROM Offer o WHERE o.endDate >= CURRENT_DATE")
    List<Offer> findValidOffers();

}
