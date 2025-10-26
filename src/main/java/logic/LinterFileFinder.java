package logic;

import model.Linter;
import model.Proof;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LinterFileFinder {

    public Map<Linter, Proof> searchForLinters(File file) {
        HashMap<Linter, Proof> linters = new HashMap<>();

        if(file.getName().equals("qodana.yaml")){
            linters.put(Linter.QODANA, new Proof(file));
        }

        if(file.getName().equals("checkstyle-idea.xml")){
            linters.put(Linter.CHECKSTYLE, new Proof(file));
        }

        return linters;
    }

}
