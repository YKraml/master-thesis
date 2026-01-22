package model;

import java.util.Set;

public enum Language {

    JAVA("**.java", "**/package-info.java, **/module-info.java", Set.of("Java")),
    PYTHON("**.py", "", null),
    GO("**.go", "", null),
    JAVASCRIPT("**.js, **.sj, **.jsx, **.ts, **.tsx, **/tsconfig.json, **/jsconfig.json, **.html", "**/build/**, **/dist/**, **/node_modules/**, **.min.js, **-min.js, **_min.js, **/obfuscated/**, **.d.ts", null),
    CPP("**.cpp, **.hpp, **.cxx, **.hxx, **.cc, **.hh, **.h, **.inc, **.inl", "", null);

    private final String includedFileNames;
    private final String excludedFileNames;
    private final Set<String> sccFileEndings;

    Language(String includedFileNames, String excludedFileNames, Set<String> sccFileEndings) {
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
}
