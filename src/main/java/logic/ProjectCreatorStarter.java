package logic;

import io.json.CemProjectPairReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.CemProjectPair;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class ProjectCreator {

    private final CemProjectPairReaderWriter cemProjectPairReaderWriter;
    private final io.teamscale.api.ProjectCreator projectCreator;

    @Inject
    public ProjectCreator(CemProjectPairReaderWriter cemProjectPairReaderWriter, io.teamscale.api.ProjectCreator projectCreator) {

        this.cemProjectPairReaderWriter = cemProjectPairReaderWriter;
        this.projectCreator = projectCreator;
    }

    public void createProjects(CemProject.GroupType groupType) throws IOException {
        System.out.println("Start reading proof files");
        List<CemProjectPair> pairs = cemProjectPairReaderWriter
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

                    switch (groupType) {
                        case CONTROL -> projectCreator.createProjectWithCemProject(cemProject.control());
                        case TREATMENT -> projectCreator.createProjectWithCemProject(cemProject.treated());
                        default -> throw new IllegalStateException("Unexpected value: " + groupType);
                    }
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
