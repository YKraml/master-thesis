package util;

import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;
import java.util.*;

public class State {

    private static final State INSTANCE = new State();

    private final Map<Pair<String, String>, String> state;
    private final Map<Pair<String, String>, Integer> indices;

    private int index = 1;

    private State() {
        state = new LinkedHashMap<>();
        indices = new LinkedHashMap<>();
    }

    public static State getInstance() {
        return INSTANCE;
    }

    public void addToState(Pair<String, String> key, String value) {
        synchronized (state) {
            state.put(key, value);
            if (!indices.containsKey(key)) {
                indices.put(key, index++);
            }
        }
    }

    public void removeFromState(Pair<String, String> key) {
        synchronized (state) {
            state.put(key, "Finished");
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (state) {
                    state.remove(key);
                    indices.remove(key);
                }
            }
        }, 1500);
    }

    public void printState() {
        synchronized (state) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
            String currentTime = formatter.format(new Date());

            StringBuilder output = new StringBuilder("\n");
            output.append("Current state at: ").append(currentTime).append("\n");

            for (Map.Entry<Pair<String, String>, String> entry : state.entrySet()) {
                String repo = entry.getKey().getKey() + "/" + entry.getKey().getValue();
                String state = entry.getValue();
                String i1 = String.valueOf(indices.get(entry.getKey()));
                output.append("%4s. %-40s %40s".formatted(i1, repo.substring(0, Math.min(repo.length(), 40)), state.substring(0, Math.min(state.length(), 40))));
                output.append("\n");
            }

            System.out.println(output);
        }
    }
}
