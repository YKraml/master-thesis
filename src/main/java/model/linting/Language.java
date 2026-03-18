package model.linting;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public enum Language {

    JAVA("Java (default)", "**.java", "**/package-info.java, **/module-info.java", Set.of("Java")),
    PYTHON("Python (default)", "**.py", "", Set.of("Python")),
    CPP("", "**.cpp, **.hpp, **.cxx, **.hxx, **.cc, **.hh, **.h, **.inc, **.inl", "", Set.of("C++", "C", "C Header"));

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

    public static void main(String[] args) throws IOException {
        FileUtils.deleteDirectory(new File("E:\\master-thesis\\temp\\repos_full"));
    }
}
