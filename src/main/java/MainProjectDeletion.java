import com.google.inject.Guice;
import util.GuiceModule;
import api.ProjectDeleter;

public class MainProjectDeletion {
    public static void main(String[] args) {
        ProjectDeleter projectDeleter = Guice.createInjector(new GuiceModule()).getInstance(ProjectDeleter.class);
        projectDeleter.deleteAllProjects();
    }
}
