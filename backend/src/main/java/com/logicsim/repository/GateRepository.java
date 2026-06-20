package com.logicsim.repository;

import com.logicsim.model.Gate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GateRepository extends JpaRepository<Gate, Long> {
    List<Gate> findByCircuitId(Long circuitId);
    void deleteByCircuitId(Long circuitId);
    
    @Query("SELECT COALESCE(MAX(g.id), 0) FROM Gate g")
    Long getMaxId();
}
