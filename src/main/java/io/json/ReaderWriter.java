package io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import model.ProjectWritable;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class ReaderWriter<T extends ProjectWritable> {

    private final Class<T> clazz;

    protected ReaderWriter(Class<T> clazz) {
        this.clazz = clazz;
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

    public Iterator<T> iterator() {
        Iterator<File> fileIterator = FileUtils.listFiles(
                new File(getFolderName()),
                new String[]{"json"},
                false
        ).iterator();

        ObjectMapper mapper = new ObjectMapper();

        return new Iterator<>() {
            private T nextElement = null;

            @Override
            public boolean hasNext() {
                if (nextElement != null) {
                    return true;
                }
                findNext();
                return nextElement != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T result = nextElement;
                nextElement = null;
                return result;
            }

            private void findNext() {
                while (nextElement == null && fileIterator.hasNext()) {
                    File file = fileIterator.next();
                    try {
                        nextElement = mapper.readValue(file, clazz);
                    } catch (MismatchedInputException e) {
                        e.printStackTrace();
                        file.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public Optional<T> getElement(String repoOwner, String repoName) throws IOException {
        File proofs = getFile(repoOwner, repoName);
        if (!proofs.exists()) {
            return Optional.empty();
        }
        return Optional.ofNullable(new ObjectMapper().readValue(proofs, clazz));
    }

    public abstract File getFile(String repoOwner, String repoName);

    protected File createFile(String repoOwner, String repoName) throws IOException {
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

    protected abstract String getFolderName();
}
