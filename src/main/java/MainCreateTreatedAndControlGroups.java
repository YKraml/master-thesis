import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GroupsCreator;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.GuiceModule;

import java.io.IOException;

public class MainCreateTreatedAndControlGroups {

    public static void main(String[] args) throws IOException, GitAPIException {
        Injector injector = Guice.createInjector(new GuiceModule());
        GroupsCreator groupsCreator = injector.getInstance(GroupsCreator.class);
        groupsCreator.createTreatmentAndControlGroups();
    }
}
