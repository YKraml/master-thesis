package logic;

import io.json.CemProjectPairReaderWriter;
import io.teamscale.api.ProjectCreator;
import io.teamscale.api.ProjectsFetcher;
import io.teamscale.api.model.project.Project;
import jakarta.inject.Inject;
import model.writable.CemProject;
import model.writable.CemProjectPair;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectCreatorStarter {

    private final CemProjectPairReaderWriter cemProjectPairReaderWriter;
    private final ProjectCreator projectCreator;
    private final ProjectsFetcher projectsFetcher;

    @Inject
    public ProjectCreatorStarter(CemProjectPairReaderWriter cemProjectPairReaderWriter, ProjectCreator projectCreator, ProjectsFetcher projectsFetcher) {
        this.cemProjectPairReaderWriter = cemProjectPairReaderWriter;
        this.projectCreator = projectCreator;
        this.projectsFetcher = projectsFetcher;
    }

    public void createProjects(CemProject.GroupType groupType) throws IOException, InterruptedException {
        System.out.println("Start reading proof files");
        List<CemProjectPair> pairs = cemProjectPairReaderWriter
                .readAll()
                .stream()
                .sorted(projectComparator())  // More stars -> earlier creation
                .toList();
        System.out.println("Finished reading proof files");

        Set<String> existingProjects = projectsFetcher
                .getExistingProjects()
                .stream()
                .map(Project::name)
                .collect(Collectors.toSet());

        int numberOfProjectsToBeCreated = getNumberOfProjectsToBeCreated();
        int numberOfCreatedProjects = 0;
        for (CemProjectPair cemProject : pairs) {

            if (numberOfCreatedProjects >= numberOfProjectsToBeCreated) {
                break;
            }

            switch (groupType) {
                case CONTROL -> {
                    CemProject project = cemProject.control();
                    String id = project.repoOwner() + "." + project.repoName();
                    if (existingProjects.contains(id)) {
                        continue;
                    }
                    projectCreator.createProjectWithCemProject(project);
                }
                case TREATMENT -> {
                    CemProject project = cemProject.treated();
                    String id = project.repoOwner() + "." + project.repoName();
                    if (existingProjects.contains(id)) {
                        continue;
                    }
                    projectCreator.createProjectWithCemProject(project);
                }
                default -> throw new IllegalStateException("Unexpected value: " + groupType);
            }

            numberOfCreatedProjects++;
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
