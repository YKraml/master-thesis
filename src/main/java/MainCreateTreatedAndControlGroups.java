import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GroupsCreator;
import util.GuiceModule;

import java.io.IOException;

public class MainCreateTreatedAndControlGroups {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        GroupsCreator groupsCreator = injector.getInstance(GroupsCreator.class);
        groupsCreator.createTreatmentAndControlGroups();
    }

}
