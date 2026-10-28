package com.delivery.model;

/**
 * One candidate route for a delivery.
 *
 * @param cost                 base delivery cost of this route
 * @param carbonEmission       estimated CO2 in kg
 * @param trafficLevel         current traffic on the route
 * @param weatherPenaltyMinutes extra minutes because of weather (rain, snow...)
 * @param additionalFees       tolls and other fees
 * @param available            false if the route is closed / cannot be used
 */
public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double cost,
        double carbonEmission,
        TrafficLevel trafficLevel,
        int weatherPenaltyMinutes,
        double additionalFees,
        boolean available
) {

    /** Price per kilometre, used in the total cost formula. */
    public static final double DISTANCE_RATE_PER_KM = 20.0;

    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Route id is missing");
        }
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Route " + id + " needs an origin and a destination");
        }
        if (trafficLevel == null) {
            throw new IllegalArgumentException("Route " + id + " has no traffic level");
        }
        if (!(distanceKm >= 0)) {
            throw new IllegalArgumentException("Route " + id + ": distance cannot be negative (" + distanceKm + ")");
        }
        if (estimatedMinutes < 0) {
            throw new IllegalArgumentException("Route " + id + ": estimated time cannot be negative (" + estimatedMinutes + ")");
        }
        if (!(cost >= 0)) {
            throw new IllegalArgumentException("Route " + id + ": cost cannot be negative (" + cost + ")");
        }
        if (!(carbonEmission >= 0)) {
            throw new IllegalArgumentException("Route " + id + ": emission cannot be negative (" + carbonEmission + ")");
        }
        if (weatherPenaltyMinutes < 0) {
            throw new IllegalArgumentException("Route " + id + ": weather penalty cannot be negative (" + weatherPenaltyMinutes + ")");
        }
        if (!(additionalFees >= 0)) {
            throw new IllegalArgumentException("Route " + id + ": additional fees cannot be negative (" + additionalFees + ")");
        }
    }

    /**
     * Short constructor with the 7 basic fields.
     * Assumes light traffic, no weather problems, no extra fees and an open route.
     */
    public Route(String id, Location origin, Location destination,
                 double distanceKm, int estimatedMinutes, double cost, double carbonEmission) {
        this(id, origin, destination, distanceKm, estimatedMinutes, cost, carbonEmission,
                TrafficLevel.LOW, 0, 0, true);
    }

    /** Extra minutes caused by traffic (estimated time x traffic multiplier - estimated time). */
    public int trafficPenaltyMinutes() {
        return (int) Math.round(estimatedMinutes * trafficLevel.multiplier()) - estimatedMinutes;
    }

    /** Real expected time = estimated + traffic penalty + weather penalty. */
    public int effectiveMinutes() {
        return estimatedMinutes + trafficPenaltyMinutes() + weatherPenaltyMinutes;
    }

    /** Distance part of the price: km x rate per km. */
    public double distanceCost() {
        return distanceKm * DISTANCE_RATE_PER_KM;
    }

    /**
     * Total price of the route for a delivery with the given priority.
     *
     * total = base cost + distance cost + priority surcharge + additional fees
     */
    public double totalCost(DeliveryPriority priority) {
        return cost + distanceCost() + priority.surcharge() + additionalFees;
    }
}
