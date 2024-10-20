package filemanager.util;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

import static filemanager.constant.Option.MainOption.*;
import static filemanager.constant.Option.*;

public final class Validator {

    private final static Scanner SCANNER = new Scanner(System.in);

    public int validationOption() {
        int option = -1;

        while (isNotInInterval(option)) {
            System.out.println(MENU);
            try {
                option = SCANNER.nextInt();
            } catch (InputMismatchException e) {
                System.out.println();
            }
        }

        return option;
    }

    public boolean validationRecursive() {
        int result = -1;

        while (result != 1 && result != 2) {
            System.out.println(RECURSIVE_OPTION);
            try {
                result = SCANNER.nextInt();
            } catch (InputMismatchException e) {
                System.out.println(e.getMessage());
            }
        }

        return result == 1;
    }

    private boolean isNotInInterval(int option) {
        return option < REMOVE_SUBSTRING_IN_FILE_AND_DIR || option > EXIT;
    }

    public boolean isDirectory(File root) {
        boolean isDirectory = root.isDirectory();
        if (!isDirectory) {
            System.out.println(NOT_DIRECTORY);
        }
        return isDirectory;
    }
}
