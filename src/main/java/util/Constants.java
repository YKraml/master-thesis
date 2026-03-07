package util;

import model.linting.Language;

import java.util.Random;

public class Constants {

    public static final String GITHUB_TOKEN = "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";

    public static final String TEAMSCALE_ACCESS_KEY = "S9ETWm94zvEVg3QaSCh8g99bTWbrj0sE";
    public static final String TEAMSCALE_URL = "https://yannickkraml.ddnss.de";
    public static final String TEAMSCALE_USER = "admin";

    public static final Random RANDOM = new Random(42);
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final int CONCURRENT_REPO_DOWNLOADS = 4;

    public static final int NUMBER_OF_REPOS = 20000;
    public static final Language CURRENT_LANGUAGE = Language.CPP;

    public static final int SIZE_IN_KB = 1000 * 1000; // 1 GB
    public static final int SIZE_IN_BYTES = 1000 * 1000 * 1000; // 1 GB

    public static final int ANALYSIS_TIME = 1764547200; // Time in seconds at: 2025.12.01, 00:00:00
    public static final int ANALYSIS_WINDOW_IN_SECONDS = 60 * 60 * 24 * 365 * 2; // 2 Years in seconds

    public static final String SCC_EXE = "C:/ProgramData/chocolatey/lib/scc/tools/scc.exe";
}
