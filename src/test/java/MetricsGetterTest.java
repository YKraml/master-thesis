import com.google.inject.Guice;
import com.google.inject.Injector;
import io.teamscale.api.MetricsFetcher;
import io.teamscale.api.model.metrics.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import util.GuiceModule;

import java.util.List;

@Disabled
public class MetricsGetterTest {

    private MetricsFetcher metricsFetcher;

    @BeforeEach
    public void setup() {
        Injector injector = Guice.createInjector(new GuiceModule());
        metricsFetcher = injector.getInstance(MetricsFetcher.class);
    }

    @Test
    void testAndytillErlyberly() {
        assertMetricsSize("andytill", "erlyberly", 206);
    }

    @Test
    void testApacheHop() {
        assertMetricsSize("apache", "hop", 919);
    }

    @Test
    void testOpenJdkJmh() {
        assertMetricsSize("openjdk", "jmh", 928);
    }

    private void assertMetricsSize(String repoOwner, String repoName, int expected) {
        List<Metrics> metrics = metricsFetcher.getMetrics(repoOwner, repoName, 1L, Long.MAX_VALUE);
        Assertions.assertEquals(expected, metrics.size());
    }
}
