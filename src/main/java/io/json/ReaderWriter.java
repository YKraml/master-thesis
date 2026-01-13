package io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import model.IdWritable;
import model.ProjectWritable;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ReaderWriter<T extends IdWritable> {

    private final Class<T> clazz;

    protected ReaderWriter(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected File createFile(String repoOwner, String repoName) throws IOException {
        File file = getFile(repoOwner, repoName);
        return createFile(file);
    }

    protected File createFile(String id) throws IOException {
        File file = getFile(id);
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

    public void write(T element) throws IOException {
        File proofFile = null;
        switch (element) {
            case ProjectWritable projectWritable ->
                    proofFile = createFile(projectWritable.repoOwner(), projectWritable.repoName());
            case IdWritable idWritable -> proofFile = createFile(idWritable.id());
            case null -> {
            }
        }

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

    public abstract File getFile(String id);

    protected abstract String getFolderName();
}
