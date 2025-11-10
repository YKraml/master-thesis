package teamscale.api.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MetricValueMapping extends MetricValue {

    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final double e;

    public MetricValueMapping(double a, double b, double c, double d, double e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    public double getD() {
        return d;
    }

    public double getE() {
        return e;
    }
}
