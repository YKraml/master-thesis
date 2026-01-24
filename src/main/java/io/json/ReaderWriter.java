package io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import model.writable.Writable;
import org.apache.commons.io.FileUtils;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ReaderWriter<T extends Writable> {

    private final Class<T> clazz;

    protected ReaderWriter(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void write(T element) throws IOException {
        File file = createFile(element.repoOwner(), element.repoName());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, element);
    }

    public List<T> readAll() throws IOException {
        List<T> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(getFolder(), new String[]{".json"}, false)) {
            try {
                list.add(new ObjectMapper().readValue(file, clazz));
            } catch (MismatchedInputException e) {
                e.printStackTrace();
                file.delete();
            }
        }
        return list;
    }

    public void deleteFolder() throws IOException {
        FileUtils.deleteDirectory(getFolder());
    }

    public Optional<T> read(String repoOwner, String repoName) throws IOException {
        File proofs = getFile(repoOwner, repoName);
        if (!proofs.exists()) {
            return Optional.empty();
        }
        return Optional.ofNullable(new ObjectMapper().readValue(proofs, clazz));
    }

    private File getFolder() {
        return new File(getCompleteFolderName());
    }

    private File getFile(String repoOwner, String repoName) {
        return new File(getCompleteFolderName() + File.separator + repoOwner + "." + repoName + ".json");
    }

    private File createFile(String repoOwner, String repoName) throws IOException {
        File file = getFile(repoOwner, repoName);
        return createFile(file);
    }

    private static File createFile(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private String getCompleteFolderName() {
        return "temp" + File.separator + getFolderName() + File.separator + Constants.CURRENT_LANGUAGE.name().toLowerCase();
    }

    protected abstract String getFolderName();
}
