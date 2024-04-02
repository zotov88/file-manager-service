package filemanager;

import filemanager.service.FileManager;
import filemanager.util.Validator;

public class FileManagerRunner {
    public static void main(String[] args) {
        new FileManager(new Validator()).run();
    }
}
