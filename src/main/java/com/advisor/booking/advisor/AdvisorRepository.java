package com.advisor.booking.advisor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {

    Optional<Advisor> findByEmail(String email);

    List<Advisor> findAllByOrderByIdAsc();
}
