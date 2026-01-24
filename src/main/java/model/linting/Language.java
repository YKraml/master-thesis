package model.linting;

import java.util.Set;

public enum Language {

    JAVA("Java (default)", "**.java", "**/package-info.java, **/module-info.java", Set.of("Java")),
    PYTHON("", "**.py", "", Set.of()),
    GO("", "**.go", "", Set.of()),
    JAVASCRIPT("", "**.js, **.sj, **.jsx, **.ts, **.tsx, **/tsconfig.json, **/jsconfig.json, **.html", "**/build/**, **/dist/**, **/node_modules/**, **.min.js, **-min.js, **_min.js, **/obfuscated/**, **.d.ts", Set.of()),
    CPP("", "**.cpp, **.hpp, **.cxx, **.hxx, **.cc, **.hh, **.h, **.inc, **.inl", "", Set.of());

    private final String analysisProfile;
    private final String includedFileNames;
    private final String excludedFileNames;
    private final Set<String> sccFileEndings;

    Language(String analysisProfile, String includedFileNames, String excludedFileNames, Set<String> sccFileEndings) {
        this.analysisProfile = analysisProfile;
        this.includedFileNames = includedFileNames;
        this.excludedFileNames = excludedFileNames;
        this.sccFileEndings = sccFileEndings;
    }

    public String getExcludedFileNames() {
        return excludedFileNames;
    }

    public String getIncludedFileNames() {
        return includedFileNames;
    }

    public Set<String> getSccFileEndings() {
        return sccFileEndings;
    }

    public String getAnalysisProfile() {
        return analysisProfile;
    }
}
