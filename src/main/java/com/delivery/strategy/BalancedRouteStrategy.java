package com.delivery.strategy;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.util.Fmt;

import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Looks at time, cost, distance and emissions at the same time.
 *
 *   score = normalizedTime x timeWeight
 *         + normalizedCost x costWeight
 *         + normalizedDistance x distanceWeight
 *         + normalizedEmission x emissionWeight
 *
 * Time, money, km and kg can't be added directly (different units), so every value is
 * normalized with min-max between the routes that are still allowed:
 *   normalized = (value - min) / (max - min)     -> 0 = best route, 1 = worst route
 * If all routes have the same value, the normalized value is 0 (nobody is better).
 *
 * The weights must be between 0 and 1 and add up to 1.0, otherwise the constructor throws.
 */
public class BalancedRouteStrategy extends AbstractRouteStrategy {

    private static final double TOLERANCE = 1e-6; // double sums are never exactly 1.0

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy() {
        this(0.40, 0.30, 0.10, 0.20);
    }

    public BalancedRouteStrategy(double timeWeight, double costWeight,
                                 double distanceWeight, double emissionWeight) {
        checkWeight("time", timeWeight);
        checkWeight("cost", costWeight);
        checkWeight("distance", distanceWeight);
        checkWeight("emission", emissionWeight);
        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;
        if (Math.abs(sum - 1.0) > TOLERANCE) {
            throw new IllegalArgumentException("Weights must add up to 1.0 but the sum is " + sum);
        }
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    private static void checkWeight(String name, double weight) {
        if (!(weight >= 0 && weight <= 1)) {
            throw new IllegalArgumentException("The " + name + " weight must be between 0 and 1 but was " + weight);
        }
    }

    /** The four normalized values of one route (0 = best, 1 = worst). */
    private record Normalized(double time, double cost, double distance, double emission) {
    }

    private Normalized normalize(Route route, DeliveryRequest request, List<Route> candidates) {
        ToDoubleFunction<Route> time = Route::effectiveMinutes;
        ToDoubleFunction<Route> cost = r -> r.totalCost(request.priority());
        ToDoubleFunction<Route> distance = Route::distanceKm;
        ToDoubleFunction<Route> emission = Route::carbonEmission;
        return new Normalized(
                minMax(route, candidates, time),
                minMax(route, candidates, cost),
                minMax(route, candidates, distance),
                minMax(route, candidates, emission));
    }

    private static double minMax(Route route, List<Route> candidates, ToDoubleFunction<Route> value) {
        double min = candidates.stream().mapToDouble(value).min().orElse(0);
        double max = candidates.stream().mapToDouble(value).max().orElse(0);
        if (max - min < 1e-12) {
            return 0.0;
        }
        return (value.applyAsDouble(route) - min) / (max - min);
    }

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        Normalized n = normalize(route, request, candidates);
        return n.time() * timeWeight
                + n.cost() * costWeight
                + n.distance() * distanceWeight
                + n.emission() * emissionWeight;
    }

    @Override
    protected List<String> explain(Route winner, DeliveryRequest request,
                                   List<Route> candidates, double score) {
        Normalized n = normalize(winner, request, candidates);
        return List.of(
                "Time: " + winner.effectiveMinutes() + " min -> normalized " + Fmt.dec2(n.time())
                        + " x " + Fmt.dec2(timeWeight) + " = " + Fmt.dec2(n.time() * timeWeight),
                "Cost: " + Fmt.money(winner.totalCost(request.priority())) + " -> normalized " + Fmt.dec2(n.cost())
                        + " x " + Fmt.dec2(costWeight) + " = " + Fmt.dec2(n.cost() * costWeight),
                "Distance: " + Fmt.dec1(winner.distanceKm()) + " km -> normalized " + Fmt.dec2(n.distance())
                        + " x " + Fmt.dec2(distanceWeight) + " = " + Fmt.dec2(n.distance() * distanceWeight),
                "CO2: " + Fmt.dec2(winner.carbonEmission()) + " kg -> normalized " + Fmt.dec2(n.emission())
                        + " x " + Fmt.dec2(emissionWeight) + " = " + Fmt.dec2(n.emission() * emissionWeight),
                "Balanced score: " + Fmt.dec2(score) + " (lowest wins)"
        );
    }

    @Override
    public String getStrategyName() {
        return "Balanced";
    }

    public double getTimeWeight() {
        return timeWeight;
    }

    public double getCostWeight() {
        return costWeight;
    }

    public double getDistanceWeight() {
        return distanceWeight;
    }

    public double getEmissionWeight() {
        return emissionWeight;
    }
}
