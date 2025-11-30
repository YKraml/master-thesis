package model;

public enum Language {

    JAVA("**.java", "**/package-info.java, **/module-info.java"),
    PYTHON("**.py", ""),
    GO("**.go", ""),
    JAVASCRIPT("**.js, **.sj, **.jsx, **.ts, **.tsx, **/tsconfig.json, **/jsconfig.json, **.html", "**/build/**, **/dist/**, **/node_modules/**, **.min.js, **-min.js, **_min.js, **/obfuscated/**, **.d.ts"),
    CPP("**.cpp, **.hpp, **.cxx, **.hxx, **.cc, **.hh, **.h, **.inc, **.inl", "");

    private final String includedFileNames;
    private final String excludedFileNames;

    Language(String includedFileNames, String excludedFileNames) {
        this.includedFileNames = includedFileNames;
        this.excludedFileNames = excludedFileNames;
    }

    public String getExcludedFileNames() {
        return excludedFileNames;
    }

    public String getIncludedFileNames() {
        return includedFileNames;
    }
}
