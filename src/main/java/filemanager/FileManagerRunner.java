package filemanager;

import filemanager.service.FileManager;
import filemanager.util.Validator;

public class FileManagerRunner {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager(new Validator());

        fileManager.run();
    }
}
