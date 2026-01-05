package fr.insa.projetIntegrateur.RoutingService.model;

import java.util.Objects;

public class VectorCost {
    // The 4 components of your vector (e.g., Time, Distance, Risk, Discomfort)
    public final double c1;
    public final double c2;
    public final double c3;
    public final double c4;

    public VectorCost(double c1, double c2, double c3, double c4) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }

    // "Updated the same way as a scalar" -> Component-wise addition
    public VectorCost add(VectorCost other) {
        return new VectorCost(
            this.c1 + other.c1,
            this.c2 + other.c2,
            this.c3 + other.c3,
            this.c4 + other.c4
        );
    }

    // The "Pietro" (Pareto) Principle Logic
    // Returns true if THIS vector dominates OTHER vector (Minimization assumed)
    public boolean dominates(VectorCost other) {
        boolean betterInAtLeastOne = false;

        if (this.c1 > other.c1) return false;
        if (this.c2 > other.c2) return false;
        if (this.c3 > other.c3) return false;
        if (this.c4 > other.c4) return false;

        if (this.c1 < other.c1) betterInAtLeastOne = true;
        if (this.c2 < other.c2) betterInAtLeastOne = true;
        if (this.c3 < other.c3) betterInAtLeastOne = true;
        if (this.c4 < other.c4) betterInAtLeastOne = true;

        return betterInAtLeastOne;
    }
    
    public static VectorCost zero() {
        return new VectorCost(0,0,0,0);
    }
    
    public static VectorCost infinity() {
        return new VectorCost(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    @Override
    public String toString() {
        return String.format("[%.2f, %.2f, %.2f, %.2f]", c1, c2, c3, c4);
    }
}
