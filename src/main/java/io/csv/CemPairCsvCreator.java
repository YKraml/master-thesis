package io.csv;

import io.json.CemProjectPairReaderWriter;
import io.json.ProofReaderWriter;
import jakarta.inject.Inject;
import model.writable.CemProject;
import model.writable.CemProjectPair;
import model.writable.Proof;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.separator;

public class CemPairCsvCreator implements CsvCreator {

    private final CemProjectPairReaderWriter cemProjectPairReaderWriter;
    private final ProofReaderWriter proofReaderWriter;

    @Inject
    CemPairCsvCreator(CemProjectPairReaderWriter cemProjectPairReaderWriter, ProofReaderWriter proofReaderWriter) {
        this.cemProjectPairReaderWriter = cemProjectPairReaderWriter;
        this.proofReaderWriter = proofReaderWriter;
    }

    @Override
    public void startCreatingCsvs() throws IOException {
        System.out.println("Starting CemPairCsvCreator");

        List<CemProjectPair> cemProjectPairs = cemProjectPairReaderWriter.readAll();

        List<CemProject> treated = new ArrayList<>();
        List<CemProject> control = new ArrayList<>();
        for (CemProjectPair cemProjectPair : cemProjectPairs) {
            treated.add(cemProjectPair.treated());
            control.add(cemProjectPair.control());
        }

        createFile("treated", treated);
        createFile("control", control);

        System.out.println("Finished CemPairCsvCreator");
    }

    private void createFile(String string, List<CemProject> cemProjects) throws IOException {
        //create file
        String folderName = "mixed.effects.models"
                + separator + "data"
                + separator + Constants.CURRENT_LANGUAGE.name().toLowerCase()
                + separator + "cem_projects";
        new File(folderName).mkdirs();
        File dataFile = new File(folderName + separator + string + ".csv");
        if (!dataFile.createNewFile()) {
            return;
        }

        try (PrintWriter pw = new PrintWriter(dataFile)) {
            pw.println("id" + "," +
                    "repoOwner" + "," +
                    "repoName" + "," +
                    "groupType" + "," +
                    "numberOfStars" + "," +
                    "creationYear" + "," +
                    "firstCommitTime" + "," +
                    "defaultBranch" + "," +
                    "numberOfCommits" + "," +
                    "loc" + "," +
                    "sloc" + "," +
                    "numberOfContributors" + "," +
                    "linterCommitName" + "," +
                    "linterCommitTime" + "," +
                    "linterCommitIndex" + "," +
                    "linterName" + "," +
                    "numberOfCommitsInTwoYearWindow");

            for (CemProject cemProject : cemProjects) {
                String repoOwner = cemProject.repoOwner();
                String repoName = cemProject.repoName();
                CemProject.GroupType groupType = cemProject.groupType();
                int numberOfStars = cemProject.numberOfStars();
                String creationYear = cemProject.creationYear();
                String defaultBranch = cemProject.defaultBranch();
                int numberOfCommits = cemProject.numberOfCommits();
                int loc = cemProject.loc();
                int sloc = cemProject.sloc();
                int numberOfContributors = cemProject.numberOfContributors();

                List<CemProject.LinterCommit> linterCommits = cemProject.linterCommits();
                Proof proof = proofReaderWriter.read(repoOwner, repoName).orElseThrow();

                for (int i = 0; i < linterCommits.size(); i++) {
                    CemProject.LinterCommit linterCommit = linterCommits.get(i);
                    String name = linterCommit.name();
                    int time = linterCommit.time();
                    int index = linterCommit.index();
                    String linterName = linterCommit.linterName();
                    int numberOfCommitsInTwoYearWindow = linterCommit.numberOfCommitsInTwoYearWindow();

                    String id = repoOwner + "." + repoName + "." + i;
                    pw.println(id + "," +
                            repoOwner + "," +
                            repoName + "," +
                            groupType + "," +
                            numberOfStars + "," +
                            creationYear + "," +
                            proof.firstCommitTime() + "," +
                            defaultBranch + "," +
                            numberOfCommits + "," +
                            loc + "," +
                            sloc + "," +
                            numberOfContributors + "," +
                            name + "," +
                            time + "," +
                            index + "," +
                            linterName + "," +
                            numberOfCommitsInTwoYearWindow);
                }
            }
        }
    }
}
