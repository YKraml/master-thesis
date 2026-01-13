package model;

public interface ProjectWritable extends IdWritable {

    String repoOwner();

    String repoName();

    @Override
    default String id() {
        return repoOwner() + "." + repoName();
    }
}
