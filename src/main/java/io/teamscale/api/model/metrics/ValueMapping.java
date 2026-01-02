package io.teamscale.api.model.metrics;

public class ValueMapping extends Value {

    private final double red;
    private final double yellow;
    private final double green;

    public ValueMapping(double red, double yellow, double green) {
        this.red = red;
        this.yellow = yellow;
        this.green = green;
    }

    public double getRed() {
        return red;
    }

    public double getYellow() {
        return yellow;
    }

    public double getGreen() {
        return green;
    }
}
