package com.walter;
public class CoordinateConverter {

    public static class Cartesian {
        public double x;
        public double y;

        public Cartesian(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("Cartesian(x=%.4f, y=%.4f)", x, y);
        }
    }

    public static class Polar {
        public double r;
        public double theta; // En radians

        public Polar(double r, double theta) {
            this.r = r;
            this.theta = theta;
        }

        @Override
        public String toString() {
            return String.format("Polar(r=%.4f, θ=%.4f rad)", r, theta);
        }
    }

    /**
     * Convertit des coordonnées polaires (r, θ) en cartésiennes (x, y)
     * θ est en radians.
     */
    public static Cartesian polarToCartesian(Polar polar) {
        double x = polar.r * Math.cos(polar.theta);
        double y = polar.r * Math.sin(polar.theta);
        return new Cartesian(x, y);
    }

    /**
     * Convertit des coordonnées cartésiennes (x, y) en polaires (r, θ)
     * Le résultat θ est en radians.
     */
    public static Polar cartesianToPolar(Cartesian cartesian) {
        double r = Math.sqrt(cartesian.x * cartesian.x + cartesian.y * cartesian.y);
        double theta = Math.atan2(cartesian.y, cartesian.x); // Gère tous les quadrants
        return new Polar(r, theta);
    }
}
