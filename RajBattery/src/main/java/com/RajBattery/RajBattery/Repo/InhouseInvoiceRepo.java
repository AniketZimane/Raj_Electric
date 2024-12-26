package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.InhouseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InhouseInvoiceRepo extends JpaRepository<InhouseInvoice,Long> {
    List<InhouseInvoice> findByUserName(String username);
    List<InhouseInvoice> findByOrderId(Long orderId);

    List<InhouseInvoice> findByUserNameAndStatus(String username,String status);

}

