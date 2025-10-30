package logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Language;
import model.Linter;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.GHRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RepoHandler {

    public static final int SIZE_1 = 1000 * 1000; // 1 GB
    public static final int SIZE_2 = 1000 * 1000 * 1000; // 1 GB

    public void handleRepo(GHRepository repo, RepositoryDownloader repositoryDownloader, LinterFolderFinder linterFolderFinder, LinterIntroductionFinder linterIntroductionFinder) throws Exception {
        String ownerName = repo.getOwnerName();
        String repoName = repo.getName();

        Printer.getInstance().printStartAnalysing(ownerName, repoName);

        //Limit at around 0.5GB. The getSize() is not very accurate.
        if (repo.getSize() > SIZE_1) {
            Printer.getInstance().printRepoTooLarge(repo, ownerName, repoName);
            return;
        }

        //Skip if already analysed
        File proofFile = new File("proofs" + File.separator + ownerName + "." + repoName + ".txt");
        if (proofFile.exists()) {
            Printer.getInstance().printAlreadyAnalysed(ownerName, repoName);
            return;
        }

        try (Git git = repositoryDownloader.getRepo(ownerName, repoName)) {

            File folder = git.getRepository().getDirectory().getParentFile();
            long size = Files.walk(folder.toPath())
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();

            //Limit at 0.5GB. The getSize() is not very accurate.
            if (size > SIZE_2) {
                Printer.getInstance().printFolderTooLarge(ownerName, repoName, size);
                return;
            }

            List<LinterFileFinder.Result> linters = linterFolderFinder.findLinters(folder);
            String currentCommit = git.getRepository().getBranch();

            List<Result.LinterWithProof> linterss = new ArrayList<>();
            for (LinterFileFinder.Result linter : linters) {
                Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroduction(git, Linter.valueOf(linter.linterName()));
                linterss.add(new Result.LinterWithProof(linter.linterName(), linter.fileName(), currentCommit, introduction.getKey().getName(), introduction.getValue()));
            }
            Result result = new Result(ownerName, repoName, Language.JAVA, repo.getHtmlUrl().toString(), linterss);

            writeResult(proofFile, result);

        }
    }

    private void writeResult(File resultFile, Result result) throws IOException {

        Printer.getInstance().printFinishedAnalysing(result);

        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(resultFile, result);
    }

}
