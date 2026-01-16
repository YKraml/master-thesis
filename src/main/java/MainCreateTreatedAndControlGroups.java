import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GroupsCreator;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.GuiceModule;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class MainCreateTreatedAndControlGroups {

    public static void main(String[] args) throws IOException, GitAPIException {
        Injector injector = Guice.createInjector(new GuiceModule());
        GroupsCreator groupsCreator = injector.getInstance(GroupsCreator.class);
        ExecutorService executorService = injector.getInstance(ExecutorService.class);

        groupsCreator.createTreatmentAndControlGroups();
        executorService.close();
    }

}
