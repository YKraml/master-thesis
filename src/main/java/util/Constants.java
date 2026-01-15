package util;

import model.Language;

public class Constants {
    public static final String GITHUB_TOKEN = "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";

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final int CONCURRENT_REPO_DOWNLOADS = 4;

    public static final int SECONDS_IN_TWO_YEARS = 60 * 60 * 24 * 365 * 2;
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
}
