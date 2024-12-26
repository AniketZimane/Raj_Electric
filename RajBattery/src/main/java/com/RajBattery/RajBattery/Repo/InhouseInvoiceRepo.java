package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.InhouseInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InhouseInvoiceRepo extends JpaRepository<InhouseInvoice,Long> {
    List<InhouseInvoice> findByUserName(String username);
    List<InhouseInvoice> findByOrderId(Long orderId);

    List<InhouseInvoice> findByUserNameAndStatus(String username,String status);

    @Query("SELECT i FROM InhouseInvoice i WHERE i.OldWight > 0")
    Page<InhouseInvoice> findByOldWeight(Pageable pageable);


}

