import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static util.Constants.SECONDS_IN_ONE_MONTH;
import static util.Constants.SECONDS_IN_TWO_YEARS;

public class MainProcessesCsvs {

    public static void main(String[] args) throws IOException {

        File[] fileList = new File("temp" + File.separator + "all").listFiles();
        if (fileList == null) {
            throw new NullPointerException();
        }

        Set<Linter> linters = new HashSet<>();
        List<LinterTimeResult> linterTimeResultList = new ArrayList<>();

        for (File file : fileList) {
            try {
                ProofWithResult proofWithResult = new ObjectMapper().readValue(file, ProofWithResult.class);

                Proof proof = proofWithResult.proof();
                Result result = proofWithResult.result();

                for (Proof.LinterWithProof linterWithProof : proof.linters()) {
                    Linter linter = Linter.valueOf(linterWithProof.linterName());
                    int introductionTime = linterWithProof.introductionTime();
                    linters.add(linter);
                    linterTimeResultList.add(new LinterTimeResult(linter, introductionTime, result));
                }
            } catch (JsonMappingException jsonMappingException) {
                file.delete();
                throw jsonMappingException;
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

    private static void createCsvFile(String bucketName, Linter linter, List<LinterTimeResult> linterTimeResults) throws IOException {
        for (LinterTimeResult linterTimeResult : linterTimeResults) {
            if (!linterTimeResult.linter().equals(linter)) {
                throw new RuntimeException();
            }
        }

        String folderName = "mixed.effects.models" + File.separator + "data";
        new File(folderName).delete();
        new File(folderName).mkdirs();
        File dataFile = new File(folderName + File.separator + bucketName + "." + linter.name() + ".csv");
        dataFile.createNewFile();

        System.out.println("Writing file: " + dataFile.getName());

        try (PrintWriter pw = new PrintWriter(dataFile)) {
            pw.println("id," + Result.Entry.csvHeader() + ",intervention,t_pre,t_post");

            for (LinterTimeResult linterTimeResult : linterTimeResults) {
                int introductionTime = linterTimeResult.time();
                Result result = linterTimeResult.result();

                for (Result.Entry entry : result.entries()) {
                    String id = result.repoOwner() + "." + result.repoName();
                    int intervention = entry.time() < introductionTime ? 0 : 1;
                    double relativeTime = ((double) (entry.time() - introductionTime)) / 60 / 60 / 24 / 365;
                    double t_pre = relativeTime * (1 - intervention);
                    double t_post = relativeTime * intervention;
                    pw.println(id + "," + entry.csvLine() + "," + intervention + "," + t_pre + "," + t_post);
                }
            }
        }
    }

    private static Map<Linter, List<LinterTimeResult>> cutEndsOff(Map<Linter, List<LinterTimeResult>> linterTimeResultMap) {
        Map<Linter, List<LinterTimeResult>> cutted = new HashMap<>();
        for (Linter linter : linterTimeResultMap.keySet()) {
            cutted.put(linter, new ArrayList<>());
            List<LinterTimeResult> linterTimeResults = linterTimeResultMap.get(linter);

            for (LinterTimeResult linterTimeResult : linterTimeResults) {
                int introductionTime = linterTimeResult.time();
                Result result = linterTimeResult.result();

                List<Result.Entry> entriesCutted = result.entries().stream().filter(entry -> entry.time() > introductionTime - SECONDS_IN_TWO_YEARS && entry.time() < introductionTime + SECONDS_IN_TWO_YEARS).collect(Collectors.toList());

                if (entriesCutted.isEmpty()) {
                    System.out.println(result.repoOwner() + "/" + result.repoName() + " - Entries are empty. Throw out!");
                } else {
                    Result cutetdResult = new Result(result.repoOwner(), result.repoName(), entriesCutted);
                    cutted.get(linter).add(new LinterTimeResult(linter, introductionTime, cutetdResult));
                }
            }
        }

        //Assert time diff
        for (List<LinterTimeResult> value : cutted.values()) {
            for (LinterTimeResult linterTimeResult : value) {
                List<Result.Entry> entries = linterTimeResult.result().entries();
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
