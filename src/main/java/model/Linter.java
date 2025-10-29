package model;

import model.indicators.FileNameEndsWithAndContentContainsIndicator;
import model.indicators.FileNameEndsWithIndicator;
import model.indicators.Indicator;

import java.util.Arrays;
import java.util.List;

public enum Linter {

    CHECKSTYLE(Language.JAVA, List.of(
            new FileNameEndsWithIndicator("checkstyle.xml")
    )),
    SPOTBUGS(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsIndicator("pom.xml","spotbugs"),
            new FileNameEndsWithAndContentContainsIndicator("build.gradle","spotbugs")
    )),
    FINDBUGS(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsIndicator("pom.xml","findbugs"),
            new FileNameEndsWithAndContentContainsIndicator("build.gradle","findbugs")
    )),
    PMD(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsIndicator("pom.xml","maven-pmd-plugin"),
            new FileNameEndsWithAndContentContainsIndicator("build.gradle","maven-pmd-plugin")
    ));


    private final Language language;
    private final List<Indicator> indicators;

    Linter(Language language, List<Indicator> indicators) {
        this.language = language;
        this.indicators = indicators;
    }

    public static List<Linter> getLinterOfLanguage(Language language) {
        return Arrays.stream(Linter.values()).filter(linter -> linter.language.equals(language)).toList();
    }

    public Language getLanguage() {
        return language;
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }
}
