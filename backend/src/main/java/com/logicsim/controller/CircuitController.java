package com.logicsim.controller;

import com.logicsim.dto.CircuitDTO;
import com.logicsim.model.Circuit;
import com.logicsim.model.Gate;
import com.logicsim.model.Connection;
import com.logicsim.service.CircuitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/circuits")
@CrossOrigin(origins = "*")
public class CircuitController {

    @Autowired
    private CircuitService circuitService;

    @GetMapping
    public ResponseEntity<List<Circuit>> getAllCircuits() {
        return ResponseEntity.ok(circuitService.getAllCircuits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Circuit> getCircuit(@PathVariable Long id) {
        return circuitService.getCircuit(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Circuit> createCircuit(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "New Circuit");
        String description = body.getOrDefault("description", "");
        return ResponseEntity.ok(circuitService.createCircuit(name, description));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Circuit> updateCircuit(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "");
        String description = body.getOrDefault("description", "");
        Circuit circuit = circuitService.updateCircuit(id, name, description);
        if (circuit != null) {
            return ResponseEntity.ok(circuit);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCircuit(@PathVariable Long id) {
        circuitService.deleteCircuit(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{circuitId}/detail")
    public ResponseEntity<CircuitDTO> getCircuitDetail(@PathVariable Long circuitId) {
        return ResponseEntity.ok(circuitService.getCircuitDetail(circuitId));
    }

    @PostMapping("/{circuitId}/detail")
    public ResponseEntity<CircuitDTO> saveCircuitDetail(@PathVariable Long circuitId, @RequestBody CircuitDTO dto) {
        dto.setCircuitId(circuitId);
        return ResponseEntity.ok(circuitService.saveCircuitDetail(dto));
    }

    @GetMapping("/{circuitId}/gates")
    public ResponseEntity<List<Gate>> getGates(@PathVariable Long circuitId) {
        CircuitDTO dto = circuitService.getCircuitDetail(circuitId);
        return ResponseEntity.ok(dto.getGates());
    }

    @PostMapping("/{circuitId}/gates")
    public ResponseEntity<Gate> addGate(@PathVariable Long circuitId, @RequestBody Gate gate) {
        return ResponseEntity.ok(circuitService.addGate(circuitId, gate));
    }

    @PutMapping("/gates/{gateId}")
    public ResponseEntity<Gate> updateGate(@PathVariable Long gateId, @RequestBody Gate gate) {
        Gate updated = circuitService.updateGate(gateId, gate);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/gates/{gateId}")
    public ResponseEntity<Void> deleteGate(@PathVariable Long gateId) {
        circuitService.deleteGate(gateId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{circuitId}/connections")
    public ResponseEntity<List<Connection>> getConnections(@PathVariable Long circuitId) {
        CircuitDTO dto = circuitService.getCircuitDetail(circuitId);
        return ResponseEntity.ok(dto.getConnections());
    }

    @PostMapping("/{circuitId}/connections")
    public ResponseEntity<Connection> addConnection(@PathVariable Long circuitId, @RequestBody Connection connection) {
        return ResponseEntity.ok(circuitService.addConnection(circuitId, connection));
    }

    @DeleteMapping("/connections/{connectionId}")
    public ResponseEntity<Void> deleteConnection(@PathVariable Long connectionId) {
        circuitService.deleteConnection(connectionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{circuitId}/simulate")
    public ResponseEntity<List<Gate>> simulateCircuit(@PathVariable Long circuitId) {
        return ResponseEntity.ok(circuitService.simulateCircuit(circuitId));
    }
}
