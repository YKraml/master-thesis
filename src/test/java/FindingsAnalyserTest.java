import com.google.inject.Guice;
import com.google.inject.Injector;
import common.GuiceModule;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import teamscale.FindingsAnalyser;
import teamscale.model.churn.Result;

import java.io.IOException;

@Execution(ExecutionMode.CONCURRENT)
class FindingsAnalyserTest {

    private FindingsAnalyser findingsAnalyser;

    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector(new GuiceModule());
        findingsAnalyser = injector.getInstance(FindingsAnalyser.class);
    }

    @Test
    void testAirbnbDeepLinkDispatch() throws GitAPIException, IOException {
        Result result = findingsAnalyser.analyseFindings("airbnb", "DeepLinkDispatch");
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.getEntries().getFirst(), 0, 0, 0, 0),
                () -> assertFindingsAtEntry(result.getEntries().getLast(), 6, 4, 6, 15)
        );
    }

    @Test
    void testAlexvasilkovGestureViews() throws GitAPIException, IOException {
        Result result = findingsAnalyser.analyseFindings("alexvasilkov", "GestureViews");
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.getEntries().getFirst(), 0, 0, 3, 0),
                () -> assertFindingsAtEntry(result.getEntries().getLast(), 6, 4, 18, 5)
        );
    }

    @Test
    void testAeronIoSimpleBinaryEncoding() throws GitAPIException, IOException {
        Result result = findingsAnalyser.analyseFindings("aeron-io", "simple-binary-encoding");
        Assertions.assertAll(
                () -> assertFindingsAtEntry(result.getEntries().getFirst(), 0, 0, 0, 0),
                () -> assertFindingsAtEntry(result.getEntries().getLast(), 480, 31, 287, 38)
        );
    }

    private static void assertFindingsAtEntry(Result.Entry entry, int numberOfCodeClones, int numberOfFileSizeViolations, int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(numberOfCodeClones, entry.numberOfCodeClones()),
                () -> Assertions.assertEquals(numberOfFileSizeViolations, entry.numberOfFileSizeViolations()),
                () -> Assertions.assertEquals(numberOfMethodLengthViolations, entry.numberOfMethodLengthViolations()),
                () -> Assertions.assertEquals(numberOfNestingDepthViolations, entry.numberOfNestingDepthViolations())
        );
    }

}