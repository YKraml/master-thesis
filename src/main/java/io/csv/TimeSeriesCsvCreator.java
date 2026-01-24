package io.csv;

import io.json.CemProjectReaderWriter;
import io.json.GitHubDataReaderWriter;
import io.json.ProofReaderWriter;
import io.json.TeamscaleDataReaderWriter;
import io.teamscale.api.model.cleaned.TeamscaleData;
import jakarta.inject.Inject;
import logic.DataCombiner;
import model.CemProject;
import model.CemProject.GroupType;
import model.CombinedData;
import model.GitHubData;
import model.Proof;
import model.Proof.LinterWithProof;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.IntStream;

import static java.io.File.separator;

public class TimeSeriesCsvCreator implements CsvCreator {

    private static final int[] DAYS = getDays();

    private final DataCombiner dataCombiner;

    private final GitHubDataReaderWriter gitHubDataReaderWriter;
    private final TeamscaleDataReaderWriter teamscaleDataReaderWriter;
    private final ProofReaderWriter proofReaderWriter;
    private final CemProjectReaderWriter cemProjectReaderWriter;

    @Inject
    public TimeSeriesCsvCreator(DataCombiner dataCombiner, GitHubDataReaderWriter gitHubDataReaderWriter, TeamscaleDataReaderWriter teamscaleDataReaderWriter, ProofReaderWriter proofReaderWriter, CemProjectReaderWriter cemProjectReaderWriter) {
        this.dataCombiner = dataCombiner;
        this.gitHubDataReaderWriter = gitHubDataReaderWriter;
        this.teamscaleDataReaderWriter = teamscaleDataReaderWriter;
        this.proofReaderWriter = proofReaderWriter;
        this.cemProjectReaderWriter = cemProjectReaderWriter;
    }

    @Override
    public void startCreatingCsvs() throws IOException {
        System.out.println("Starting TimeSeriesCsvCreator\n");

        System.out.println("Reading teamscale data");
        List<TeamscaleData> teamscaleData = teamscaleDataReaderWriter.readAll();
        System.out.println("Finished\n");

        System.out.println("Combining data");
        List<CombinedData> combinedData = new ArrayList<>();
        for (TeamscaleData teamscaleDatum : teamscaleData) {
            String repoOwner = teamscaleDatum.repoOwner();
            String repoName = teamscaleDatum.repoName();
            
            try {
                Proof proof = proofReaderWriter.getElement(repoOwner, repoName).orElseThrow();
                CemProject cemProject = cemProjectReaderWriter.getElement(repoOwner, repoName).orElseThrow();
                GitHubData gitHubData = gitHubDataReaderWriter.getElement(repoOwner, repoName).orElseThrow();
                combinedData.add(dataCombiner.combineData(proof, teamscaleDatum, cemProject, gitHubData));
            } catch (NoSuchElementException e) {
                System.err.printf("Could not get data for %s.%s%n", repoOwner, repoName);
                e.printStackTrace();
                System.err.println("\n");
            }
        }
        System.out.println("Finished\n");

        List<CombinedData> treatedData = combinedData.stream().filter(TimeSeriesCsvCreator::isTreatment).toList();
        List<CombinedData> controlData = combinedData.stream().filter(TimeSeriesCsvCreator::isControl).toList();

        createCsvFiles(GroupType.TREATMENT, treatedData);
        createCsvFiles(GroupType.CONTROL, controlData);
    }

    private void createCsvFiles(GroupType groupType, List<CombinedData> combinedDataList) throws IOException {
        for (CombinedData combinedData : combinedDataList) {
            createCsvFiles(groupType, combinedData);
        }
    }

    private void createCsvFiles(GroupType groupType, CombinedData combinedData) throws IOException {
        String repoOwner = combinedData.repoOwner();
        String repoName = combinedData.repoName();

        //Ignore empty data sets
        if (combinedData.entries().isEmpty()) {
            System.out.printf("No entries found in %s.%s %n", repoOwner, repoName);
            return;
        }

        //create folder
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

    private static boolean isTreatment(CombinedData project) {
        return project.cemProject().groupType().equals(GroupType.TREATMENT);
    }

    private static boolean isControl(CombinedData project) {
        return project.cemProject().groupType().equals(GroupType.CONTROL);
    }
}
