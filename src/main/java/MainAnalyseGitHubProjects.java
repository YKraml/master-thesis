import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GitHubAnalysisStarter;
import util.GuiceModule;

public class MainAnalyseGitHubProjects {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(GitHubAnalysisStarter.class).start();
    }
}
