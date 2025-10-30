package model;

import model.indicators.FileNameEndsWithAndContentContainsFileIndicator;
import model.indicators.FileNameEndsWithFileIndicator;
import model.indicators.FileIndicator;

import java.util.Arrays;
import java.util.List;

public enum Linter {

    CHECKSTYLE(Language.JAVA, List.of(
            new FileNameEndsWithFileIndicator("checkstyle.xml")
    )),
    SPOTBUGS(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml","spotbugs"),
            new FileNameEndsWithAndContentContainsFileIndicator("build.gradle","spotbugs")
    )),
    FINDBUGS(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml","findbugs"),
            new FileNameEndsWithAndContentContainsFileIndicator("build.gradle","findbugs")
    )),
    PMD(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml","maven-pmd-plugin"),
            new FileNameEndsWithAndContentContainsFileIndicator("build.gradle","maven-pmd-plugin")
    ));


    private final Language language;
    private final List<FileIndicator> fileIndicators;

    Linter(Language language, List<FileIndicator> fileIndicators) {
        this.language = language;
        this.fileIndicators = fileIndicators;
    }

    public static List<Linter> getLinterOfLanguage(Language language) {
        return Arrays.stream(Linter.values()).filter(linter -> linter.language.equals(language)).toList();
    }

    public Language getLanguage() {
        return language;
    }

    public List<FileIndicator> getIndicators() {
        return fileIndicators;
    }
}
