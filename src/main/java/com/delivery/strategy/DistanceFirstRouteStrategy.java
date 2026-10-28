package com.delivery.strategy;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.util.Fmt;

import java.util.List;

/**
 * Fifth strategy (added after the first version worked): shortest distance wins.
 *
 * Useful for bike couriers or when the vehicle has a small range, where km matter
 * more than minutes or money. Hard constraints still apply, so a short route that is
 * closed, too expensive or too slow can't win.
 *
 * To add it we needed: this class + one line in StrategyConfig. RoutePlanner,
 * StrategySelector, StrategyComparator and the other strategies stayed untouched.
 */
public class DistanceFirstRouteStrategy extends AbstractRouteStrategy {

    @Override
    protected double score(Route route, DeliveryRequest request, List<Route> candidates) {
        return route.distanceKm();
    }

    @Override
    protected List<String> explain(Route winner, DeliveryRequest request,
                                   List<Route> candidates, double score) {
        return List.of(
                "Distance: " + Fmt.dec1(winner.distanceKm()) + " km (shortest allowed route)",
                "Final time on this route: " + winner.effectiveMinutes() + " min",
                "Total cost on this route: " + Fmt.money(winner.totalCost(request.priority()))
        );
    }

    @Override
    public String getStrategyName() {
        return "Distance-First";
    }
}
