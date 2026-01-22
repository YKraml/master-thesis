package io.csv;

import io.json.CemProjectPairReaderWriter;
import io.json.ProofReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.CemProjectPair;
import model.Proof;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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

        createFiles("treated", treated);
        createFiles("control", control);
    }

    private void createFiles(String string, List<CemProject> cemProjects) throws IOException {
        //create file
        String folderName = "mixed.effects.models" + File.separator + "data" + File.separator + "cem_projects";
        new File(folderName).mkdirs();
        File dataFile = new File(folderName + File.separator + string + ".csv");
        dataFile.createNewFile();

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
                    "linterCommitName" + "," +
                    "linterCommitTime" + "," +
                    "linterCommitIndex" + "," +
                    "linterName" + "," +
                    "numberOfCommitsInTwoYearWindow" + "," +
                    "loc" + "," +
                    "sloc");

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

                List<CemProject.LinterCommit> linterCommits = cemProject.linterCommits();
                Proof proof = proofReaderWriter.getElement(repoOwner, repoName).orElseThrow();

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
                            name + "," +
                            time + "," +
                            index + "," +
                            linterName + "," +
                            numberOfCommitsInTwoYearWindow + "," +
                            loc + "," +
                            sloc);
                }
            }
        }
    }
}
