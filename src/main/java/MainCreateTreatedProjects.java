import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.ProjectCreatorStarter;
import model.CemProject;
import util.GuiceModule;

import java.io.IOException;

public class MainCreateTreatedProjects {

    public static void main(String[] args) throws IOException, InterruptedException {
        Injector injector = Guice.createInjector(new GuiceModule());
        ProjectCreatorStarter projectCreatorStarter = injector.getInstance(ProjectCreatorStarter.class);
        projectCreatorStarter.createProjects(CemProject.GroupType.TREATMENT);
    }
}
