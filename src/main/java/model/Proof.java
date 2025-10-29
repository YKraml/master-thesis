package model;

public class Proof {

    private final String fileName;

    public Proof(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "Proof{" +
                "file=" + fileName +
                '}';
    }
}
