package teamscale.api.model.metrics;

public class MetricValueDouble extends MetricValue {

    private double value;

    public MetricValueDouble(double value) {
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
