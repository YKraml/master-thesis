import logic.LinterFileFinder;
import logic.LinterFolderFinder;
import logic.LinterIntroductionFinder;
import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class LinterIntroductionFinderTest {

    public static final String BIN_SEARCH_PATH = "src" + File.separator + "test" + File.separator + "java" + File.separator + "data" + File.separator + "binsearch" + File.separator;
    public static final String repo1Folder = BIN_SEARCH_PATH + "repo1"; // 0
    public static final String repo2Folder = BIN_SEARCH_PATH + "repo2"; // 16
    public static final String repo3Folder = BIN_SEARCH_PATH + "repo3"; // 54
    public static final String repo4Folder = BIN_SEARCH_PATH + "repo4"; // 78
    public static final String repo5Folder = BIN_SEARCH_PATH + "repo5"; // 99

    private final LinterFileFinder linterFileFinder = new LinterFileFinder();
    private final LinterFolderFinder linterFolderFinder = new LinterFolderFinder(linterFileFinder);
    private final LinterIntroductionFinder linterIntroductionFinder = new LinterIntroductionFinder(linterFolderFinder);

    @Disabled
    @Test
    void createTestData() throws GitAPIException, IOException {

        String folder = "";
        File gitFile = new File(folder + File.separator + ".git");
        Repository repo = new FileRepositoryBuilder().setGitDir(gitFile).build();
        Git git = Git.wrap(repo);

        int index = 99;
        for (int i = 0; i < index; i++) {
            new File(folder + File.separator + i).createNewFile();
            git.add().addFilepattern(".").call();
            git.commit().setMessage(String.valueOf(i)).call();
        }

        new File(folder + File.separator + "checkstyle.xml").createNewFile();
        git.add().addFilepattern(".").call();
        git.commit().setMessage("Added ckeckstyle").call();

        for (int i = index + 1; i < 100; i++) {
            new File(folder + File.separator + i).createNewFile();
            git.add().addFilepattern(".").call();
            git.commit().setMessage(String.valueOf(i)).call();
        }

    }


    @Test
    void testRepo1() throws IOException, GitAPIException {
        testLinterIntroduction(repo1Folder, "0dc81605449873fa629fd880ebc7133350fa7b29");
    }

    @Test
    void testRepo2() throws IOException, GitAPIException {
        testLinterIntroduction(repo2Folder, "901f7eac6762ce03a31129e4ff8c08dcf7482113");
    }

    @Test
    void testRepo3() throws IOException, GitAPIException {
        testLinterIntroduction(repo3Folder, "fcf39ea9db82016efa8a535d18e923606de598ce");
    }

    @Test
    void testRepo4() throws IOException, GitAPIException {
        testLinterIntroduction(repo4Folder, "b00efd6250fe411f753b697c338372525206acbf");
    }

    @Test
    void testRepo5() throws IOException, GitAPIException {
        testLinterIntroduction(repo5Folder, "dd2731038fa7e09cc57874facf067a5e44cd9d78");
    }

    private void testLinterIntroduction(String repoFolder, String commit) throws IOException, GitAPIException {
        File gitFile = new File(repoFolder + File.separator + ".git");
        Repository repo = new FileRepositoryBuilder().setGitDir(gitFile).build();
        Git git = Git.wrap(repo);
        git.checkout().setName("master").call();

        Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroduction(git, Linter.CHECKSTYLE);
        Assertions.assertEquals(repoFolder + File.separator + "checkstyle.xml", introduction.getRight());
        Assertions.assertEquals(commit, introduction.getLeft().getName());
    }
}
