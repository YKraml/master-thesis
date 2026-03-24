import com.google.inject.Guice;
import com.google.inject.Injector;
import io.csv.CemPairCsvCreator;
import io.csv.DataBasePopulationCsvCreator;
import io.csv.TimeSeriesCsvCreator;
import util.GuiceModule;

import java.io.IOException;

public class MainProcessesCsvs {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        TimeSeriesCsvCreator timeSeriesCsvCreator = injector.getInstance(TimeSeriesCsvCreator.class);
        CemPairCsvCreator cemPairCsvCreator = injector.getInstance(CemPairCsvCreator.class);
        DataBasePopulationCsvCreator dataBasePopulationCsvCreator = injector.getInstance(DataBasePopulationCsvCreator.class);

        dataBasePopulationCsvCreator.startCreatingCsvs();
        cemPairCsvCreator.startCreatingCsvs();
        timeSeriesCsvCreator.startCreatingCsvs();

    }
}
