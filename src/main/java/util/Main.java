package util;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {

    public static final String TOKEN = "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";
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(Starter.class).start();
    }
}
