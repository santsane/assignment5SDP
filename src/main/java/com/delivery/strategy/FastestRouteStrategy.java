package com.delivery.strategy;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;

import java.util.List;

/**
 * Picks the route with the lowest REAL delivery time.
 *
 * It is not just min(estimatedMinutes): the time is
 *   estimated time + traffic penalty + weather penalty
 * and routes that are unavailable or miss the deadline are thrown out first
 * (done in AbstractRouteStrategy).
 */
public class FastestRouteStrategy extends AbstractRouteStrategy {

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.effectiveMinutes();
    }

    @Override
    protected List<String> explain(Route winner, DeliveryRequest request,
                                   List<Route> candidates, double score) {
        return List.of(
                "Estimated time: " + winner.estimatedMinutes() + " min",
                "Traffic penalty (" + winner.trafficLevel() + " traffic): +" + winner.trafficPenaltyMinutes() + " min",
                "Weather penalty: +" + winner.weatherPenaltyMinutes() + " min",
                "Final time: " + winner.effectiveMinutes() + " min"
        );
    }

    @Override
    public String getStrategyName() {
        return "Fastest";
    }
}
