package runnables;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.csv.CemPairCsvCreator;
import io.csv.CemProjectCsvCreator;
import io.csv.DataBasePopulationCsvCreator;
import io.csv.TimeSeriesCsvCreator;
import model.linting.Language;
import util.Constants;
import util.GuiceModule;

import java.io.IOException;

public class MainProcessesCsvs {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        TimeSeriesCsvCreator timeSeriesCsvCreator = injector.getInstance(TimeSeriesCsvCreator.class);
        CemPairCsvCreator cemPairCsvCreator = injector.getInstance(CemPairCsvCreator.class);
        DataBasePopulationCsvCreator dataBasePopulationCsvCreator = injector.getInstance(DataBasePopulationCsvCreator.class);
        CemProjectCsvCreator cemProjectCsvCreator = injector.getInstance(CemProjectCsvCreator.class);

        Constants.CURRENT_LANGUAGE = Language.JAVA;
        cemProjectCsvCreator.startCreatingCsvs();
        dataBasePopulationCsvCreator.startCreatingCsvs();
        cemPairCsvCreator.startCreatingCsvs();
        timeSeriesCsvCreator.startCreatingCsvs();

        Constants.CURRENT_LANGUAGE = Language.PYTHON;
        cemProjectCsvCreator.startCreatingCsvs();
        dataBasePopulationCsvCreator.startCreatingCsvs();
        cemPairCsvCreator.startCreatingCsvs();
        timeSeriesCsvCreator.startCreatingCsvs();

        Constants.CURRENT_LANGUAGE = Language.CPP;
        cemProjectCsvCreator.startCreatingCsvs();
        dataBasePopulationCsvCreator.startCreatingCsvs();
        cemPairCsvCreator.startCreatingCsvs();
        timeSeriesCsvCreator.startCreatingCsvs();
    }
}
