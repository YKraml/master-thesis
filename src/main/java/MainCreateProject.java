import com.google.inject.Guice;
import com.google.inject.Injector;
import io.json.ProofReaderWriter;
import io.teamscale.api.ProjectCreator;
import model.Proof;
import util.GuiceModule;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class MainCreateProject {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        ProjectCreator projectCreator = injector.getInstance(ProjectCreator.class);
        ProofReaderWriter proofReaderWriter = injector.getInstance(ProofReaderWriter.class);

        System.out.println("Start reading proof files");
        List<Proof> proofs = proofReaderWriter.readAll();
        System.out.println("Finished reading proof files");

        proofs.sort((o1, o2) -> o2.numberOfStars() - o1.numberOfStars()); // More stars -> earlier creation
        proofs.removeIf(proof -> proof.linters().isEmpty());

        int numberOfProjectsToBeCreated = getNumberOfProjectsToBeCreated();
        projectCreator.createProjectsWithProofs(proofs, numberOfProjectsToBeCreated);
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
