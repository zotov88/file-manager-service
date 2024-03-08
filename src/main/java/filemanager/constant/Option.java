package filemanager.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Option {

    public final int REMOVE_SUBSTRING_IN_FILE_AND_DIR = 1;
    public final int REMOVE_FILE_WITH_SUBSTRING = 2;
    public final int REMOVE_DIR_WITH_SUBSTRING = 3;
    public final int MOVE_FILE = 4;
    public final int COPY_FILE = 5;
    public final int REMOVE_FILE_WITH_EXTENSION = 6;
    public final int EXIT = 7;
}
