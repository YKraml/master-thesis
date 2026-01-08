package io.csv;

import io.json.CombinedDataReaderWriter;
import jakarta.inject.Inject;
import model.CombinedData;
import model.Linter;
import model.Proof;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CsvCreator {

    private static final List<Integer> DAYS = getDays();
    private final CombinedDataReaderWriter combinedDataReaderWriter;

    @Inject
    public CsvCreator(CombinedDataReaderWriter combinedDataReaderWriter) {
        this.combinedDataReaderWriter = combinedDataReaderWriter;
    }

    public void startCreatingCsvs() throws IOException {
        List<LinterTimeData> linterTimeDataList = new ArrayList<>();
        for (CombinedData combinedData : combinedDataReaderWriter.readAll()) {
            Proof proof = combinedData.proof();

            for (Proof.LinterWithProof linterWithProof : proof.linters()) {
                Linter linter = Linter.valueOf(linterWithProof.linterName());
                int introductionTime = linterWithProof.introductionTime();
                linterTimeDataList.add(new LinterTimeData(linter, introductionTime, combinedData, proof));
            }
        }

        Map<Linter, List<LinterTimeData>> bucketOne = new HashMap<>();

        for (Linter linter : Linter.values()) {
            bucketOne.put(linter, new ArrayList<>());
        }

        for (LinterTimeData linterTimeData : linterTimeDataList) {
            Linter linter = linterTimeData.linter();
            bucketOne.get(linter).add(linterTimeData);
        }

        //save bucket
        saveBucket(bucketOne, "bucket_one");
    }

    private void saveBucket(Map<Linter, List<LinterTimeData>> bucket, String bucketName) throws IOException {
        List<LinterTimeData> all = bucket.entrySet().stream().flatMap(s -> s.getValue().stream()).toList();
        createCsvFile(bucketName, "ALL", all);

        for (Map.Entry<Linter, List<LinterTimeData>> linterListEntry : bucket.entrySet()) {
            createCsvFile(bucketName, linterListEntry.getKey().name(), linterListEntry.getValue());
        }
    }

    private void createCsvFile(String bucketName, String linterName, List<LinterTimeData> linterTimeData) throws IOException {
        //Ignore empty data sets
        if (linterTimeData.isEmpty()) {
            System.out.println(bucketName + "/" + linterName + " - Entries are empty. Throw out!");
            return;
        }

        //create file
        String folderName = "mixed.effects.models" + File.separator + "data" + File.separator + bucketName;
        new File(folderName).mkdirs();
        File dataFile = new File(folderName + File.separator + linterName + ".csv");
        dataFile.createNewFile();

        //Write into csv file
        System.out.println("Writing file: " + bucketName + "/" + dataFile.getName());
        try (PrintWriter pw = new PrintWriter(dataFile)) {
            pw.println("id," + CombinedData.Entry.csvHeader() + ",intervention,t,t_pre,t_post,relative_introduction_time");

            for (LinterTimeData date : linterTimeData) {
                int introductionTime = date.time();
                double introduction_time_relative_to_first_commit_time = (introductionTime - date.proof.firstCommitTime());
                if (introduction_time_relative_to_first_commit_time < 0) {
                    System.out.println();
                }

                CombinedData combinedData = date.result();

                NavigableMap<Integer, CombinedData.Entry> entryByTime = new TreeMap<>();
                for (CombinedData.Entry entry : combinedData.entries()) {
                    entryByTime.put(entry.time(), entry);
                }

                for (int point : DAYS) {

                    int pointInSeconds = point * 24 * 60 * 60;
                    int time = introductionTime + pointInSeconds;
                    Map.Entry<Integer, CombinedData.Entry> floorEntry = entryByTime.floorEntry(time);
                    if (floorEntry == null) {
                        continue;
                    }
                    CombinedData.Entry entry = floorEntry.getValue();

                    String id = combinedData.repoOwner() + "." + combinedData.repoName() + "." + date.linter().name();
                    int intervention = point < 0 ? 0 : 1;
                    int t = point;
                    int t_pre = t * (1 - intervention);
                    int t_post = t * intervention;

                    pw.println(id + "," + entry.csvLine() + "," + intervention + "," + t + "," + t_pre + "," + t_post + "," + introduction_time_relative_to_first_commit_time);
                }
            }
        }
    }

    private static List<Integer> getDays() {
        List<Integer> points = new ArrayList<>();

        int start = -1 * 7 * 52 * 2;
        int end = 7 * 52 * 2;
        int jump = 7;

        for (int i = start; i <= end; i = i + jump) {
            points.add(i);
        }

        return points;
    }

    private record LinterTimeData(Linter linter, int time, CombinedData result, Proof proof) {
    }

}
