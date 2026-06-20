package com.logicsim.service;

import com.logicsim.dto.CircuitDTO;
import com.logicsim.model.Circuit;
import com.logicsim.model.Gate;
import com.logicsim.model.Connection;
import com.logicsim.repository.CircuitRepository;
import com.logicsim.repository.GateRepository;
import com.logicsim.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CircuitService {

    @Autowired
    private CircuitRepository circuitRepository;

    @Autowired
    private GateRepository gateRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private CircuitSimulatorService simulatorService;

    public List<Circuit> getAllCircuits() {
        return circuitRepository.findAll();
    }

    public Optional<Circuit> getCircuit(Long id) {
        return circuitRepository.findById(id);
    }

    public Circuit createCircuit(String name, String description) {
        Circuit circuit = new Circuit(name);
        circuit.setDescription(description);
        return circuitRepository.save(circuit);
    }

    public Circuit updateCircuit(Long id, String name, String description) {
        Optional<Circuit> opt = circuitRepository.findById(id);
        if (opt.isPresent()) {
            Circuit circuit = opt.get();
            circuit.setName(name);
            circuit.setDescription(description);
            return circuitRepository.save(circuit);
        }
        return null;
    }

    @Transactional
    public void deleteCircuit(Long id) {
        gateRepository.deleteByCircuitId(id);
        connectionRepository.deleteByCircuitId(id);
        circuitRepository.deleteById(id);
    }

    public CircuitDTO getCircuitDetail(Long circuitId) {
        CircuitDTO dto = new CircuitDTO();
        dto.setCircuitId(circuitId);

        Optional<Circuit> circuitOpt = circuitRepository.findById(circuitId);
        circuitOpt.ifPresent(circuit -> dto.setName(circuit.getName()));

        List<Gate> gates = gateRepository.findByCircuitId(circuitId);
        List<Connection> connections = connectionRepository.findByCircuitId(circuitId);

        List<Gate> simulatedGates = simulatorService.simulate(gates, connections);

        dto.setGates(simulatedGates);
        dto.setConnections(connections);

        return dto;
    }

    @Transactional
    public CircuitDTO saveCircuitDetail(CircuitDTO dto) {
        Long circuitId = dto.getCircuitId();

        List<Gate> gates = dto.getGates();
        if (gates != null) {
            for (Gate gate : gates) {
                gate.setCircuitId(circuitId);
                if (gate.getInputCount() == null) {
                    gate.setInputCount(2);
                }
            }
        }

        List<Connection> connections = dto.getConnections();
        if (connections != null) {
            for (Connection conn : connections) {
                conn.setCircuitId(circuitId);
            }
        }

        gateRepository.deleteByCircuitId(circuitId);
        connectionRepository.deleteByCircuitId(circuitId);

        if (gates != null && !gates.isEmpty()) {
            gates = gateRepository.saveAll(gates);
        }

        if (connections != null && !connections.isEmpty()) {
            connections = connectionRepository.saveAll(connections);
        }

        List<Gate> simulatedGates = simulatorService.simulate(gates, connections);

        CircuitDTO result = new CircuitDTO();
        result.setCircuitId(circuitId);
        result.setGates(simulatedGates);
        result.setConnections(connections);

        return result;
    }

    public Gate addGate(Long circuitId, Gate gate) {
        gate.setCircuitId(circuitId);
        if (gate.getInputCount() == null) {
            gate.setInputCount(2);
        }
        return gateRepository.save(gate);
    }

    public Gate updateGate(Long gateId, Gate gate) {
        Optional<Gate> opt = gateRepository.findById(gateId);
        if (opt.isPresent()) {
            Gate existing = opt.get();
            existing.setX(gate.getX());
            existing.setY(gate.getY());
            existing.setLabel(gate.getLabel());
            if (gate.getOutputValue() != null) {
                existing.setOutputValue(gate.getOutputValue());
            }
            return gateRepository.save(existing);
        }
        return null;
    }

    public void deleteGate(Long gateId) {
        gateRepository.deleteById(gateId);
    }

    public Connection addConnection(Long circuitId, Connection connection) {
        connection.setCircuitId(circuitId);
        return connectionRepository.save(connection);
    }

    public void deleteConnection(Long connectionId) {
        connectionRepository.deleteById(connectionId);
    }

    public List<Gate> simulateCircuit(Long circuitId) {
        List<Gate> gates = gateRepository.findByCircuitId(circuitId);
        List<Connection> connections = connectionRepository.findByCircuitId(circuitId);
        return simulatorService.simulate(gates, connections);
    }
}
