import com.google.inject.Guice;
import com.google.inject.Injector;
import io.CsvReaderWriter;
import io.NormalisedResultReaderWriter;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;
import util.GuiceModule;

import java.io.IOException;

public class MainCsvCreation {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        NormalisedResultReaderWriter resultReaderWriter = injector.getInstance(NormalisedResultReaderWriter.class);
        CsvReaderWriter csvReaderWriter = injector.getInstance(CsvReaderWriter.class);

        System.out.println("Start writing CSVs");
        for (Pair<String, Result> entry : resultReaderWriter.readAllNormalisedResults()) {
            csvReaderWriter.writeResultAsCsv(entry.getRight(), entry.getLeft());
        }
        System.out.println("Finished writing CSVs");
    }

}
