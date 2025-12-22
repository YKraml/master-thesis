import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.DataCollector;
import logic.StateHandler;
import util.GuiceModule;

public class MainCollectData {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        StateHandler stateHandler = injector.getInstance(StateHandler.class);
        DataCollector dataCollector = injector.getInstance(DataCollector.class);

        stateHandler.start();
        dataCollector.startCollectingData();
        stateHandler.end();
    }
}
