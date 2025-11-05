package linter.searching;

import com.google.inject.Guice;
import com.google.inject.Injector;
import linter.searching.logic.Starter;
import common.GuiceModule;

public class Main {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(Starter.class).start();
    }
}
