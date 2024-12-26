package com.RajBattery.RajBattery.Service;

import com.RajBattery.RajBattery.Repo.OrderPlacedRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderPlacedService {

    @Autowired
    private OrderPlacedRepo orderPlacedRepository;

    public Map<String, Double> getAggregateAmounts() {
        List<Object[]> results = orderPlacedRepository.getAggregateAmounts();

        if (results.isEmpty()) {
            return Map.of("totalNetAmt", 0.0, "totalPaidAmt", 0.0, "totalDueAmt", 0.0);
        }

        Object[] row = results.get(0);
        return Map.of(
                "totalNetAmt", (Double) row[0],
                "totalPaidAmt", (Double) row[1],
                "totalDueAmt", (Double) row[2]
        );
    }
}

