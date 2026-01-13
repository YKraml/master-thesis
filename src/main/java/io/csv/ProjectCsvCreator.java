package io.csv;

import io.json.CombinedDataReaderWriter;
import jakarta.inject.Inject;
import model.CombinedData;
import model.Language;
import model.Proof;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ProjectCsvCreator implements CsvCreator {

    private final CombinedDataReaderWriter combinedDataReaderWriter;

    @Inject
    public ProjectCsvCreator(CombinedDataReaderWriter combinedDataReaderWriter) {
        this.combinedDataReaderWriter = combinedDataReaderWriter;
    }

    @Override
    public void startCreatingCsvs() throws IOException {
        System.out.println("Starting ProjectCsvCreator");

        //create file
        String folderName = "mixed.effects.models" + File.separator + "data" + File.separator + "project_data";
        new File(folderName).mkdirs();
        File dataFile = new File(folderName + File.separator + "projects.csv");
        dataFile.createNewFile();

        try (PrintWriter pw = new PrintWriter(dataFile)) {
            pw.println("id," +
                    "repoOwner,repoName," +
                    "language,link,defaultBranch," +
                    "currentCommit,currentCommitTime," +
                    "firstCommit,firstCommitTime," +
                    "analysisStartTime,analysisEndTime," +
                    "numberOfStars," +
                    "linterName,currentFile,introductionCommit,introductionFile,introductionTime");

            for (CombinedData combinedData : combinedDataReaderWriter.readAll()) {
                Proof proof = combinedData.proof();

                String repoOwner = proof.repoOwner();
                String repoName = proof.repoName();
                Language language = proof.language();
                String link = proof.link();
                String defaultBranch = proof.defaultBranch();
                String currentCommit = proof.currentCommit();
                int currentCommitTime = proof.currentCommitTime();
                String firstCommit = proof.firstCommit();
                int firstCommitTime = proof.firstCommitTime();
                int analysisStartTime = proof.analysisStartTime();
                int analysisEndTime = proof.analysisEndTime();
                int numberOfStars = proof.numberOfStars();

                for (Proof.LinterWithProof linter : proof.linters()) {
                    String linterName = linter.linterName();
                    String currentFile = linter.currentFile();
                    String introductionCommit = linter.introductionCommit();
                    String introductionFile = linter.introductionFile();
                    int introductionTime = linter.introductionTime();

                    String id = repoOwner + "." + repoName + "." + linterName;
                    pw.println(id + "," +
                            repoOwner + "," +
                            repoName + "," +
                            language + "," +
                            link + "," +
                            defaultBranch + "," +
                            currentCommit + "," +
                            currentCommitTime + "," +
                            firstCommit + "," +
                            firstCommitTime + "," +
                            analysisStartTime + "," +
                            analysisEndTime + "," +
                            numberOfStars + "," +
                            linterName + "," +
                            currentFile + "," +
                            introductionCommit + "," +
                            introductionFile + "," +
                            introductionTime);
                }

            }

        }

    }
}
