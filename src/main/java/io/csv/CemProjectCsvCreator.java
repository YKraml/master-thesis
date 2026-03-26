package io.csv;

import io.json.CemProjectReaderWriter;
import jakarta.inject.Inject;
import model.writable.CemProject;
import util.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CemProjectCsvCreator implements CsvCreator {

    private final CemProjectReaderWriter cemProjectReaderWriter;

    @Inject
    CemProjectCsvCreator(CemProjectReaderWriter cemProjectReaderWriter) {
        this.cemProjectReaderWriter = cemProjectReaderWriter;
    }

    @Override
    public void startCreatingCsvs() throws IOException {
        System.out.println("Starting CemProjectCsvCreator");

        List<CemProject> cemProjects = cemProjectReaderWriter.readAll();

        List<CemProject> potential_treated = new ArrayList<>();
        List<CemProject> potential_control = new ArrayList<>();
        for (CemProject cemProject : cemProjects) {
            if (cemProject.groupType().equals(CemProject.GroupType.CONTROL)) {
                potential_control.add(cemProject);
            } else {
                potential_treated.add(cemProject);
            }
        }

        createFile("potential_treated", potential_treated);
        createFile("potential_control", potential_control);

        System.out.println("Finished CemProjectCsvCreator");
    }

    private void createFile(String fileName, List<CemProject> cemProjects) throws IOException {
        String folderName = "mixed.effects.models"
                + File.separator + "data"
                + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase()
                + File.separator + "cem_projects";

        File directory = new File(folderName);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + folderName);
        }

        File dataFile = new File(directory, fileName + ".csv");

        // Use BufferedWriter for high-performance writing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile), 1 << 16)) { // 64KB buffer

            // Header
            writer.write("id,repoOwner,repoName,groupType,numberOfStars,creationYear,defaultBranch,numberOfCommits,loc,sloc,numberOfContributors");
            writer.newLine();

            StringBuilder sb = new StringBuilder(256);

            for (CemProject cemProject : cemProjects) {
                sb.setLength(0); // reuse buffer (important!)

                String repoOwner = cemProject.repoOwner();
                String repoName = cemProject.repoName();

                sb.append(repoOwner).append('.').append(repoName).append(',')
                        .append(repoOwner).append(',')
                        .append(repoName).append(',')
                        .append(cemProject.groupType()).append(',')
                        .append(cemProject.numberOfStars()).append(',')
                        .append(cemProject.creationYear()).append(',')
                        .append(cemProject.defaultBranch()).append(',')
                        .append(cemProject.numberOfCommits()).append(',')
                        .append(cemProject.loc()).append(',')
                        .append(cemProject.sloc()).append(',')
                        .append(cemProject.numberOfContributors());

                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }
}

