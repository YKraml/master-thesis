import com.google.inject.Guice;
import io.teamscale.api.ProjectDeleter;
import util.GuiceModule;

public class MainDeleteAllProjects {
    public static void main(String[] args) {
        ProjectDeleter projectDeleter = Guice.createInjector(new GuiceModule()).getInstance(ProjectDeleter.class);
        projectDeleter.deleteAllProjects();
    }
}
