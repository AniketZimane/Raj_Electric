package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Entity.Offer;
import com.RajBattery.RajBattery.Repo.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import java.util.List;

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


    public List<Offer> getValidOffers() {
        return offerRepository.findAll().stream()
                .filter(offer -> {
                    if (offer.getEndDate() == null) {
                        return false;
                    }

                    // Parse the endDate to LocalDate
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate parsedEndDate = LocalDate.parse(offer.getEndDate(), formatter);

                    // Check if the end date is greater than or equal to today's date
                    return !parsedEndDate.isBefore(LocalDate.now());
                })
                .collect(Collectors.toList());
    }

}

