package model.indicators;

import java.io.File;
import java.io.IOException;

public abstract class FileIndicator {

   public abstract boolean test(File file) throws IOException;

}
