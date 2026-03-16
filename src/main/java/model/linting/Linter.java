package model.linting;

import java.util.Arrays;
import java.util.List;

public enum Linter {

    CHECKSTYLE(Language.JAVA, List.of(
            new FileNameEndsWithFileIndicator("checkstyle.xml"),
            new FileNameEndsWithAndContentContainsFileIndicator("xml", "<module name=\"Checker\">"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "checkstyle {"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", ":maven-checkstyle-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "'checkstyle'"),
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>maven-checkstyle-plugin</artifactId>")
    )),
    SPOTBUGS(Language.JAVA, List.of(
            /* SpotBugs */
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>spotbugs</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>spotbugs-maven-plugin</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", ":spotbugs:"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", ":spotbugs-maven-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "\"com.github.spotbugs\""),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "spotbugs {"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "'spotbugs'"),
            /* FindBugs*/
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>findbugs-maven-plugin</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", ":findbugs-maven-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "'findbugs'"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "findbugs {")
    )),
    PMD(Language.JAVA, List.of(
            new FileNameEndsWithAndContentContainsFileIndicator("pom.xml", "<artifactId>maven-pmd-plugin</artifactId>"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", ":maven-pmd-plugin:"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "'pmd'"),
            new FileNameEndsWithAndContentContainsFileIndicator("gradle", "pmd {")
    )),
    PYLINT(Language.PYTHON, List.of(
            new FileNameEndsWithFileIndicator("pylintrc"),
            new FileNameEndsWithAndContentContainsFileIndicator("toml", "[tool.pylint"),
            new FileNameEndsWithAndContentContainsFileIndicator("setup.py", "'pylint'"),
            new FileNameEndsWithAndContentContainsFileIndicator("setup.py", "'pytest-pylint'"),
            new FileNameEndsWithAndContentContainsFileIndicator("cfg", "--pylint"),
            new FileNameEndsWithAndContentContainsFileIndicator("yaml", ": pylint")
    )),
    FLAKE8(Language.PYTHON, List.of(
            new FileNameEndsWithFileIndicator("flake8"),
            new FileNameEndsWithAndContentContainsFileIndicator("ini", "[flake8"),
            new FileNameEndsWithAndContentContainsFileIndicator("cfg", "[flake8"),
            new FileNameEndsWithAndContentContainsFileIndicator("ini", "[tool.flake8"),
            new FileNameEndsWithAndContentContainsFileIndicator("cfg", "[tool.flake8")

    )),
    RUFF(Language.PYTHON, List.of(
            new FileNameEndsWithAndContentContainsFileIndicator("toml", "[tool.ruff"),
            new FileNameEndsWithAndContentContainsFileIndicator("yaml", ": ruff")
    )),
    CLANG_TIDY(Language.CPP, List.of(
            new FileNameContainsWithIndicator("clang-tidy"),
            new FileNameEndsWithAndContentContainsFileIndicator("yml", "clang-tidy"),
            new FileNameEndsWithAndContentContainsFileIndicator("yaml", "clang-tidy"),
            new FileNameEndsWithAndContentContainsFileIndicator("cmake", "clang-tidy"),
            new FileNameEndsWithAndContentContainsFileIndicator("dockerfile", "clang-tidy"),
            new FileNameEndsWithAndContentContainsFileIndicator("makefile", "clang-tidy")
    )),
    CPPCHECK(Language.CPP, List.of(
            new FileNameContainsWithIndicator("cppcheck"),
            new FileNameEndsWithAndContentContainsFileIndicator("yml", "cppcheck"),
            new FileNameEndsWithAndContentContainsFileIndicator("yaml", "cppcheck"),
            new FileNameEndsWithAndContentContainsFileIndicator("cmake", "cppcheck"),
            new FileNameEndsWithAndContentContainsFileIndicator("dockerfile", "cppcheck"),
            new FileNameEndsWithAndContentContainsFileIndicator("makefile", "cppcheck")
    )),
    OCLint(Language.CPP, List.of(
            new FileNameContainsWithIndicator("oclint"),
            new FileNameEndsWithAndContentContainsFileIndicator("yml", "oclint"),
            new FileNameEndsWithAndContentContainsFileIndicator("yaml", "oclint"),
            new FileNameEndsWithAndContentContainsFileIndicator("cmake", "oclint"),
            new FileNameEndsWithAndContentContainsFileIndicator("dockerfile", "oclint"),
            new FileNameEndsWithAndContentContainsFileIndicator("makefile", "oclint")
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
                return new String[]{"xml", "gradle"};
            }
            case PYTHON -> {
                return new String[]{"toml", "pylintrc", "setup.py", "cfg", "yaml", "flake8"};
            }
            case CPP -> {
                return new String[]{"clang-tidy", "yml", "cmake", "xml", "json", "cfg", "conf", "txt",
                        "xml", "oclint", "sln", "vcxproj", "filters", "props", "targets", "sh", "bat",
                        "cmd", "ps1", "html", "editorconfig", "cpp", "hpp", "dockerfile", "makefile"};
            }
            default -> throw new IllegalStateException("Unexpected value: " + language);
        }
    }
}
