package model;

import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.io.IOException;

public abstract class FileIndicator {

   public abstract boolean test(File file) throws IOException;

   public abstract boolean test(DiffEntry.ChangeType changeType, String diffText);

}
