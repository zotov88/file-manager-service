package filemanager.util;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

import static filemanager.constant.OptionConst.*;
import static filemanager.constant.OptionConst.MainOption.*;

public class Validator {

    public int validationOption() {
        int option = -1;

        while (isNotInInterval(option)) {
            System.out.println(MENU);
            try {
                option = new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.out.println();
            }
        }

        return option;
    }

    private boolean isNotInInterval(int option) {
        return option < REMOVE_SUBSTRING_IN_FILE_AND_DIR || option > EXIT;
    }

    public void validateRootPath(File root) {
        if (!root.isDirectory()) {
            System.out.println(NOT_DIRECTORY);
            System.exit(0);
        }
    }
}
