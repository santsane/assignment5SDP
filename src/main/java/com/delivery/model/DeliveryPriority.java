package com.delivery.model;

/**
 * Business priority of a delivery.
 * The surcharge is an extra fee added to the delivery price
 * (urgent deliveries cost more).
 */
public enum DeliveryPriority {
    URGENT(1500),
    STANDARD(0),
    ECONOMY(0),
    ECO(0);

    private final double surcharge;

    DeliveryPriority(double surcharge) {
        this.surcharge = surcharge;
    }

    public double surcharge() {
        return surcharge;
    }
}
