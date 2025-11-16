package api.model.metrics;

public class ValueMapping extends Value {

    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final double e;

    public ValueMapping(double a, double b, double c, double d, double e) {
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

    public double getYellow() {
        return c;
    }

    public double getGreen() {
        return d;
    }

    public double getE() {
        return e;
    }
}
