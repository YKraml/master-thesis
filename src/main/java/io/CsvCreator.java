package io;

import jakarta.inject.Inject;
import model.CombinedData;
import model.Linter;
import model.LinterTimeResult;
import model.Proof;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static util.Constants.SECONDS_IN_ONE_MONTH;
import static util.Constants.SECONDS_IN_TWO_YEARS;

public class CsvCreator {

    private final CombinedDataReaderWriter combinedDataReaderWriter;

    @Inject
    public CsvCreator(CombinedDataReaderWriter combinedDataReaderWriter) {
        this.combinedDataReaderWriter = combinedDataReaderWriter;
    }

    public void startCreatingCsvs() throws IOException {
        File[] fileList = new File("temp" + File.separator + "all").listFiles();
        if (fileList == null) {
            throw new NullPointerException();
        }

        List<LinterTimeResult> linterTimeResultList = new ArrayList<>();
        for (CombinedData combinedData : combinedDataReaderWriter.readAll()) {
            Proof proof = combinedData.proof();

            for (Proof.LinterWithProof linterWithProof : proof.linters()) {
                Linter linter = Linter.valueOf(linterWithProof.linterName());
                int introductionTime = linterWithProof.introductionTime();
                linterTimeResultList.add(new LinterTimeResult(linter, introductionTime, combinedData));
            }
        }

        Map<Linter, List<LinterTimeResult>> bucketOne = new HashMap<>();
        Map<Linter, List<LinterTimeResult>> bucketTwo = new HashMap<>();
        Map<Linter, List<LinterTimeResult>> bucketThree = new HashMap<>();

        for (Linter linter : Linter.values()) {
            bucketOne.put(linter, new ArrayList<>());
            bucketTwo.put(linter, new ArrayList<>());
            bucketThree.put(linter, new ArrayList<>());
        }

        for (LinterTimeResult linterTimeResult : linterTimeResultList) {
            int firstTime = linterTimeResult.result().entries().getFirst().time();
            int introductionTime = linterTimeResult.time();
            Linter linter = linterTimeResult.linter();

            if (introductionTime - firstTime > SECONDS_IN_TWO_YEARS) {
                bucketOne.get(linter).add(linterTimeResult);
            } else if (introductionTime - firstTime > SECONDS_IN_ONE_MONTH) {
                bucketTwo.get(linter).add(linterTimeResult);
            } else {
                bucketThree.get(linter).add(linterTimeResult);
            }
        }

        bucketOne = cutEndsOff(bucketOne);
        bucketTwo = cutEndsOff(bucketTwo);
        bucketThree = cutEndsOff(bucketThree);

        for (Map.Entry<Linter, List<LinterTimeResult>> linterListEntry : bucketOne.entrySet()) {
            createCsvFile("bucket_one", linterListEntry.getKey(), linterListEntry.getValue());
        }
        for (Map.Entry<Linter, List<LinterTimeResult>> linterListEntry : bucketTwo.entrySet()) {
            createCsvFile("bucket_two", linterListEntry.getKey(), linterListEntry.getValue());
        }
        for (Map.Entry<Linter, List<LinterTimeResult>> linterListEntry : bucketThree.entrySet()) {
            createCsvFile("bucket_three", linterListEntry.getKey(), linterListEntry.getValue());
        }

        System.out.println();
    }

    private void createCsvFile(String bucketName, Linter linter, List<LinterTimeResult> linterTimeResults) throws IOException {
        for (LinterTimeResult linterTimeResult : linterTimeResults) {
            if (!linterTimeResult.linter().equals(linter)) {
                throw new RuntimeException();
            }
        }

        if (linterTimeResults.isEmpty()) {
            System.out.println(bucketName + "/" + linter + " - Entries are empty. Throw out!");
            return;
        }

        String folderName = "mixed.effects.models" + File.separator + "data" + File.separator + bucketName;
        new File(folderName).delete();
        new File(folderName).mkdirs();
        File dataFile = new File(folderName + File.separator + linter.name() + ".csv");
        dataFile.createNewFile();

        System.out.println("Writing file: " + bucketName + "/" + dataFile.getName());

        try (PrintWriter pw = new PrintWriter(dataFile)) {
            pw.println("id," + CombinedData.Entry.csvHeader() + ",intervention,t_pre,t_post");

            for (LinterTimeResult linterTimeResult : linterTimeResults) {
                int introductionTime = linterTimeResult.time();
                CombinedData result = linterTimeResult.result();

                NavigableMap<Integer, CombinedData.Entry> entryByTime = new TreeMap<>();
                for (CombinedData.Entry entry : result.entries()) {
                    entryByTime.put(entry.time(), entry);
                }

                //int[] points = getPoints();
                //for (int point : points) {
                //    int time = introductionTime + point * 24 * 60 * 60;
                //    Result.Entry entry = getNearestEntry(entryByTime, time);
                //    String id = result.repoOwner() + "." + result.repoName();
                //    int intervention = point < 0 ? 0 : 1;
                //    int t_pre = point * (1 - intervention);
                //    int t_post = point * intervention;
                //    pw.println(id + "," + entry.csvLine() + "," + intervention + "," + t_pre + "," + t_post);
                //}

                for (CombinedData.Entry entry : result.entries()) {
                    String id = result.repoOwner() + "." + result.repoName();
                    int intervention = entry.time() < introductionTime ? 0 : 1;
                    double relativeTime = ((double) (entry.time() - introductionTime)) / 60 / 60 / 24;
                    double t_pre = relativeTime * (1 - intervention);
                    double t_post = relativeTime * intervention;
                    pw.println(id + "," + entry.csvLine() + "," + intervention + "," + t_pre + "," + t_post);
                }
            }
        }
    }

    private CombinedData.Entry getNearestEntry(NavigableMap<Integer, CombinedData.Entry> map, int time) {
        Map.Entry<Integer, CombinedData.Entry> floor = map.floorEntry(time);
        Map.Entry<Integer, CombinedData.Entry> ceil = map.ceilingEntry(time);

        if (floor != null) {
            return floor.getValue();
        }
        if (ceil != null) {
            return ceil.getValue();
        }

        throw new RuntimeException("No floor and no ceil was found");
    }

    private int[] getPoints() {
        int[] points = new int[52 * 2 * 2];

        for (int i = 0; i < points.length; i++) {
            points[i] = i * 7 - 2 * 52 * 7;
        }

        return points;
    }

    private Map<Linter, List<LinterTimeResult>> cutEndsOff(Map<Linter, List<LinterTimeResult>> linterTimeResultMap) {
        Map<Linter, List<LinterTimeResult>> cutted = new HashMap<>();
        for (Linter linter : linterTimeResultMap.keySet()) {
            cutted.put(linter, new ArrayList<>());
            List<LinterTimeResult> linterTimeResults = linterTimeResultMap.get(linter);

            for (LinterTimeResult linterTimeResult : linterTimeResults) {
                int introductionTime = linterTimeResult.time();
                CombinedData result = linterTimeResult.result();

                List<CombinedData.Entry> entriesCutted = result.entries().stream().filter(entry -> entry.time() > introductionTime - SECONDS_IN_TWO_YEARS && entry.time() < introductionTime + SECONDS_IN_TWO_YEARS).collect(Collectors.toList());

                if (entriesCutted.isEmpty()) {
                    System.out.println(result.repoOwner() + "/" + result.repoName() + " - Entries are empty. Throw out!");
                } else {
                    CombinedData cutetdResult = new CombinedData(result.repoOwner(), result.repoName(), result.proof(), entriesCutted);
                    cutted.get(linter).add(new LinterTimeResult(linter, introductionTime, cutetdResult));
                }
            }
        }

        //Assert time diff
        for (List<LinterTimeResult> value : cutted.values()) {
            for (LinterTimeResult linterTimeResult : value) {
                List<CombinedData.Entry> entries = linterTimeResult.result().entries();
                int firstTime = entries.getFirst().time();
                int lastTime = entries.getLast().time();
                int diff = lastTime - firstTime;
                if (diff / 60d / 60 / 24 / 365 > 4) {
                    throw new RuntimeException();
                }
            }
        }

        return cutted;
    }
}
