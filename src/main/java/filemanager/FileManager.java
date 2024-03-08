package filemanager;

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

    private String rootPath;
    private String substring;
    private int option;
    private int countDeletedFiles = 0;
    private int countDeletedDirs = 0;

    public void run() {
        validationOption();
        if (option != EXIT) {
            getData();
            switcher();
        } else {
            System.out.println("Canceled");
        }
    }

    private void validationOption() {
        option = -1;

        while (isNotInInterval(option)) {
            menu();
            try {
                option = new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.out.println();
            }
        }
    }

    private boolean isNotInInterval(int option) {
        return option < 1 || option > 7;
    }

    private void menu() {
        System.out.println("1 - remove substring from filename and foldername\n" +
                "2 - remove file include substring\n" +
                "3 - remove dir include substring\n" +
                "4 - move files with substring in new directory\n" +
                "5 - copy files with substring in new directory\n" +
                "6 - remove files with extension\n" +
                "7 - exit\n");
    }

    private void getData() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter rootPath:");
        rootPath = sc.nextLine();
        System.out.println("Enter substring:");
        substring = sc.nextLine();
    }

    private void switcher() {
        File root = new File(rootPath + File.separator);

        switch (option) {
            case REMOVE_SUBSTRING_IN_FILE_AND_DIR:
                removeSubstringFromNameFileAndDir(root, substring);
                System.out.printf("%s %s %s %d %s", "Removed substring", substring, "in", countDeletedFiles, "files");
                break;
            case REMOVE_FILE_WITH_SUBSTRING:
                removeFileWithSubstring(root, substring);
                System.out.printf("%s %d %s", "Removed", countDeletedFiles, "files");
                break;
            case REMOVE_DIR_WITH_SUBSTRING:
                removeDirWithSubstring(root, substring);
                System.out.printf("%s %d %s %d %s", "Removed", countDeletedDirs, "directories and", countDeletedFiles, "files");
                break;
            case MOVE_FILE, COPY_FILE:
                try {
                    File file = createNewDir();
                    copyOrMoveFileInNewPlace(root, substring, file);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                System.out.printf("%s %d %s", "Copied", countDeletedFiles, "files");
                break;
            case REMOVE_FILE_WITH_EXTENSION:
                removeFilesWithExtension(root, substring);
                System.out.printf("%s %d %s %s", "Removed", countDeletedFiles, "files with extension", substring);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + option);
        }
    }

    private void removeSubstringFromNameFileAndDir(File rootFile, String substring) {
        if (rootFile.isDirectory()) {
            File[] dirFiles = rootFile.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        removeSubstringFromNameFileAndDir(file, substring);
                    }
                    if (isFileContainsString(substring, file)) {
                        rename(file, substring);
                        countDeletedFiles++;
                    }
                }
            }
        }
    }

    private boolean isFileContainsString(String substring, File file) {
        return file.getName().toLowerCase().contains(substring.toLowerCase());
    }

    private void rename(File file, String trigger) {
        System.out.println("Rename " + file.getAbsolutePath());
        File newFile = new File(file.getParent() + File.separator + getName(file.getName(), trigger));
        file.renameTo(newFile);
    }

    private String getName(String name, String trigger) {
        int start = name.indexOf(trigger);
        int end = start + trigger.length();

        return name.substring(0, start) + name.substring(end);
    }

    private void removeFileWithSubstring(File rootFile, String substring) {
        if (rootFile.isDirectory()) {
            File[] dirFiles = rootFile.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        removeFileWithSubstring(file, substring);
                    }
                    if (file.isFile() && isFileContainsString(substring, file)) {
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
                    if (dir.isDirectory() && isFileContainsString(substring, dir)) {
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
            for (File f : file.listFiles()) {
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
        String newPath = new Scanner(System.in).nextLine();
        File directory = new File(newPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private void copyOrMoveFileInNewPlace(File rootFile, String substring, File target) throws IOException {
        if (rootFile.isDirectory()) {
            File[] dirFiles = rootFile.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        copyOrMoveFileInNewPlace(file, substring, target);
                    }
                    if (isFileContainsString(substring, file)) {
                        variable(target, file);
                    }
                }
            }
        }
    }

    private void variable(File target, File file) throws IOException {
        String from = file.getAbsolutePath();
        String to = target.getAbsolutePath() + File.separator + file.getName();

        if (new File(to).exists()) {
            return;
        }

        if (option == 4) {
            Files.move(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
        }
        if (option == 5) {
            Files.copy(file.toPath(), new File(to).toPath());
        }
        countDeletedFiles++;
    }

    public void removeFilesWithExtension(File rootFile, String extension) {
        if (rootFile.isDirectory()) {
            for (File file : Objects.requireNonNull(rootFile.listFiles())) {
                if (file.getName().endsWith(extension)) {
                    file.delete();
                    countDeletedFiles++;
                }
            }
        }
    }
}