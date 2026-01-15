import com.google.inject.Guice;
import com.google.inject.Injector;
import io.json.CemProjectPairReaderWriter;
import io.teamscale.api.ProjectCreator;
import model.CemProjectPair;
import util.GuiceModule;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class MainCreateProject {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        ProjectCreator projectCreator = injector.getInstance(ProjectCreator.class);
        CemProjectPairReaderWriter projectPairReaderWriter = injector.getInstance(CemProjectPairReaderWriter.class);

        System.out.println("Start reading proof files");
        List<CemProjectPair> pairs = projectPairReaderWriter
                .readAll()
                .stream()
                .sorted(projectComparator())  // More stars -> earlier creation
                .toList();
        System.out.println("Finished reading proof files");

        int numberOfProjectsToBeCreated = getNumberOfProjectsToBeCreated();
        int i = 0;
        for (CemProjectPair cemProject : pairs) {
            if (i < numberOfProjectsToBeCreated) {
                try {
                    projectCreator.createProjectWithCemProject(cemProject.control());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            i++;
        }
    }

    private static int getNumberOfProjectsToBeCreated() {
        JFrame topFrame = new JFrame();
        topFrame.setAlwaysOnTop(true);
        topFrame.setUndecorated(true);
        topFrame.setLocationRelativeTo(null);
        topFrame.setVisible(true);

        String input = JOptionPane.showInputDialog(topFrame, "How many projects should be created?");

        topFrame.dispose();
        topFrame.requestFocus();

        return Integer.parseInt(input);
    }

    private static Comparator<CemProjectPair> projectComparator() {
        return (o1, o2) -> o2.treated().numberOfStars() - o1.treated().numberOfStars();
    }
}
