import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.CsvCreator;
import util.GuiceModule;

import java.io.IOException;

public class MainProcessesCsvs {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        CsvCreator csvCreator = injector.getInstance(CsvCreator.class);

        csvCreator.startCreatingCsvs();
    }
}
