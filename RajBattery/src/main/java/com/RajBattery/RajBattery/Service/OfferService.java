package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.Offer;
import com.RajBattery.RajBattery.Repo.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfferService {
    @Autowired
    private OfferRepository offerRepository;

    public void saveOffer(Offer offer) {
        offerRepository.save(offer);
    }

    public Offer getOfferById(Long id) {
        return offerRepository.findById(id).orElse(null);
    }
}

