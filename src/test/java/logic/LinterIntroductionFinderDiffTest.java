package logic;

import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.io.IOException;

@Execution(ExecutionMode.CONCURRENT)
class LinterIntroductionFinderDiffTest {

    private final GitHub github = new GitHubBuilder().withOAuthToken(Main.TOKEN).build();
    private final RepositoryDownloader repositoryDownloader = new RepositoryDownloader(github);
    private final LinterFileFinder linterFileFinder = new LinterFileFinder();
    private final LinterFolderFinder linterFolderFinder = new LinterFolderFinder(linterFileFinder);
    private final LinterIntroductionFinderDiff linterIntroductionFinderDiff = new LinterIntroductionFinderDiff(linterFolderFinder);

    private LinterIntroductionFinderDiffTest() throws IOException {
    }

    @Test
    void testHaloDevHalo() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("halo-dev", "halo");
        asserNumberOfLinters(git, 1);
        assertLinterPresent(git, Linter.CHECKSTYLE, "e8db12a93dbab4e49bbff1ed136cc25357d4504d", "config" + File.separator + "checkstyle" + File.separator + "checkstyle.xml");
    }

    @Test
    void testTheAlgorithmsJava() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("TheAlgorithms", "Java");
        asserNumberOfLinters(git, 3);
        assertLinterPresent(git, Linter.CHECKSTYLE, "7a42f68b66dfc7a53d67125f9685a2db0a5e2dd9", "checkstyle.xml");
        assertLinterPresent(git, Linter.SPOTBUGS, "4bb64559deb6722069678aaa910f7379645d5a7b", "pom.xml");
        assertLinterPresent(git, Linter.PMD, "cb401fed69258f3ccc0484ccd9f588c10af0dd32", "pom.xml");
    }


    @Test
    void testNettyNetty() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("netty", "netty");
        asserNumberOfLinters(git, 1);
        assertLinterPresent(git, Linter.CHECKSTYLE, "ebfc4513e0ba619592af3ba946bd1974d1becb55", "build" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "io" + File.separator + "netty" + File.separator + "checkstyle.xml");
    }

    @Test
    void testApacheIncubatorSeata() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "incubator-seata");
        asserNumberOfLinters(git, 1);
        assertLinterPresent(git, Linter.PMD, "dbc295fc6518a988f62af3730e27fb3615938ddd", "pom.xml");
    }

    @Test
    void testRedissonRedisson() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("redisson", "redisson");
        asserNumberOfLinters(git, 3);
        assertLinterPresent(git, Linter.CHECKSTYLE, "d18b8c2133662b48466df47f551da507517727ee", "checkstyle.xml");
        assertLinterPresent(git, Linter.PMD, "173000394121d1f0e8a97fd4c185d4449e2bcbad", "pom.xml");
        assertLinterPresent(git, Linter.SPOTBUGS, "fbb0a9cbf86dc908187f60e090bf62acc422f5ff", "pom.xml");
    }

    @Test
    void testApacheRocketmq() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "rocketmq");
        asserNumberOfLinters(git, 2);
        assertLinterPresent(git, Linter.CHECKSTYLE, "ec5a9c9bf611631ba62f38309a7ffdabaf8f6d89", "checkstyle"+File.separator+"checkstyle.xml");
        assertLinterPresent(git, Linter.SPOTBUGS, "f23fdbe016ab4b60c2dd9786016c5f4756b3db54", "pom.xml");
    }

    private void assertLinterPresent(Git git, Linter linter, String commitName, String fileName) throws IOException, GitAPIException {
        Pair<RevCommit, String> introduction = linterIntroductionFinderDiff.findIntroduction(git, linter);
        Assertions.assertEquals(commitName, introduction.getLeft().getName());
        Assertions.assertTrue(introduction.getRight().endsWith(fileName));
    }

    private void asserNumberOfLinters(Git git, int expected) {
        Assertions.assertEquals(expected, linterFolderFinder.findLinters(git.getRepository().getDirectory().getParentFile()).size());
    }

}