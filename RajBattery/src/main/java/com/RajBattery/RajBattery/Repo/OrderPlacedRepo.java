package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.OrderPlaced;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderPlacedRepo extends JpaRepository<OrderPlaced,Long> {
    List<OrderPlaced> findByusername(String username);

    @Query("SELECT SUM(op.netAmt) AS totalNetAmt, "
            + "SUM(op.paidAmount) AS totalPaidAmt, "
            + "(SUM(op.netAmt) - SUM(op.paidAmount)) AS totalDueAmt "
            + "FROM OrderPlaced op")
    List<Object[]> getAggregateAmounts();

    @Query(value = "SELECT * FROM raj_electric.order_placed WHERE net_amt > paid_amount", nativeQuery = true)
    List<OrderPlaced> findByPaidAmountLess();

    @Query("SELECT SUM(o.netAmt) FROM OrderPlaced o")
    Double sumOfNetAmt();

    @Query("SELECT SUM(o.paidAmount) FROM OrderPlaced o")
    Double sumOfPaidAmount();


}
