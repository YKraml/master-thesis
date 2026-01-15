import com.google.inject.Guice;
import com.google.inject.Injector;
import io.json.CemProjectControlReaderWriter;
import io.json.CemProjectTreatmentReaderWriter;
import io.teamscale.api.ProjectCreator;
import model.CemProject;
import util.GuiceModule;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.List;

public class MainCreateProject {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        ProjectCreator projectCreator = injector.getInstance(ProjectCreator.class);

        CemProjectTreatmentReaderWriter cemProjectTreatmentReaderWriter = injector.getInstance(CemProjectTreatmentReaderWriter.class);
        CemProjectControlReaderWriter cemProjectControlReaderWriter = injector.getInstance(CemProjectControlReaderWriter.class);

        System.out.println("Start reading proof files");
        List<CemProject> cemProjects = cemProjectTreatmentReaderWriter.readAll();
        System.out.println("Finished reading proof files");

        cemProjects.sort((o1, o2) -> o2.numberOfStars() - o1.numberOfStars()); // More stars -> earlier creation

        int numberOfProjectsToBeCreated = getNumberOfProjectsToBeCreated();
        int i = 0;
        for (CemProject cemProject : cemProjects) {
            if (i < numberOfProjectsToBeCreated) {
                projectCreator.createProjectWithCemProject(cemProject);
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

        int numberOfProjectsToBeCreeated;
        try {
            numberOfProjectsToBeCreeated = Integer.parseInt(input);
        } catch (Exception e) {
            numberOfProjectsToBeCreeated = -1;
        }
        return numberOfProjectsToBeCreeated;
    }
}
