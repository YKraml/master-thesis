package io.csv;

import io.json.GitHubRepositoryReaderWriter;
import jakarta.inject.Inject;
import model.writable.GitHubRepository;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static java.io.File.separator;

public class DataBasePopulationCsvCreator implements CsvCreator {

    private final GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter;

    @Inject
    public DataBasePopulationCsvCreator(GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter) {
        this.gitHubRepositoryReaderWriter = gitHubRepositoryReaderWriter;
    }

    @Override
    public void startCreatingCsvs() throws IOException {

        List<GitHubRepository> gitHubRepositories = gitHubRepositoryReaderWriter.readAll();

        String folderName = "mixed.effects.models"
                + separator + "data"
                + separator + Constants.CURRENT_LANGUAGE.name().toLowerCase()
                + separator + "database_population";
        new File(folderName).mkdirs();
        File dataFile = new File(folderName + separator + "data.csv");
        if (!dataFile.createNewFile()) {
            return;
        }

        try (PrintWriter pw = new PrintWriter(dataFile)) {
            pw.println("id" + "," +
                    "repoOwner" + "," +
                    "repoName" + "," +
                    "numberOfStars" + "," +
                    "size" + "," +
                    "defaultBranch" + "," +
                    "defaultBranch");

            for (GitHubRepository repository : gitHubRepositories) {
                String id = repository.repoOwner() + "." + repository.repoName();
                String repoOwner = repository.repoOwner();
                String repoName = repository.repoName();
                int numberOfStars = repository.numberOfStars();
                int size = repository.size();
                String defaultBranch = repository.defaultBranch();

                pw.println(id + "," +
                        repoOwner + "," +
                        repoName + "," +
                        numberOfStars + "," +
                        size + "," +
                        defaultBranch);
            }

        }

    }
}
