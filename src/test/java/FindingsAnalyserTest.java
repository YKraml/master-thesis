import com.google.inject.Guice;
import com.google.inject.Injector;
import util.GuiceModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import logic.FindingsAnalyser;
import model.Result;

@Execution(ExecutionMode.CONCURRENT)
class FindingsAnalyserTest {

    private static final FindingsAnalyser FINDINGS_ANALYSER;

    static {
        Injector injector = Guice.createInjector(new GuiceModule());
        FINDINGS_ANALYSER = injector.getInstance(FindingsAnalyser.class);
    }

    @Test
    void testAirbnbDeepLinkDispatch() {
        Result result = FINDINGS_ANALYSER.analyseFindings("airbnb", "DeepLinkDispatch");
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 0, 0, 0, 0),
                () -> assertFindingsAtEntry(result.entries().getLast(), 4, 3, 12, 15),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 3, 67, 41),
                () -> assertMetricsAtEntry(result.entries().getLast(), 36, 4126, 3179)
        );
    }

    @Test
    void testApacheMetron() {
        Result result = FINDINGS_ANALYSER.analyseFindings("apache", "metron");
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 66, 5, 54, 76),
                () -> assertFindingsAtEntry(result.entries().getLast(), 852, 42, 266, 363),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 150, 19892, 11945),
                () -> assertMetricsAtEntry(result.entries().getLast(), 1053, 149933, 97244)
        );
    }

    @Test
    void testTraccerTraccer() {
        Result result = FINDINGS_ANALYSER.analyseFindings("traccar", "traccar");
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 290, 1, 67, 12),
                () -> assertFindingsAtEntry(result.entries().getLast(), 216, 12, 247, 162),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 147, 15855, 10264),
                () -> assertMetricsAtEntry(result.entries().getLast(), 789, 68087, 46056)
        );
    }

    @Test
    void testGooglePhysicalWeb() {
        Result result = FINDINGS_ANALYSER.analyseFindings("google", "physical-web");
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.entries().getFirst(), 2, 1, 2, 5),
                () -> assertFindingsAtEntry(result.entries().getLast(), 21, 3, 20, 18),
                () -> assertMetricsAtEntry(result.entries().getFirst(), 12, 2633, 1509),
                () -> assertMetricsAtEntry(result.entries().getLast(), 63, 11524, 7217)
        );
    }

    private static void assertMetricsAtEntry(Result.Entry entry, int numberOfFiles, int numberOfLinesOfCode, int numberOfSourceLinesOfCode) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(numberOfFiles, entry.totalNumberOfFiles()),
                () -> Assertions.assertEquals(numberOfLinesOfCode, entry.totalNumberOfLinesOfCode()),
                () -> Assertions.assertEquals(numberOfSourceLinesOfCode, entry.totalNumberOfSourceLinesOfCode())
        );
    }

    private static void assertFindingsAtEntry(Result.Entry entry, int numberOfCodeClones, int numberOfFileSizeViolations, int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(numberOfCodeClones, entry.totalNumberOfCodeClones()),
                () -> Assertions.assertEquals(numberOfFileSizeViolations, entry.totalNumberOfFileSizeViolations()),
                () -> Assertions.assertEquals(numberOfMethodLengthViolations, entry.totalNumberOfMethodLengthViolations()),
                () -> Assertions.assertEquals(numberOfNestingDepthViolations, entry.totalNumberOfNestingDepthViolations())
        );
    }

}