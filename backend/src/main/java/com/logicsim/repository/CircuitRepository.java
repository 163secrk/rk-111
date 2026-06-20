package com.logicsim.repository;

import com.logicsim.model.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CircuitRepository extends JpaRepository<Circuit, Long> {
    @Query("SELECT COALESCE(MAX(c.id), 0) FROM Circuit c")
    Long getMaxId();
}
