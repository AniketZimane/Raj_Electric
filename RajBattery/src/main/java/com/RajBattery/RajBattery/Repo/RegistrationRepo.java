package com.RajBattery.RajBattery.Repo;

import com.RajBattery.RajBattery.Entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistrationRepo extends JpaRepository<Registration, Integer> {

    List<Registration> findByEmail(String email);

    @Query("from Registration where email=:email")
    Registration getByEmail(String email);

    @Query("SELECT p.userType FROM Registration p WHERE p.username = :username")
    String findUserTypeByUsername(@Param("username") String username);

    @Query("SELECT count(p.email) FROM Registration p WHERE p.userType = 0")
    Integer findCountByUserType();

    List<Registration> findByStatusOrderByRegisteredAtDesc(String status);

    List<Registration> findByCompanyIdOrderByRegisteredAtDesc(Long companyId);

    List<Registration> findAllByOrderByRegisteredAtDesc();

    boolean existsByEmail(String email);
}
