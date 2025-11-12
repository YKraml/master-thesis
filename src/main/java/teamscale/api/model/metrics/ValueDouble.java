package teamscale.api.model.metrics;

public class ValueDouble extends Value {

    private double value;

    public ValueDouble(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "MetricValueDouble{" +
                "value=" + value +
                '}';
    }
}
