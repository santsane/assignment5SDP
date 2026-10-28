package com.delivery.model;

/**
 * How busy the road is. The multiplier stretches the estimated time,
 * e.g. 30 min in HIGH traffic becomes about 41 min.
 */
public enum TrafficLevel {
    LOW(1.00),
    MEDIUM(1.15),
    HIGH(1.35);

    private final double multiplier;

    TrafficLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
