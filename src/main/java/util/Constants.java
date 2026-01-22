package util;

import model.Language;

import java.util.Random;

public class Constants {
    public static final String GITHUB_TOKEN = "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";

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final int CONCURRENT_BARE_REPO_DOWNLOADS = 16;
    public static final int CONCURRENT_REPO_DOWNLOADS = 4;

    public static final int ANALYSIS_WINDOW_IN_SECONDS = 60 * 60 * 24 * 365 * 2;
    public static final Language CURRENT_LANGUAGE = Language.JAVA;

    public static final String TEAMSCALE_ACCESS_KEY = "JR62xEGhWWh7NYh2srekjo61ftAYZBYo";
    public static final String TEAMSCALE_URL = "https://yannickkraml.ddnss.de";
    public static final String TEAMSCALE_USER = "admin";

    public static final String TEAMSCALE_CONTROL_ACCESS_KEY = "JR62xEGhWWh7NYh2srekjo61ftAYZBYo";
    public static final String TEAMSCALE_CONTROL_URL = "https://yannickkraml.ddnss.de";
    public static final String TEAMSCALE_CONTROL_USER = "admin";

    public static final int SIZE_1 = 1000 * 1000; // 1 GB
    public static final int SIZE_2 = 1000 * 1000 * 1000; // 1 GB

    /**
     * Time in seconds at: 2025.12.01, 00:00:00
     */
    public static final int ANALYSIS_TIME = 1764547200;

    public static final int NUMBER_OF_PROJECTS_PER_LANGUAGE = 1000;
    public static final Random RANDOM = new Random(42);
    public static final String SCC = "C:\\Users\\yanni\\AppData\\Local\\Microsoft\\WinGet\\Packages\\BenBoyter.scc_Microsoft.Winget.Source_8wekyb3d8bbwe\\scc.exe";
}
