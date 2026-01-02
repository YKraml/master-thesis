package io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import model.Writable;
import org.apache.commons.io.FileUtils;

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

    protected File createFile(String repoOwner, String repoName) throws IOException {
        File file = getFile(repoOwner, repoName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public void write(T element) throws IOException {
        File proofFile = createFile(element.repoOwner(), element.repoName());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(proofFile, element);
    }

    public List<T> readAll() throws IOException {
        List<T> list = new ArrayList<>();
        for (File file : FileUtils.listFiles(new File(getFolderName()), new String[]{".json"}, false)) {
            try {
                list.add(new ObjectMapper().readValue(file, clazz));
            } catch (MismatchedInputException e) {
                e.printStackTrace();
                file.delete();
            }
        }
        return list;
    }

    public Optional<T> getElement(String repoOwner, String repoName) throws IOException {
        File proofs = getFile(repoOwner, repoName);
        if (!proofs.exists()) {
            return Optional.empty();
        }
        return Optional.ofNullable(new ObjectMapper().readValue(proofs, clazz));
    }

    public abstract File getFile(String repoOwner, String repoName);

    protected abstract String getFolderName();
}
