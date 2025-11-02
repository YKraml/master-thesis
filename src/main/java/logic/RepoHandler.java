package logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Language;
import model.Linter;
import model.Result;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.GHRepository;
import util.State;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepoHandler {

    public static final int SIZE_1 = 1000 * 1000; // 1 GB
    public static final int SIZE_2 = 1000 * 1000 * 1000; // 1 GB

    public void handleRepo(GHRepository repo, RepositoryDownloader repositoryDownloader, LinterFinder linterFinder, LinterIntroductionFinder linterIntroductionFinder) throws Exception {
        String ownerName = repo.getOwnerName();
        String repoName = repo.getName();
        State.getInstance().addToState(Pair.of(ownerName, repoName), "Started handling");

        //Limit at around 0.5GB. The getSize() is not very accurate.
        if (repo.getSize() > SIZE_1) {
            return;
        }

        //Skip if already analysed
        File proofFile = new File("proofs" + File.separator + ownerName + "." + repoName + ".txt");
        if (proofFile.exists()) {
            return;
        }

        try (Git git = repositoryDownloader.getRepo(ownerName, repoName)) {
            File folder = git.getRepository().getDirectory().getParentFile();

            //Limit at 0.5GB. The getSize() is not very accurate.
            long size = FileUtils.sizeOfDirectory(folder);
            if (size > SIZE_2) {
                return;
            }

            State.getInstance().addToState(Pair.of(ownerName, repoName), "Searching for linter");
            List<LinterFinder.Result> linters = linterFinder.findLinters(folder);
            String currentCommit = git.getRepository().getBranch();

            List<Result.LinterWithProof> linterss = new ArrayList<>();
            for (LinterFinder.Result linter : linters) {
                State.getInstance().addToState(Pair.of(ownerName, repoName), "Searching introduction of: " + linter.linterName());
                Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroductionCommitWithProof(git, Linter.valueOf(linter.linterName()));
                linterss.add(new Result.LinterWithProof(linter.linterName(), linter.fileName(), currentCommit, introduction.getKey().getName(), introduction.getValue()));
            }
            Result result = new Result(ownerName, repoName, Language.JAVA, repo.getHtmlUrl().toString(), linterss);

            writeResult(proofFile, result);
        }
    }

    private void writeResult(File resultFile, Result result) throws IOException {
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(resultFile, result);
    }
}
