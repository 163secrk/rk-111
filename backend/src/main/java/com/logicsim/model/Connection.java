package com.logicsim.model;

import jakarta.persistence.*;

@Entity
@Table(name = "connections")
public class Connection {

    @Id
    private Long id;

    @Column(name = "circuit_id")
    private Long circuitId;

    @Column(name = "from_gate_id")
    private Long fromGateId;

    @Column(name = "from_port")
    private Integer fromPort;

    @Column(name = "to_gate_id")
    private Long toGateId;

    @Column(name = "to_port")
    private Integer toPort;

    public Connection() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(Long circuitId) {
        this.circuitId = circuitId;
    }

    public Long getFromGateId() {
        return fromGateId;
    }

    public void setFromGateId(Long fromGateId) {
        this.fromGateId = fromGateId;
    }

    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    public Long getToGateId() {
        return toGateId;
    }

    public void setToGateId(Long toGateId) {
        this.toGateId = toGateId;
    }

    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }
}
