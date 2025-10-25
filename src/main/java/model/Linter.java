package model;

public enum Linter {

    CHECKSTYLE(Language.JAVA),
    SPOTBUGS(Language.JAVA),
    FINDBUGS(Language.JAVA),
    PMD(Language.JAVA),
    QODANA(Language.JAVA);


    private final Language language;

    Linter(Language language) {
        this.language = language;
    }

    public Language getLanguage() {
        return language;
    }
}
