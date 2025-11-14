package logic;

import io.NormalisedResultReaderWriter;
import model.Proof;
import io.ResultReaderWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import model.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ResultNormaliser {

    private static final int SECONDS_IN_A_DAY = 60 * 60 * 24;
    private static final int DAYS_IN_TWO_YEARS = 2 * 365;

    private final ResultReaderWriter resultReaderWriter;
    private final NormalisedResultReaderWriter normalisedResultReaderWriter;
    private final RepositoryHandler repositoryHandler;
    private final ExecutorService executorService;
    private final State state;

    @Inject
    public ResultNormaliser(ResultReaderWriter resultReaderWriter, NormalisedResultReaderWriter normalisedResultReaderWriter, RepositoryHandler repositoryHandler, ExecutorService executorService, State state) {
        this.resultReaderWriter = resultReaderWriter;
        this.normalisedResultReaderWriter = normalisedResultReaderWriter;
        this.repositoryHandler = repositoryHandler;
        this.executorService = executorService;
        this.state = state;
    }

    public void normaliseResults() throws IOException {
        List<Result> results = resultReaderWriter.readAllResults();
        for (Result result : results) {
            executorService.submit(() -> {
                state.addToState(Pair.of(result.repoOwner(), result.repoName()), "Normalising results");
                try {
                    normaliseResult(result);
                } catch (GitAPIException | IOException e) {
                    throw new RuntimeException(e);
                }
                state.removeFromState(Pair.of(result.repoOwner(), result.repoName()));
            });
        }
    }

    private void normaliseResult(Result result) throws IOException, GitAPIException {
        Proof proof = repositoryHandler.handleRepo(result.repoOwner(), result.repoName()).get();
        TreeMap<Integer, Result.Entry> map = new TreeMap<>();
        for (Result.Entry entry : result.entries()) {
            map.put(entry.time(), entry);
        }

        for (Proof.LinterWithProof linter : proof.linters()) {
            Result normalise = normalise(map, linter, proof.repoOwner(), proof.repoName());
            normalisedResultReaderWriter.writeNormalisedResult(normalise, linter.linterName());
        }

    }

    private Result normalise(TreeMap<Integer, Result.Entry> map, Proof.LinterWithProof linter, String repoOwner, String repoName) {

        List<Result.Entry> entries = new ArrayList<>();
        for (Integer time : createTimes(linter)) {
            Map.Entry<Integer, Result.Entry> low = map.floorEntry(time);
            Map.Entry<Integer, Result.Entry> high = map.ceilingEntry(time);

            Result.Entry res = null;
            if (low != null && high != null) {
                res = Math.abs(time - low.getKey()) < Math.abs(time - high.getKey()) ? low.getValue() : high.getValue();
            } else if (low != null || high != null) {
                res = low != null ? low.getValue() : high.getValue();
            }
            entries.add(res);
        }

        return new Result(repoOwner, repoName, entries);
    }

    private static List<Integer> createTimes(Proof.LinterWithProof linter) {
        List<Integer> times = new ArrayList<>();
        int introductionTime = linter.introductionTime();

        for (int i = DAYS_IN_TWO_YEARS - 1; i >= 0; i--) {
            times.add(introductionTime - i * SECONDS_IN_A_DAY);
        }

        times.add(introductionTime);

        for (int i = 0; i < DAYS_IN_TWO_YEARS; i++) {
            times.add(introductionTime + i * SECONDS_IN_A_DAY);
        }
        return times;
    }

}
