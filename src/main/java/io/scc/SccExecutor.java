package io.scc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.inject.Inject;
import util.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class SccExecutor {

    @Inject
    public SccExecutor() {

    }

    public List<SccObject> execute(String folder) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(Constants.SCC, "-f", "json");
            builder.directory(new File(folder));

            Process process = builder.start();

            String jsonOutput;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                jsonOutput = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("SCC exited with error code: " + exitCode);
                return null;
            }

            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            return objectMapper.readValue(jsonOutput, typeFactory.constructCollectionType(List.class, SccObject.class));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
