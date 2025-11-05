package model;

import model.indicators.FileIndicator;
import model.indicators.FileNameEndsWithAndContentContainsFileIndicator;
import model.indicators.FileNameEndsWithFileIndicator;

import java.util.Arrays;
import java.util.List;

public enum Linter {

    CHECKSTYLE(Language.JAVA, List.of(
            new FileNameEndsWithFileIndicator("checkstyle.xml"),
            new FileNameEndsWithAndContentContainsFileIndicator(".xml", "<module name=\"Checker\">"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "checkstyle {"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", ":maven-checkstyle-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "'checkstyle'"),
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>maven-checkstyle-plugin</artifactId>")
    )),
    SPOTBUGS(Language.JAVA, List.of(
            /* SpotBugs */
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>spotbugs</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>spotbugs-maven-plugin</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", ":spotbugs:"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", ":spotbugs-maven-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "\"com.github.spotbugs\""),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "spotbugs {"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "'spotbugs'"),
            /* FindBugs*/
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>findbugs-maven-plugin</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", ":findbugs-maven-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "'findbugs'"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "findbugs {")
    )),
    PMD(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>maven-pmd-plugin</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", ":maven-pmd-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "'pmd'"),
            new FileNameEndsWithAndContentContainsFileIndicator(".gradle", "pmd {")
    ));


    private final Language language;
    private final List<FileIndicator> fileIndicators;

    Linter(Language language, List<FileIndicator> fileIndicators) {
        this.language = language;
        this.fileIndicators = fileIndicators;
    }


    public Language getLanguage() {
        return language;
    }

    public List<FileIndicator> getIndicators() {
        return fileIndicators;
    }

    public static List<Linter> getLinterOfLanguage(Language language) {
        return Arrays.stream(Linter.values()).filter(linter -> linter.language.equals(language)).toList();
    }

    public static String[] getExtensionsOfLanguage(Language language) {
        switch (language) {
            case JAVA -> {
                return new String[]{".xml", ".gradle"};
            }
            default -> throw new IllegalStateException("Unexpected value: " + language);
        }
    }
}
