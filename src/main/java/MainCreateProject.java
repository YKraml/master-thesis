import com.google.inject.Guice;
import com.google.inject.Injector;
import util.GuiceModule;
import api.ProjectCreator;

import java.io.IOException;

public class MainCreateProject {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        ProjectCreator projectCreator = injector.getInstance(ProjectCreator.class);
        projectCreator.createAllProjects();
    }
}
