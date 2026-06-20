package com.logicsim.dto;

import com.logicsim.model.Gate;
import com.logicsim.model.Connection;
import java.util.List;

public class CircuitDTO {
    private Long circuitId;
    private String name;
    private List<Gate> gates;
    private List<Connection> connections;

    public CircuitDTO() {
    }

    public Long getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(Long circuitId) {
        this.circuitId = circuitId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public void setGates(List<Gate> gates) {
        this.gates = gates;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }
}
