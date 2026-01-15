import com.google.inject.Guice;
import com.google.inject.Injector;
import io.teamscale.TeamscaleDataDownloader;
import io.teamscale.api.model.cleaned.TeamscaleData;
import logic.DataCombiner;
import model.CombinedData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import util.GuiceModule;

@Execution(ExecutionMode.CONCURRENT)
@Disabled
class DataCombinerTest {

    private TeamscaleDataDownloader teamscaleDataDownloader;
    private DataCombiner dataCombiner;

    @BeforeEach
    public void setup() {
        Injector injector = Guice.createInjector(new GuiceModule());
        dataCombiner = injector.getInstance(DataCombiner.class);
        teamscaleDataDownloader = injector.getInstance(TeamscaleDataDownloader.class);
    }

    @Test
    void testAirbnbDeepLinkDispatch() throws Exception {
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom("airbnb", "DeepLinkDispatch");
        CombinedData result = dataCombiner.combineData(null, teamscaleData);
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 0, 0, 0, 0),
                () -> assertFindingsAtEntry(result.entries().getLast(), 4, 3, 12, 15),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 3, 67, 41),
                () -> assertMetricsAtEntry(result.entries().getLast(), 36, 4126, 3179)
        );
    }

    @Test
    void testApacheMetron() throws Exception {
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom("apache", "metron");
        CombinedData result = dataCombiner.combineData(null, teamscaleData);
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 66, 5, 54, 76),
                () -> assertFindingsAtEntry(result.entries().getLast(), 852, 42, 266, 363),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 150, 19892, 11945),
                () -> assertMetricsAtEntry(result.entries().getLast(), 1053, 149933, 97244)
        );
    }

    @Test
    @Disabled("Takes too long")
    void testTraccerTraccer() throws Exception {
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom("traccar", "traccar");
        CombinedData result = dataCombiner.combineData(null, teamscaleData);
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 290, 1, 67, 12),
                () -> assertFindingsAtEntry(result.entries().getLast(), 216, 12, 247, 162),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 147, 15855, 10264),
                () -> assertMetricsAtEntry(result.entries().getLast(), 789, 68087, 46056)
        );
    }

    @Test
    void testGooglePhysicalWeb() throws Exception {
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom("google", "physical-web");
        CombinedData result = dataCombiner.combineData(null, teamscaleData);
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 2, 1, 2, 5),
                () -> assertFindingsAtEntry(result.entries().getLast(), 21, 3, 20, 18),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 12, 2633, 1509),
                () -> assertMetricsAtEntry(result.entries().getLast(), 63, 11524, 7217)
        );
    }

    private static void assertMetricsAtEntry(CombinedData.Entry entry, int numberOfFiles, int numberOfLinesOfCode, int numberOfSourceLinesOfCode) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(numberOfFiles, entry.files()),
                () -> Assertions.assertEquals(numberOfLinesOfCode, entry.linesOfCode()),
                () -> Assertions.assertEquals(numberOfSourceLinesOfCode, entry.sourceLinesOfCode())
        );
    }

    private static void assertFindingsAtEntry(CombinedData.Entry entry, int numberOfCodeClones, int numberOfFileSizeViolations, int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(numberOfCodeClones, entry.redundancy()),
                () -> Assertions.assertEquals(numberOfFileSizeViolations, entry.longFiles()),
                () -> Assertions.assertEquals(numberOfMethodLengthViolations, entry.longMethods()),
                () -> Assertions.assertEquals(numberOfNestingDepthViolations, entry.deepNesting())
        );
    }
}