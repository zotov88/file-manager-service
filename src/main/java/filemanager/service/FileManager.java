package filemanager.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Scanner;

import static filemanager.constant.Option.*;

public class FileManager {

    private File root;
    private String substring;
    private int option = -1;
    private int countDeletedFiles = 0;
    private int countDeletedDirs = 0;

    public void run() {
        validationOption();
        getData();
        if (option != EXIT) {
            switcher();
        }
    }

    private void validationOption() {
        while (isNotInInterval(option)) {
            System.out.println(MENU);
            try {
                option = new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.out.println();
            }
        }
    }

    private boolean isNotInInterval(int option) {
        return option < REMOVE_SUBSTRING_IN_FILE_AND_DIR || option > EXIT;
    }

    private void getData() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter rootPath:");
        root = new File(sc.nextLine());
        System.out.println("Enter substring:");
        substring = sc.nextLine();
    }

    private void switcher() {
        switch (option) {
            case REMOVE_SUBSTRING_IN_FILE_AND_DIR:
                removeSubstringFromNameFileAndDir(root, substring);
                System.out.printf("%s %s %s %d %s", "Removed substring", substring, "in", countDeletedFiles, "files.");
                break;
            case REMOVE_FILE_WITH_SUBSTRING:
                removeFileWithSubstring(root, substring);
                System.out.printf("%s %d %s", "Removed", countDeletedFiles, "files.");
                break;
            case REMOVE_DIR_WITH_SUBSTRING:
                removeDirWithSubstring(root, substring);
                System.out.printf("%s %d %s %d %s", "Removed", countDeletedDirs, "directories and", countDeletedFiles, "files.");
                break;
            case MOVE_FILE, COPY_FILE:
                try {
                    File file = createNewDir();
                    copyOrMoveFile(root, file, substring);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                System.out.printf("%s %d %s", "Processed", countDeletedFiles, "files.");
                break;
            case REMOVE_FILE_WITH_EXTENSION:
                removeFilesWithExtension(root, substring);
                System.out.printf("%s %d %s %s", "Removed", countDeletedFiles, "files with extension.", substring);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + option);
        }
    }

    private void removeSubstringFromNameFileAndDir(File root, String substring) {
        if (root.isDirectory()) {
            File[] dirFiles = root.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        removeSubstringFromNameFileAndDir(file, substring);
                    }
                    if (isFileContainsString(file, substring)) {
                        renameFile(file, substring);
                        countDeletedFiles++;
                    }
                }
            }
        }
    }

    private boolean isFileContainsString(File file, String substring)  {
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
        if (root.isDirectory()) {
            File[] dirFiles = root.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        removeFileWithSubstring(file, substring);
                    }
                    if (file.isFile() && isFileContainsString(file, substring)) {
                        if (file.delete()) {
                            countDeletedFiles++;
                            System.out.println("File removed " + file.getAbsolutePath());
                        } else {
                            System.out.println("File didn't remove");
                        }
                    }
                }
            }
        }
    }

    private void removeDirWithSubstring(File rootFile, String substring) {
        if (rootFile.isDirectory()) {
            File[] directoriesList = rootFile.listFiles();
            if (directoriesList != null) {
                for (File dir : directoriesList) {
                    if (dir.isDirectory()) {
                        removeDirWithSubstring(dir, substring);
                    }
                    if (dir.isDirectory() && isFileContainsString(dir, substring)) {
                        if (dir.delete()) {
                            countDeletedDirs++;
                            System.out.println("Directory removed " + dir.getAbsolutePath());
                        } else {
                            recursiveDelete(dir);
                        }
                    }
                }
            }
        }
    }

    private void recursiveDelete(File file) {
        if (!file.exists())
            return;

        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                recursiveDelete(f);
            }
        }
        if (file.isDirectory()) {
            file.delete();
            countDeletedDirs++;
        }
        if (file.isFile()) {
            file.delete();
            countDeletedFiles++;
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
        if (root.isDirectory()) {
            File[] dirFiles = root.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        copyOrMoveFile(file, newLocation, substring);
                    }
                    if (isFileContainsString(file, substring)) {
                        processFile(file, newLocation);
                    }
                }
            }
        }
    }

    private void processFile(File file, File newLocation) throws IOException {
        String from = file.getAbsolutePath();
        String to = newLocation.getAbsolutePath() + File.separator + file.getName();

        if (new File(to).exists()) {
            return;
        }
        if (option == MOVE_FILE) {
            Files.move(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
        }
        if (option == COPY_FILE) {
            Files.copy(file.toPath(), new File(to).toPath());
        }
        countDeletedFiles++;
    }

    public void removeFilesWithExtension(File root, String extension) {
        if (root.isDirectory()) {
            for (File file : Objects.requireNonNull(root.listFiles())) {
                if (file.getName().endsWith(extension)) {
                    file.delete();
                    countDeletedFiles++;
                }
            }
        }
    }
}