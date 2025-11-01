package logic;

import main.Main;
import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
    private final LinterFinder linterFinder = new LinterFinder();
    private final LinterIntroductionFinderDiff linterIntroductionFinderDiff = new LinterIntroductionFinderDiff(linterFinder);

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
        assertLinterPresent(git, Linter.CHECKSTYLE, "ebfc4513e0ba619592af3ba946bd1974d1becb55", "build" + File.separator + "pom.xml");
    }

    @Test
    void testApacheIncubatorSeata() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "incubator-seata");
        asserNumberOfLinters(git, 2);
        assertLinterPresent(git, Linter.PMD, "dbc295fc6518a988f62af3730e27fb3615938ddd", "pom.xml");
        assertLinterPresent(git, Linter.CHECKSTYLE, "85192cc816e71a852e0123333ed964f0c6c6f3b4", "style" + File.separator + "feats_checkstyle.xml");
    }

    @Test
    void testRedissonRedisson() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("redisson", "redisson");
        asserNumberOfLinters(git, 3);
        assertLinterPresent(git, Linter.CHECKSTYLE, "173000394121d1f0e8a97fd4c185d4449e2bcbad", "pom.xml");
        assertLinterPresent(git, Linter.PMD, "173000394121d1f0e8a97fd4c185d4449e2bcbad", "pom.xml");
        assertLinterPresent(git, Linter.SPOTBUGS, "fbb0a9cbf86dc908187f60e090bf62acc422f5ff", "pom.xml");
    }

    @Test
    void testApacheRocketmq() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "rocketmq");
        asserNumberOfLinters(git, 2);
        assertLinterPresent(git, Linter.CHECKSTYLE, "ec5a9c9bf611631ba62f38309a7ffdabaf8f6d89", "checkstyle" + File.separator + "checkstyle.xml");
        assertLinterPresent(git, Linter.SPOTBUGS, "f23fdbe016ab4b60c2dd9786016c5f4756b3db54", "pom.xml");
    }

    @Test
    void testBumptechGlide() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("bumptech", "glide");
        asserNumberOfLinters(git, 2);
        assertLinterPresent(git, Linter.CHECKSTYLE, "fe090f50f3040f4d478143a3e0ffa8cdf813fefc", "build.gradle");
        assertLinterPresent(git, Linter.PMD, "42086c3cc9c9303dd7d219cd43da7426683d3a26", "library" + File.separator + "build.gradle");
    }

    @Test
    void testApacheKafka() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "kafka");
        asserNumberOfLinters(git, 2);
        assertLinterPresent(git, Linter.CHECKSTYLE, "1c6d5bbac672cbf49591aed0889510b10e3285fa", "build.gradle");
        assertLinterPresent(git, Linter.SPOTBUGS, "f123d2f18c55b1cf2edb452aeb87e6ad0743c292", "build.gradle");
    }

    @Test
    void testTencentTinker() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("Tencent", "tinker");
        asserNumberOfLinters(git, 1);
        assertLinterPresent(git, Linter.CHECKSTYLE, "ddd547f864903f1689091a3b3ac53f87198cfdc9", "build.gradle");
    }


    @Test
    void testNetflixZuul() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("Netflix", "zuul");
        asserNumberOfLinters(git, 1);
        assertLinterPresent(git, Linter.CHECKSTYLE, "697fd66aae9beed107e13f49a741455f1d9d8dd9", "codequality" + File.separator + "checkstyle.xml");
    }


    @Test
    void testAlibabaNacos() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("alibaba", "nacos");
        asserNumberOfLinters(git, 3);
        assertLinterPresent(git, Linter.CHECKSTYLE, "7c4cca4360284344d9194c0d0f76edca02a5ba9c", "style" + File.separator + "NacosCheckStyle.xml");
        assertLinterPresent(git, Linter.SPOTBUGS, "b16b90048a23225cd72bd7df8223c413daf27d78", "address" + File.separator + "pom.xml");
        assertLinterPresent(git, Linter.PMD, "91d20f34fdec6b77b0b27c874983ca57a2feb584", "pom.xml");
    }

    @Test
    void testApacheShardingsphere() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "shardingsphere");
        asserNumberOfLinters(git, 3);
        assertLinterPresent(git, Linter.SPOTBUGS, "0f4fc284826b3ae20440b4958385901c5091f105", "pom.xml");
        assertLinterPresent(git, Linter.PMD, "5fd3ee9c4fec5cbd672593606de0d3db93b15241", "pom.xml");
        assertLinterPresent(git, Linter.CHECKSTYLE, "5fd3ee9c4fec5cbd672593606de0d3db93b15241", "pom.xml");
    }

    @Test
    void testPinpointapmPinpoint() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("pinpoint-apm", "pinpoint");
        asserNumberOfLinters(git, 3);
        assertLinterPresent(git, Linter.CHECKSTYLE, "6f9b181081e4b570ba3219bae657ac8675609cf5", "checkstyle-UTF8(ver0.6).xml");
        assertLinterPresent(git, Linter.SPOTBUGS, "25362e69bbf21fbab64e56ecad2c3bc59cb393d5", "pom.xml");
        assertLinterPresent(git, Linter.PMD, "36cacf799487075af4b28a1db55acd0aa45ba2f8", "pom.xml");
    }


    private void assertLinterPresent(Git git, Linter linter, String commitName, String fileName) throws IOException, GitAPIException {
        Pair<RevCommit, String> introduction = linterIntroductionFinderDiff.findIntroductionCommitWithProof(git, linter);
        Assertions.assertEquals(commitName, introduction.getLeft().getName());
        Assertions.assertTrue(introduction.getRight().endsWith(fileName));
    }

    private void asserNumberOfLinters(Git git, int expected) {
        Assertions.assertEquals(expected, linterFinder.findLinters(git.getRepository().getDirectory().getParentFile()).size());
    }

}