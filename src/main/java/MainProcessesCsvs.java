import com.google.inject.Guice;
import com.google.inject.Injector;
import io.csv.CemPairCsvCreator;
import io.csv.TimeSeriesCsvCreator;
import util.GuiceModule;

import java.io.IOException;

public class MainProcessesCsvs {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        TimeSeriesCsvCreator timeSeriesCsvCreator = injector.getInstance(TimeSeriesCsvCreator.class);
        CemPairCsvCreator cemPairCsvCreator = injector.getInstance(CemPairCsvCreator.class);

        cemPairCsvCreator.startCreatingCsvs();
        timeSeriesCsvCreator.startCreatingCsvs();
    }
}
