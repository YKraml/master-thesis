package logic;

import model.Language;
import model.Linter;
import model.Proof;
import model.indicators.Indicator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LinterFileFinder {

    public Map<Linter, Proof> searchForLinters(File file) {
        HashMap<Linter, Proof> linters = new HashMap<>();

        for (Linter linter : Linter.getLinterOfLanguage(Language.JAVA)) {
            for (Indicator indicator : linter.getIndicators()) {
                boolean test;
                try {
                    test = indicator.test(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (test) {
                    linters.put(linter, new Proof(file.getName()));
                    break;
                }
            }
        }

        return linters;
    }

}
