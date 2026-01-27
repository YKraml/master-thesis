package io.csv;

import io.json.CemProjectPairReaderWriter;
import io.json.GitHubDataReaderWriter;
import io.json.ProofReaderWriter;
import io.json.TeamscaleDataReaderWriter;
import io.teamscale.api.model.cleaned.TeamscaleData;
import jakarta.inject.Inject;
import logic.DataCombiner;
import model.writable.CemProject;
import model.writable.CemProject.GroupType;
import model.writable.CombinedData;
import model.writable.GitHubData;
import model.writable.Proof;
import model.writable.Proof.LinterWithProof;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.io.File.separator;

public class TimeSeriesCsvCreator implements CsvCreator {

    private static final int[] DAYS = getDays();

    private final DataCombiner dataCombiner;

    private final GitHubDataReaderWriter gitHubDataReaderWriter;
    private final TeamscaleDataReaderWriter teamscaleDataReaderWriter;
    private final ProofReaderWriter proofReaderWriter;
    private final CemProjectPairReaderWriter cemProjectPairReaderWriter;

    @Inject
    public TimeSeriesCsvCreator(DataCombiner dataCombiner, GitHubDataReaderWriter gitHubDataReaderWriter, TeamscaleDataReaderWriter teamscaleDataReaderWriter, ProofReaderWriter proofReaderWriter, CemProjectPairReaderWriter cemProjectPairReaderWriter) {
        this.dataCombiner = dataCombiner;
        this.gitHubDataReaderWriter = gitHubDataReaderWriter;
        this.teamscaleDataReaderWriter = teamscaleDataReaderWriter;
        this.proofReaderWriter = proofReaderWriter;
        this.cemProjectPairReaderWriter = cemProjectPairReaderWriter;
    }

    @Override
    public void startCreatingCsvs() throws IOException {
        System.out.println("Starting TimeSeriesCsvCreator\n");

        System.out.println("Reading cem pair data");
        List<CemProject> cemProjects = cemProjectPairReaderWriter
                .readAll()
                .stream()
                .flatMap(pair -> Stream.of(pair.control(), pair.treated()))
                .toList();
        System.out.println("Finished reading cem pair data\n");

        System.out.println("Combining data");
        List<CombinedData> combinedDataList = new ArrayList<>();
        for (CemProject cemProject : cemProjects) {
            String repoOwner = cemProject.repoOwner();
            String repoName = cemProject.repoName();

            try {
                Proof proof = proofReaderWriter.read(repoOwner, repoName).orElseThrow();
                TeamscaleData teamscaleData = teamscaleDataReaderWriter.read(repoOwner, repoName).orElseThrow();
                GitHubData gitHubData = gitHubDataReaderWriter.read(repoOwner, repoName).orElseThrow();
                combinedDataList.add(dataCombiner.combineData(proof, teamscaleData, cemProject, gitHubData));
            } catch (NoSuchElementException e) {
                System.err.printf("Could not get data for %s.%s%n", repoOwner, repoName);
                e.printStackTrace();
                System.err.println("\n");
            }
        }
        System.out.println("Finished combining data\n");

        System.out.println("Creating files");
        for (CombinedData combinedData : combinedDataList) {
            createCsvFiles(combinedData);
        }
        System.out.println("Finished creating files\n");
    }

    private void createCsvFiles(CombinedData combinedData) throws IOException {
        String repoOwner = combinedData.repoOwner();
        String repoName = combinedData.repoName();

        //Ignore empty data sets
        if (combinedData.entries().isEmpty()) {
            System.out.printf("No entries found in %s.%s %n", repoOwner, repoName);
            return;
        }

        //create folder
        GroupType groupType = combinedData.cemProject().groupType();
        String folderName = "mixed.effects.models" + separator + "data" + separator + "time_series" + separator + groupType.name().toLowerCase();
        new File(folderName).mkdirs();

        // Connect linter commits with proofs
        Map<CemProject.LinterCommit, Optional<LinterWithProof>> linterCommitsWithProof = new HashMap<>();
        for (CemProject.LinterCommit linterCommit : combinedData.cemProject().linterCommits()) {
            Optional<LinterWithProof> linterWithProof = combinedData
                    .proof()
                    .linters()
                    .stream()
                    .filter(linter -> linter.introductionCommit().equals(linterCommit.name()))
                    .findFirst();
            linterCommitsWithProof.put(linterCommit, linterWithProof);
        }

        // Create file for each linter
        int i = 0;
        for (Map.Entry<CemProject.LinterCommit, Optional<LinterWithProof>> entry : linterCommitsWithProof.entrySet()) {
            createFile(combinedData, entry, i, folderName);
            i++;
        }
    }

    private static void createFile(CombinedData combinedData, Map.Entry<CemProject.LinterCommit, Optional<LinterWithProof>> linterCommitWithProof, int i, String folderName) throws IOException {

        // Extracting some infos from the given data
        String repoOwner = combinedData.repoOwner();
        String repoName = combinedData.repoName();

        CemProject.LinterCommit linterCommit = linterCommitWithProof.getKey();
        int introductionTime = linterCommit.time();
        double introduction_time_relative_to_first_commit_time = (introductionTime - combinedData.proof().firstCommitTime()) / 60d / 60d / 24 / 365d;

        int treated = combinedData.cemProject().groupType() == GroupType.TREATMENT ? 1 : 0;

        // Create treemap to find latest commit at a specific time
        NavigableMap<Integer, CombinedData.Entry> entryByTime = new TreeMap<>();
        for (CombinedData.Entry entry : combinedData.entries()) {
            entryByTime.put(entry.time(), entry);
        }

        // Creating id for file name and id column
        Optional<LinterWithProof> linterWithProof = linterCommitWithProof.getValue();
        String linterName = linterWithProof.map(LinterWithProof::linterName).orElse("CONTROL" + i);
        String id = repoOwner + "." + repoName + "." + linterName;

        // Create file
        File dataFile = new File(folderName + separator + id + ".csv");
        dataFile.createNewFile();

        // Write into file
        try (PrintWriter pw = new PrintWriter(dataFile)) {
            pw.println("id,relative_introduction_time,linter,treated," + CombinedData.Entry.csvHeader() + ",intervention,t");

            // For each week (two years in each direction) one row is written
            for (int day : DAYS) {

                int pointInSeconds = day * 24 * 60 * 60;
                int time = introductionTime + pointInSeconds;
                Map.Entry<Integer, CombinedData.Entry> floorEntry = entryByTime.floorEntry(time);
                if (floorEntry == null) {
                    continue;
                }

                CombinedData.Entry floorEntryValue = floorEntry.getValue();
                int intervention = day < 0 ? 0 : 1;

                pw.println(id + ","
                        + introduction_time_relative_to_first_commit_time + ","
                        + linterName + ","
                        + treated + ","
                        + floorEntryValue.csvLine() + ","
                        + intervention + ","
                        + day);
            }
        }
    }

    private static int[] getDays() {
        int start = -1 * 7 * 52 * 2;
        int end = 7 * 52 * 2;
        int jump = 7;

        return IntStream.iterate(start, i -> i <= end, i -> i + jump).toArray();
    }

}
