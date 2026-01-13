package model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record CombinedData(String repoOwner, String repoName, Proof proof,
                           List<Entry> entries) implements ProjectWritable {

    public record Entry(
            int time,
            int files,
            int linesOfCode,
            int sourceLinesOfCode,
            int longFiles,
            int longMethods,
            int deepNesting,
            int findings,
            int comprehensibility,
            int correctness,
            int documentation,
            int efficiency,
            int errorHandling,
            int redundancy,
            int structure,
            int security,
            double comprehensibilityDensity,
            double correctnessDensity,
            double documentationDensity,
            double efficiencyDensity,
            double errorHandlingDensity,
            double redundancyDensity,
            double structureDensity,
            double securityDensity,
            double longFilesDensity,
            double longMethodDensity,
            double deepNestingDensity,
            double findingsDensity,
            double cloneCoverage,
            double averageFileLoc,
            double averageFileSloc
    ) {

        public static String csvHeader() {
            return Arrays.stream(Entry.class.getRecordComponents())
                    .map(RecordComponent::getName)
                    .collect(Collectors.joining(","));
        }

        public static Entry getDefault() {
            RecordComponent[] components = Entry.class.getRecordComponents();
            Object[] args = new Object[components.length];
            Arrays.fill(args, 0);

            try {
                return (Entry) Entry.class.getDeclaredConstructors()[0].newInstance(args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        public String csvLine() {
            try {
                return Arrays.stream(this.getClass().getRecordComponents())
                        .map(recordComponent -> {
                            try {
                                return String.valueOf(recordComponent.getAccessor().invoke(this));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.joining(","));
            } catch (Exception e) {
                throw new RuntimeException("Error creating CSV string", e);
            }
        }
    }
}
