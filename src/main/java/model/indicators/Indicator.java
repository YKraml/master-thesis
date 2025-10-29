package model.indicators;

import java.io.File;
import java.io.IOException;

public abstract class Indicator {

   public abstract boolean test(File file) throws IOException;

}
