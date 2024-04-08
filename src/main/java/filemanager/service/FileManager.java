package filemanager.service;

import filemanager.util.Validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Scanner;

import static filemanager.constant.Option.MainOption.*;

public class FileManager {

    private final Validator validator;
    private File root;
    private String substring;
    private int options;
    private boolean isRecursive;
    private int count = 0;
    private int countFiles = 0;
    private int countDirs = 0;

    public FileManager(Validator validator) {
        this.validator = validator;
    }

    public void run() {
        options = validator.validationOption();

        if (options != EXIT) {
            setRecursive();
            getData();
            validator.validateRootPath(root);
            switcher();
        }
    }

    private void setRecursive() {
        isRecursive = validator.validationRecursive();
    }

    private void getData() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter rootPath:");
        root = new File(sc.nextLine());

        if (isNeedGetSubstring()) {
            System.out.println("Enter substring:");
            substring = sc.nextLine();
        }
    }

    private boolean isNeedGetSubstring() {
        return options != COUNT_OF_FILES_AND_DIRECTORIES;
    }

    private void switcher() {
        switch (options) {
            case REMOVE_SUBSTRING_IN_FILE_AND_DIR:
                removeSubstringFromNameFileAndDir(root, substring);
                System.out.printf("%s %s %s %d %s\n", "Removed substring", substring, "in", countFiles, "files.");
                break;
            case REMOVE_FILE_WITH_SUBSTRING:
                removeFileWithSubstring(root, substring);
                System.out.printf("%s %d %s\n", "Removed", countFiles, "files.");
                break;
            case REMOVE_DIR_WITH_SUBSTRING:
                removeDirWithSubstring(root, substring);
                System.out.printf("%s %d %s %d %s\n", "Removed", countDirs, "directories and", countFiles, "files.");
                break;
            case MOVE_FILE, COPY_FILE:
                try {
                    File file = createNewDir();
                    copyOrMoveFile(root, file, substring);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                System.out.printf("%s %d %s\n", "Processed", countFiles, "files.");
                break;
            case REMOVE_FILE_WITH_EXTENSION:
                removeFilesWithExtension(root, substring);
                System.out.printf("%s %d %s %s\n", "Removed", countFiles, "files with extension.", substring);
                break;
            case COUNT_OF_FILES_AND_DIRECTORIES:
                countFiles(root);
                System.out.printf("%s %d\n%s %d\n", "Count of files:", countFiles, "Count of directories:", countDirs);
                break;
            case COUNT_OF_FILES_WITH_SUBSTRING:
//                countOfFilesIncludeSubstring();
                System.out.printf("%s %s: %d\n", "Count of files in path ", root, count);
                break;
            case COUNT_OF_DIRECTORIES_SUBSTRING:
//                countOfDirectoriesIncludeSubstring();
                System.out.printf("%s %s: %d\n", "Count of directories in path ", root, count);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + options);
        }
    }

    private void removeSubstringFromNameFileAndDir(File root, String substring) {
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (isRecursive && file.isDirectory()) {
                removeSubstringFromNameFileAndDir(file, substring);
            }
            if (isFileContainsString(file, substring)) {
                renameFile(file, substring);
                countFiles++;
            }
        }
    }

    private boolean isFileContainsString(File file, String substring) {
        return file.getName().length() > 1 && file.getName().toLowerCase().contains(substring.toLowerCase());
    }

    private void renameFile(File file, String substring) {
        System.out.println("Rename " + file.getAbsolutePath());
        File newFile = new File(file.getParent() + File.separator + getName(file.getName(), substring));
        file.renameTo(newFile);
    }

    private String getName(String fileName, String substring) {
        int start = fileName.indexOf(substring);
        int end = start + substring.length();

        return fileName.substring(0, start) + fileName.substring(end);
    }

    private void removeFileWithSubstring(File root, String substring) {
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (isRecursive && file.isDirectory()) {
                removeFileWithSubstring(file, substring);
            }
            if (file.isFile() && isFileContainsString(file, substring)) {
                if (file.delete()) {
                    countFiles++;
                    System.out.println("File removed " + file.getAbsolutePath());
                } else {
                    System.out.println("File didn't remove");
                }
            }
        }
    }

    private void removeDirWithSubstring(File root, String substring) {
        for (File dir : Objects.requireNonNull(root.listFiles())) {
            if (isRecursive && dir.isDirectory()) {
                removeDirWithSubstring(dir, substring);
            }
            if (dir.isDirectory() && isFileContainsString(dir, substring)) {
                if (dir.delete()) {
                    countDirs++;
                    System.out.println("Directory removed " + dir.getAbsolutePath());
                } else {
                    recursiveDelete(dir);
                }
            }
        }
    }

    private void recursiveDelete(File file) {
        if (!file.exists()) {
            return;
        }
        if (isRecursive && file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                recursiveDelete(f);
            }
        }
        if (file.isDirectory()) {
            file.delete();
            countDirs++;
        }
        if (file.isFile()) {
            file.delete();
            countFiles++;
        }

        System.out.println("Deleted file/folder: " + file.getAbsolutePath());
    }

    private File createNewDir() {
        System.out.println("new path to directory for files");
        File directory = new File(new Scanner(System.in).nextLine());

        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory;
    }

    private void copyOrMoveFile(File root, File newLocation, String substring) throws IOException {
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (isRecursive && file.isDirectory()) {
                copyOrMoveFile(file, newLocation, substring);
            }
            if (isFileContainsString(file, substring)) {
                processFile(file, newLocation);
            }
        }
    }

    private void processFile(File file, File newLocation) throws IOException {
        String from = file.getAbsolutePath();
        String to = newLocation.getAbsolutePath() + File.separator + file.getName();

        if (new File(to).exists()) {
            return;
        }
        if (options == MOVE_FILE) {
            Files.move(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
        }
        if (options == COPY_FILE) {
            Files.copy(file.toPath(), new File(to).toPath());
        }
        countFiles++;
    }

    public void removeFilesWithExtension(File root, String extension) {
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (file.getName().endsWith(extension)) {
                file.delete();
                countFiles++;
            }
        }
    }

    private void countFiles(File root) {
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (isRecursive && file.isDirectory()) {
                countDirs++;
                countFiles(file);
            } else {
                countFiles++;
            }
        }
    }
}