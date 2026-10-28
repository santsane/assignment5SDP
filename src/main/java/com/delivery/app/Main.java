package com.delivery.app;

import com.delivery.exception.NoSuitableRouteException;
import com.delivery.model.DeliveryPriority;
import com.delivery.model.DeliveryRequest;
import com.delivery.model.Route;
import com.delivery.planner.RoutePlanner;
import com.delivery.planner.StrategyComparator;
import com.delivery.selector.StrategySelector;
import com.delivery.strategy.BalancedRouteStrategy;
import com.delivery.strategy.CheapestRouteStrategy;
import com.delivery.strategy.EcoFriendlyRouteStrategy;
import com.delivery.strategy.FastestRouteStrategy;

import java.util.List;
import java.util.Locale;

/**
 * Console demo of the whole assignment:
 *   1. candidate routes
 *   2. runtime strategy switching
 *   3. automatic strategy selection
 *   4. comparison mode
 *   5. failure scenarios
 */
public class Main {

    public static void main(String[] args) {
        List<Route> routes = DemoData.routes();

        printRoutes(routes);
        runtimeSwitching(routes);
        automaticSelection(routes);
        comparisonMode(routes);
        failureScenarios(routes);
    }

    // ---------------------------------------------------------------- 1
    private static void printRoutes(List<Route> routes) {
        title("CANDIDATE ROUTES");
        System.out.printf(Locale.US, "%-4s %6s %6s %8s %7s %8s %8s %6s %s%n",
                "ID", "km", "est.", "final", "CO2", "base", "fees", "traffic", "available");
        for (Route r : routes) {
            System.out.printf(Locale.US, "%-4s %6.1f %6d %8d %7.1f %8.0f %8.0f %6s %s%n",
                    r.id(), r.distanceKm(), r.estimatedMinutes(), r.effectiveMinutes(),
                    r.carbonEmission(), r.cost(), r.additionalFees(), r.trafficLevel(), r.available());
        }
        System.out.println("(est. = estimated minutes, final = with traffic and weather penalty)");
    }

    // ---------------------------------------------------------------- 2
    private static void runtimeSwitching(List<Route> routes) {
        title("RUNTIME STRATEGY SWITCHING (one RoutePlanner object, 4 switches)");

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        show(planner, routes, DemoData.requestA());

        planner.setStrategy(new CheapestRouteStrategy());   // switch 1
        show(planner, routes, DemoData.requestB());

        planner.setStrategy(new EcoFriendlyRouteStrategy()); // switch 2
        show(planner, routes, DemoData.requestC());

        planner.setStrategy(new BalancedRouteStrategy(0.50, 0.20, 0.10, 0.20)); // switch 3
        show(planner, routes, DemoData.requestD());

        planner.setStrategy(new FastestRouteStrategy());    // switch 4: back to the first one
        show(planner, routes, DemoData.requestD());
    }

    private static void show(RoutePlanner planner, List<Route> routes, DeliveryRequest request) {
        System.out.println("Request " + request.requestId() + " with " + planner.getStrategy().getStrategyName());
        System.out.println(planner.plan(routes, request).explain());
    }

    // ---------------------------------------------------------------- 3
    private static void automaticSelection(List<Route> routes) {
        title("AUTOMATIC STRATEGY SELECTION");

        StrategySelector selector = StrategyConfig.createSelector();
        RoutePlanner planner = new RoutePlanner(new BalancedRouteStrategy());

        List<DeliveryRequest> requests = List.of(
                DemoData.requestA(), DemoData.requestB(), DemoData.requestC(), DemoData.requestD());
        for (DeliveryRequest request : requests) {
            System.out.println("Request " + request.requestId() + ": " + selector.explainChoice(request));
            planner.setStrategy(selector.select(request));
            System.out.println(planner.plan(routes, request).explain());
        }
    }

    // ---------------------------------------------------------------- 4
    private static void comparisonMode(List<Route> routes) {
        title("STRATEGY COMPARISON MODE");
        StrategyComparator comparator = new StrategyComparator(StrategyConfig.allStrategies());
        System.out.println(comparator.compare(routes, DemoData.requestD()).format());
    }

    // ---------------------------------------------------------------- 5
    private static void failureScenarios(List<Route> routes) {
        title("FAILURE SCENARIOS");
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        attempt("1. empty route list",
                () -> planner.plan(List.of(), DemoData.requestA()));

        attempt("2. budget too small for every route",
                () -> planner.plan(routes, new DeliveryRequest("X1", DeliveryPriority.STANDARD, 500, 90, false)));

        attempt("3. deadline too tight for every route",
                () -> planner.plan(routes, new DeliveryRequest("X2", DeliveryPriority.URGENT, 10000, 20, false)));

        attempt("4. invalid Balanced weights (sum is 1.5)",
                () -> new BalancedRouteStrategy(0.5, 0.5, 0.3, 0.2));

        attempt("5. negative route cost",
                () -> new Route("BAD", DemoData.WAREHOUSE, DemoData.CUSTOMER, 10, 20, -100, 1.0));

        attempt("6. missing request data",
                () -> planner.plan(routes, null));

        attempt("7. request without priority",
                () -> new DeliveryRequest("X3", null, 1000, 60, false));
    }

    private static void attempt(String title, Runnable action) {
        System.out.println(title);
        try {
            action.run();
            System.out.println("  (no error)");
        } catch (NoSuitableRouteException e) {
            System.out.println("  NoSuitableRouteException: " + e.getMessage());
            e.getRejectedRoutes().forEach((id, why) -> System.out.println("    " + id + ": " + why));
        } catch (IllegalArgumentException e) {
            System.out.println("  IllegalArgumentException: " + e.getMessage());
        }
        System.out.println();
    }

    private static void title(String text) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(text);
        System.out.println("==================================================");
    }
}
