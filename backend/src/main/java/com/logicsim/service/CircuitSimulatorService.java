package com.logicsim.service;

import com.logicsim.model.Gate;
import com.logicsim.model.GateType;
import com.logicsim.model.Connection;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CircuitSimulatorService {

    public List<Gate> simulate(List<Gate> gates, List<Connection> connections) {
        Map<Long, Gate> gateMap = new HashMap<>();
        for (Gate gate : gates) {
            gateMap.put(gate.getId(), gate);
        }

        Map<Long, List<Connection>> incomingConnections = new HashMap<>();
        for (Connection conn : connections) {
            incomingConnections
                .computeIfAbsent(conn.getToGateId(), k -> new ArrayList<>())
                .add(conn);
        }

        Set<Long> visited = new HashSet<>();
        Set<Long> visiting = new HashSet<>();

        for (Gate gate : gates) {
            calculateGateOutput(gate, gateMap, incomingConnections, visited, visiting);
        }

        return gates;
    }

    private boolean calculateGateOutput(Gate gate, Map<Long, Gate> gateMap,
                                         Map<Long, List<Connection>> incomingConnections,
                                         Set<Long> visited, Set<Long> visiting) {
        Long gateId = gate.getId();

        if (visited.contains(gateId)) {
            return gate.getOutputValue() != null && gate.getOutputValue();
        }

        if (visiting.contains(gateId)) {
            return false;
        }

        visiting.add(gateId);

        GateType type = gate.getType();

        if (type == GateType.INPUT) {
            boolean value = gate.getOutputValue() != null && gate.getOutputValue();
            gate.setOutputValue(value);
            visited.add(gateId);
            visiting.remove(gateId);
            return value;
        }

        List<Connection> inConns = incomingConnections.getOrDefault(gateId, Collections.emptyList());

        boolean inputA = false;
        boolean inputB = false;
        boolean hasInputA = false;
        boolean hasInputB = false;

        for (Connection conn : inConns) {
            Gate fromGate = gateMap.get(conn.getFromGateId());
            if (fromGate != null) {
                boolean fromValue = calculateGateOutput(fromGate, gateMap, incomingConnections, visited, visiting);
                int toPort = conn.getToPort() != null ? conn.getToPort() : 0;
                if (toPort == 0) {
                    inputA = fromValue;
                    hasInputA = true;
                } else {
                    inputB = fromValue;
                    hasInputB = true;
                }
            }
        }

        gate.setInputValueA(hasInputA ? inputA : null);
        gate.setInputValueB(hasInputB ? inputB : null);

        boolean output = false;

        switch (type) {
            case AND:
                output = inputA && inputB;
                break;
            case OR:
                output = inputA || inputB;
                break;
            case NOT:
                output = !inputA;
                break;
            case XOR:
                output = inputA ^ inputB;
                break;
            case NAND:
                output = !(inputA && inputB);
                break;
            case NOR:
                output = !(inputA || inputB);
                break;
            case OUTPUT:
                output = inputA;
                break;
            default:
                output = false;
        }

        gate.setOutputValue(output);
        visited.add(gateId);
        visiting.remove(gateId);

        return output;
    }

    public boolean calculateGate(GateType type, boolean... inputs) {
        switch (type) {
            case AND:
                return inputs.length >= 2 && inputs[0] && inputs[1];
            case OR:
                return inputs.length >= 2 && (inputs[0] || inputs[1]);
            case NOT:
                return inputs.length >= 1 && !inputs[0];
            case XOR:
                return inputs.length >= 2 && (inputs[0] ^ inputs[1]);
            case NAND:
                return inputs.length >= 2 && !(inputs[0] && inputs[1]);
            case NOR:
                return inputs.length >= 2 && !(inputs[0] || inputs[1]);
            default:
                return false;
        }
    }
}
