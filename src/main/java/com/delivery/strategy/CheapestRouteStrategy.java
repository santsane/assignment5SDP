package com.delivery.strategy;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.util.Fmt;

import java.util.List;

/**
 * Picks the route with the lowest total delivery cost.
 *
 * Formula (implemented in Route.totalCost):
 *   total cost = base cost
 *              + distance cost (distanceKm x 20 per km)
 *              + priority surcharge (URGENT = 1500, others = 0)
 *              + additional fees (tolls etc.)
 *
 * The same total is used for the budget check, so "cheapest" and "within budget"
 * always talk about the same number.
 */
public class CheapestRouteStrategy extends AbstractRouteStrategy {

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.totalCost(request.priority());
    }

    @Override
    protected List<String> explain(Route winner, DeliveryRequest request,
                                   List<Route> candidates, double score) {
        return List.of(
                "Base cost: " + Fmt.money(winner.cost()),
                "Distance cost: " + Fmt.dec1(winner.distanceKm()) + " km x "
                        + Fmt.dec1(Route.DISTANCE_RATE_PER_KM) + " = " + Fmt.money(winner.distanceCost()),
                "Priority surcharge (" + request.priority() + "): " + Fmt.money(request.priority().surcharge()),
                "Additional fees: " + Fmt.money(winner.additionalFees()),
                "Total cost: " + Fmt.money(score)
        );
    }

    @Override
    public String getStrategyName() {
        return "Cheapest";
    }
}
