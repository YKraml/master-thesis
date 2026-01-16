package model;

public record CemProjectPair(CemProject treated, CemProject control) implements ProjectWritable {
    @Override
    public String repoOwner() {
        return "(%s.%s)".formatted(treated.repoOwner(), treated.repoName());
    }

    @Override
    public String repoName() {
        return "(%s.%s)".formatted(control.repoOwner(), control.repoName());
    }
}
