package logic;

import com.google.inject.Guice;
import com.google.inject.Injector;
import linter.searching.logic.LinterFinder;
import linter.searching.logic.LinterIntroductionFinder;
import linter.searching.logic.RepositoryDownloader;
import linter.searching.model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import common.GuiceModule;

import java.io.File;
import java.io.IOException;

@Execution(ExecutionMode.CONCURRENT)
class LinterIntroductionFinderTest {

    private RepositoryDownloader repositoryDownloader;
    private LinterFinder linterFinder;
    private LinterIntroductionFinder linterIntroductionFinder;

    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector(new GuiceModule());
        repositoryDownloader = injector.getInstance(RepositoryDownloader.class);
        linterFinder = injector.getInstance(LinterFinder.class);
        linterIntroductionFinder = injector.getInstance(LinterIntroductionFinder.class);
    }

    @Test
    void testHaloDevHalo() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("halo-dev", "halo");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 1),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "e8db12a93dbab4e49bbff1ed136cc25357d4504d", "build.gradle")
        );
    }

    @Test
    void testTheAlgorithmsJava() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("TheAlgorithms", "Java");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "7a42f68b66dfc7a53d67125f9685a2db0a5e2dd9", "pom.xml"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "4bb64559deb6722069678aaa910f7379645d5a7b", "pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "cb401fed69258f3ccc0484ccd9f588c10af0dd32", "pom.xml")
        );
    }

    @Test
    void testNettyNetty() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("netty", "netty");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 1),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "ebfc4513e0ba619592af3ba946bd1974d1becb55", "pom.xml")
        );
    }

    @Test
    void testApacheIncubatorSeata() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "incubator-seata");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 2),
                () -> assertLinterPresent(git, Linter.PMD, "dbc295fc6518a988f62af3730e27fb3615938ddd", "pom.xml"),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "85192cc816e71a852e0123333ed964f0c6c6f3b4", "style/feats_checkstyle.xml")
        );
    }

    @Test
    void testRedissonRedisson() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("redisson", "redisson");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "173000394121d1f0e8a97fd4c185d4449e2bcbad", "pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "173000394121d1f0e8a97fd4c185d4449e2bcbad", "pom.xml"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "07684ee5c2d4bf2408bd8eeefeb88dcb53c3fdfe", "redisson/pom.xml")
        );
    }

    @Test
    void testApacheRocketmq() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "rocketmq");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 2),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "ec5a9c9bf611631ba62f38309a7ffdabaf8f6d89", "pom.xml"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "0623b3c6392e717af6879cd3bf78fc2ca40308f4", "pom.xml")
        );
    }

    @Test
    void testBumptechGlide() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("bumptech", "glide");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 2),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "fe090f50f3040f4d478143a3e0ffa8cdf813fefc", "build.gradle"),
                () -> assertLinterPresent(git, Linter.PMD, "42086c3cc9c9303dd7d219cd43da7426683d3a26", "library/build.gradle")
        );
    }

    @Test
    void testApacheKafka() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "kafka");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 2),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "1c6d5bbac672cbf49591aed0889510b10e3285fa", "build.gradle"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "c9872cb2142bbee12e4657af9a84627a1684fddd", "build.gradle")
        );
    }

    @Test
    void testTencentTinker() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("Tencent", "tinker");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 1),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "ddd547f864903f1689091a3b3ac53f87198cfdc9", "build.gradle")
        );
    }

    @Test
    void testNetflixZuul() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("Netflix", "zuul");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 1),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "697fd66aae9beed107e13f49a741455f1d9d8dd9", "gradle/check.gradle")
        );
    }

    @Test
    void testAlibabaNacos() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("alibaba", "nacos");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "7c4cca4360284344d9194c0d0f76edca02a5ba9c", "style/NacosCheckStyle.xml"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "91d20f34fdec6b77b0b27c874983ca57a2feb584", "pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "91d20f34fdec6b77b0b27c874983ca57a2feb584", "pom.xml")
        );
    }

    @Test
    void testApacheShardingsphere() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("apache", "shardingsphere");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "5fd3ee9c4fec5cbd672593606de0d3db93b15241", "pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "5fd3ee9c4fec5cbd672593606de0d3db93b15241", "pom.xml"),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "5fd3ee9c4fec5cbd672593606de0d3db93b15241", "pom.xml")
        );
    }

    @Test
    void testPinpointapmPinpoint() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("pinpoint-apm", "pinpoint");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "6f9b181081e4b570ba3219bae657ac8675609cf5", "checkstyle-UTF8(ver0.6).xml"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "8997e2a20a65e378be524fe45455be994209c148", "pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "36cacf799487075af4b28a1db55acd0aa45ba2f8", "pom.xml")
        );
    }

    @Test
    void testRealmRealmJava() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("realm", "realm-java");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "d9f5c8fc0b6f0d3788769d1100220fc07719ab9e", "realm/build.gradle"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "876bac7df960ad8535a01c1c64228ccc806d6fe2", "realm/build.gradle"),
                () -> assertLinterPresent(git, Linter.PMD, "eb041c8a486b6cd3031752ff6b2d048320b1c330", "realm/realm-library/build.gradle")
        );
    }

    @Test
    void testGooglePhysicalWeb() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("google", "physical-web");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "9c79041432287be4009ed3dca2eb1e6907b86c55", "android/PhysicalWeb/app/build.gradle"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "c407aaf545a5be52f406d7671144b9746dc7db3b", "android/PhysicalWeb/app/build.gradle"),
                () -> assertLinterPresent(git, Linter.PMD, "0c6210186510e3a8a6ad8bb050f2d1096b6a8573", "android/PhysicalWeb/app/build.gradle")
        );
    }

    @Test
    void testIflytekAstronAgent() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("iflytek", "astron-agent");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 3),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "6a496ae4b0e3d5708c9f4b19502d706892c5e02c", "console/backend/pom.xml"),
                () -> assertLinterPresent(git, Linter.SPOTBUGS, "6a496ae4b0e3d5708c9f4b19502d706892c5e02c", "console/backend/pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "6a496ae4b0e3d5708c9f4b19502d706892c5e02c", "console/backend/pom.xml")
        );
    }

    @Test
    void testQuerydslQuerydsl() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("querydsl", "querydsl");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 2),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "13a4afeda8b4ba6c982f234f8aaed41142c3adb0", "pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "015e0b9c5cbbdb70ba109c35d06a948510572216", "querydsl-apt/pom.xml")
        );
    }

    @Test
    void testActivitiActiviti() throws GitAPIException, IOException {
        Git git = repositoryDownloader.getRepo("Activiti", "Activiti");
        Assertions.assertAll(
                () -> asserNumberOfLinters(git, 2),
                () -> assertLinterPresent(git, Linter.CHECKSTYLE, "48c823db90aa29ec90adf4d70a45c7db99d1d448", "pom.xml"),
                () -> assertLinterPresent(git, Linter.PMD, "7481f93a37103944203f2ccdbd8ed716fcc0d647", "pom.xml")
        );
    }

    private void assertLinterPresent(Git git, Linter linter, String commitName, String fileName) throws IOException, GitAPIException {
        Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroductionCommitWithProof(git, linter);
        Assertions.assertEquals(commitName, introduction.getLeft().getName());
        Assertions.assertEquals(fileName.replace("/", File.separator), introduction.getRight());
    }

    private void asserNumberOfLinters(Git git, int expected) throws IOException {
        Assertions.assertEquals(expected, linterFinder.findLinters(git.getRepository().getDirectory().getParentFile()).size());
    }
}