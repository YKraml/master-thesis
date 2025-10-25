package model;

import java.io.File;

public class Proof {

    private final File file;

    public Proof(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Proof{" +
                "file=" + file +
                '}';
    }
}
