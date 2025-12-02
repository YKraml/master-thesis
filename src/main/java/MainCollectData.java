import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.DataCollector;
import logic.State;
import util.GuiceModule;

public class MainCollectData {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        State state = injector.getInstance(State.class);
        DataCollector dataCollector = injector.getInstance(DataCollector.class);

        state.start();
        dataCollector.startCollectingData();
        state.end();
    }
}
