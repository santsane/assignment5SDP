package com.delivery.strategy;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.util.Fmt;

import java.util.List;

/**
 * Picks the route with the lowest environmental impact.
 *
 *   ecoScore = carbonEmission x emissionWeight + distanceKm x distanceWeight
 *
 * Why is this inside the strategy and not in Route? Because "what counts as eco-friendly"
 * is a decision of THIS algorithm (the weights can be changed or another formula can be used)
 * and the other strategies don't care about it. Route only stores the raw facts.
 */
public class EcoFriendlyRouteStrategy extends AbstractRouteStrategy {

    private final double emissionWeight;
    private final double distanceWeight;

    public EcoFriendlyRouteStrategy() {
        this(1.0, 0.05);
    }

    public EcoFriendlyRouteStrategy(double emissionWeight, double distanceWeight) {
        if (!(emissionWeight >= 0) || !(distanceWeight >= 0)) {
            throw new IllegalArgumentException("Eco weights cannot be negative (emission="
                    + emissionWeight + ", distance=" + distanceWeight + ")");
        }
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.carbonEmission() * emissionWeight + route.distanceKm() * distanceWeight;
    }

    @Override
    protected List<String> explain(Route winner, DeliveryRequest request,
                                   List<Route> candidates, double score) {
        return List.of(
                "CO2: " + Fmt.dec2(winner.carbonEmission()) + " kg x " + Fmt.dec2(emissionWeight)
                        + " = " + Fmt.dec2(winner.carbonEmission() * emissionWeight),
                "Distance: " + Fmt.dec1(winner.distanceKm()) + " km x " + Fmt.dec2(distanceWeight)
                        + " = " + Fmt.dec2(winner.distanceKm() * distanceWeight),
                "Eco score: " + Fmt.dec2(score) + " (lowest wins)"
        );
    }

    @Override
    public String getStrategyName() {
        return "Eco-Friendly";
    }
}
