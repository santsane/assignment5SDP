package com.delivery.strategy;

import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;

import java.util.List;

/**
 * The Strategy abstraction. RoutePlanner (the Context) only knows this interface.
 *
 * Small improvement over the assignment template: decide() returns a RouteDecision
 * (route + score + explanation + rejected routes) because every strategy must be able
 * to explain its result. selectRoute() is kept as a shortcut that returns only the Route.
 */
public interface RouteStrategy {

    /**
     * Picks the best route.
     *
     * @throws com.delivery.exception.NoSuitableRouteException if nothing is usable
     * @throws IllegalArgumentException                        if the request is missing
     */
    RouteDecision decide(List<Route> routes, DeliveryRequest request);

    default Route selectRoute(List<Route> routes, DeliveryRequest request) {
        return decide(routes, request).selectedRoute();
    }

    String getStrategyName();
}
