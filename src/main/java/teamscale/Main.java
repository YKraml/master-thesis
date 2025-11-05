package teamscale;

import com.google.inject.Guice;
import com.google.inject.Injector;
import common.GuiceModule;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(ProjectCreator.class).createAllProjects();
    }
}
