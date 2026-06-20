package com.logicsim.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gates")
public class Gate {

    @Id
    private Long id;

    @Column(name = "circuit_id")
    private Long circuitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GateType type;

    private String label;

    private Double x;

    private Double y;

    @Column(name = "input_count")
    private Integer inputCount;

    @Column(name = "output_value")
    private Boolean outputValue;

    @Column(name = "input_value_a")
    private Boolean inputValueA;

    @Column(name = "input_value_b")
    private Boolean inputValueB;

    public Gate() {
    }

    public Gate(GateType type, Double x, Double y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.inputCount = (type == GateType.NOT || type == GateType.INPUT || type == GateType.OUTPUT) ? 1 : 2;
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

    public GateType getType() {
        return type;
    }

    public void setType(GateType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Integer getInputCount() {
        return inputCount;
    }

    public void setInputCount(Integer inputCount) {
        this.inputCount = inputCount;
    }

    public Boolean getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(Boolean outputValue) {
        this.outputValue = outputValue;
    }

    public Boolean getInputValueA() {
        return inputValueA;
    }

    public void setInputValueA(Boolean inputValueA) {
        this.inputValueA = inputValueA;
    }

    public Boolean getInputValueB() {
        return inputValueB;
    }

    public void setInputValueB(Boolean inputValueB) {
        this.inputValueB = inputValueB;
    }
}
