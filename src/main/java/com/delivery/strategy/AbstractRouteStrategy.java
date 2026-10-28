package com.delivery.strategy;

import com.delivery.constraint.RouteConstraint;
import com.delivery.constraint.RouteConstraints;
import com.delivery.exception.NoSuitableRouteException;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.model.RouteDecision;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared skeleton for the concrete strategies (Template Method).
 *
 * The steps that are the same for every algorithm live here:
 *   1. validate input
 *   2. throw away routes that break a hard constraint
 *   3. score the rest and take the lowest score
 *   4. build the RouteDecision
 *
 * The only things a concrete strategy has to write are its score and its explanation.
 * Lower score = better.
 */
public abstract class AbstractRouteStrategy implements RouteStrategy {

    private final List<RouteConstraint> constraints;

    protected AbstractRouteStrategy() {
        this(RouteConstraints.defaults());
    }

    protected AbstractRouteStrategy(List<RouteConstraint> constraints) {
        this.constraints = List.copyOf(constraints);
    }

    @Override
    public final RouteDecision decide(List<Route> routes, DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request is missing");
        }
        if (routes == null || routes.isEmpty()) {
            throw new NoSuitableRouteException("No candidate routes were provided");
        }

        // step 2: hard constraints
        List<Route> eligible = new ArrayList<>();
        Map<String, String> rejected = new LinkedHashMap<>();
        for (Route route : routes) {
            String problems = findProblems(route, request);
            if (problems.isEmpty()) {
                eligible.add(route);
            } else {
                rejected.put(route.id(), problems);
            }
        }
        if (eligible.isEmpty()) {
            throw new NoSuitableRouteException("No route satisfies the constraints ("
                    + rejected.size() + " of " + routes.size() + " routes rejected)", rejected);
        }

        // step 3: lowest score wins, equal scores -> smaller route id (so the result is always the same)
        Route best = null;
        double bestScore = 0;
        for (Route route : eligible) {
            double score = score(route, request, eligible);
            if (best == null || score < bestScore
                    || (score == bestScore && route.id().compareTo(best.id()) < 0)) {
                best = route;
                bestScore = score;
            }
        }

        // step 4
        List<String> reasons = explain(best, request, eligible, bestScore);
        return new RouteDecision(getStrategyName(), best, bestScore, reasons, rejected);
    }

    /** Collects every broken constraint of the route into one text. Empty text = route is fine. */
    private String findProblems(Route route, DeliveryRequest request) {
        List<String> problems = new ArrayList<>();
        for (RouteConstraint constraint : constraints) {
            Optional<String> problem = constraint.check(route, request);
            problem.ifPresent(problems::add);
        }
        return String.join(" ", problems);
    }

    /**
     * Score of one route. Lower is better.
     *
     * @param candidates all routes that passed the constraints
     *                   (needed when the score depends on the other routes, e.g. normalization)
     */
    protected abstract double score(Route route, DeliveryRequest request, List<Route> candidates);

    /** Lines that explain why the winning route was chosen. */
    protected abstract List<String> explain(Route winner, DeliveryRequest request,
                                            List<Route> candidates, double score);
}
