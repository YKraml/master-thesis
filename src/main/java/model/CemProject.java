package model;

public record CemProject(String id,
                         GroupType groupType,
                         int numberOfStars,
                         double ageAtLinterIntro,
                         String creationYear) implements IdWritable {

    public enum GroupType {
        TREATMENT,
        CONTROL
    }
}
