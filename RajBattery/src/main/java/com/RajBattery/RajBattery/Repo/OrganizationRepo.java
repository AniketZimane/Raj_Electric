package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepo extends JpaRepository<Organization, Long> {
    Optional<Organization> findFirstByOrderByIdAsc();
    boolean existsByOrgEmail(String email);
    List<Organization> findByStatusOrderByCreatedAtDesc(String status);
    List<Organization> findAllByOrderByCreatedAtDesc();
}
