package util;

import model.linting.Language;

import java.util.Random;

public class Constants {

    public static final String GITHUB_TOKEN = "YOUR_GITHUB_TOKEN";

    public static final String TEAMSCALE_ACCESS_KEY = "YOUR_TEAMSCALE_ACCESS_KEY";
    public static final String TEAMSCALE_URL = "YOUR_TEAMSCALE_URL";
    public static final String TEAMSCALE_USER = "YOUR_TEAMSCALE_USER";

    public static final Random RANDOM = new Random(42);
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final int CONCURRENT_REPO_DOWNLOADS = 4;

    public static final int NUMBER_OF_REPOS = 20000;
    public static final int SIZE_IN_KB = 1000 * 1000; // 1 GB
    public static final int SIZE_IN_BYTES = 1000 * 1000 * 1000; // 1 GB
    public static final int ANALYSIS_TIME = 1764547200; // Time in seconds at: 2025.12.01, 00:00:00
    public static final int ANALYSIS_WINDOW_IN_SECONDS = 60 * 60 * 24 * 365 * 2; // 2 Years in seconds
    public static final String SCC_EXE = "YOUR_PATH/scc.exe";
    public static Language CURRENT_LANGUAGE = Language.JAVA; //Change your targer language here
}
