package io.csv;

import io.json.CombinedDataReaderWriter;
import jakarta.inject.Inject;
import model.CemProject.GroupType;
import model.CombinedData;
import model.Proof.LinterWithProof;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.IntStream;

import static java.io.File.separator;
import static model.CemProject.Commit;

public class TimeSeriesCsvCreator implements CsvCreator {

    private static final int[] DAYS = getDays();
    private final CombinedDataReaderWriter combinedDataReaderWriter;

    @Inject
    public TimeSeriesCsvCreator(CombinedDataReaderWriter combinedDataReaderWriter) {
        this.combinedDataReaderWriter = combinedDataReaderWriter;
    }

    @Override
    public void startCreatingCsvs() throws IOException {
        System.out.println("Starting TimeSeriesCsvCreator");

        List<CombinedData> combinedData = combinedDataReaderWriter.readAll();

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
        Map<Commit, Optional<LinterWithProof>> linterCommitsWithProof = new HashMap<>();
        for (Commit linterCommit : combinedData.cemProject().linterCommits()) {
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
        for (Map.Entry<Commit, Optional<LinterWithProof>> entry : linterCommitsWithProof.entrySet()) {
            createFile(combinedData, entry, i, folderName);
            i++;
        }

    }

    private static void createFile(CombinedData combinedData, Map.Entry<Commit, Optional<LinterWithProof>> linterCommitWithProof, int i, String folderName) throws IOException {

        // Extracting some infos from the given data
        String repoOwner = combinedData.repoOwner();
        String repoName = combinedData.repoName();

        Commit linterCommit = linterCommitWithProof.getKey();
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
