package filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class FileManager {

    private String rootPath;
    private String substring;
    private int countRepeat = 0;
    private int countF = 0;
    private int countD = 0;
    private int option;

    public void run() {
        menu();
        option = validationOption();
        if (option != 7) {
            getData();
            switcher();
        } else {
            System.out.println("Canceled");
        }
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
        File file;
        switch (option) {
            case 1:
                removeSubstringFromNameFileAndDir(root, substring);
                System.out.printf("%s %s %s %d %s", "Removed substring", substring, "in", countF, "files");
                break;
            case 2:
                removeFileWithSubstring(root, substring);
                System.out.printf("%s %d %s", "Removed", countF, "files");
                break;
            case 3:
                removeDirWithSubstring(root, substring);
                System.out.printf("%s %d %s %d %s", "Removed", countD, "directories and", countF, "files");
                break;
            case 4, 5:
                if ((file = createNewDir()) == null) {
                    System.out.println("Incorrect path. Try again");
                    file = createNewDir();
                } else {
                    try {
                        copyOrMoveFileInNewPlace(root, substring, file);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    System.out.printf("%s %d %s", "Copied", countF, "files");
                }
                break;
            case 6:
                removeFilesWithExtension(root, substring);
                System.out.printf("%s %d %s %s", "Removed", countF, "files with extension", substring);
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
                    if (file.getName().toLowerCase().contains(substring.toLowerCase())) {
                        rename(file, substring);
                        countF++;
                    }
                }
            }
        }
    }

    private int validationOption() {
        Scanner sc = new Scanner(System.in);
        int result = -1;
        do {
            try {
                result = Integer.parseInt(sc.nextLine());
                if (result < 1 || result > 7) {
                    System.out.println("Incorrect option");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Incorrect data");
            }
        } while (result < 1 || result > 7);
        return result;
    }

    private File createNewDir() {
        System.out.println("new path for files");
        String newPath = new Scanner(System.in).nextLine();
        File directory = new File(newPath);
        if (!directory.exists()) {
            directory.mkdirs();
        } else {
            directory = null;
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
                    if (file.getName().toLowerCase().contains(substring.toLowerCase())) {
                        variable(substring, target, file);
                    }
                }
            }
        }
    }

    private void variable(String substring, File target, File file) throws IOException {
        String from = file.getAbsolutePath();
        String to = target.getAbsolutePath() + File.separator + file.getName();
        if (new File(to).exists()) {
            to = target.getAbsolutePath() + File.separator + countRepeat++ + file.getName();
        }
        if (option == 4) {
            Files.move(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
        } else if (option == 5) {
            Files.copy(file.toPath(), new File(to).toPath());
        }
        countF++;
    }

    private void removeDirWithSubstring(File rootFile, String substring) {
        if (rootFile.isDirectory()) {
            File[] directoriesList = rootFile.listFiles();
            if (directoriesList != null) {
                for (File dir : directoriesList) {
                    if (dir.isDirectory()) {
                        removeDirWithSubstring(dir, substring);
                    }
                    if (dir.isDirectory() && dir.getName().toLowerCase().contains(substring.toLowerCase())) {
                        boolean result = dir.delete();
                        if (result) {
                            countD++;
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
            countD++;
        }
        if (file.isFile()) {
            file.delete();
            countF++;
        }
        System.out.println("Deleted file/folder: " + file.getAbsolutePath());
    }

    private void removeFileWithSubstring(File rootFile, String substring) {
        if (rootFile.isDirectory()) {
            File[] dirFiles = rootFile.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        removeFileWithSubstring(file, substring);
                    }
                    if (file.isFile() && file.getName().toLowerCase().contains(substring.toLowerCase())) {
                        if (file.delete()) {
                            countF++;
                            System.out.println("File removed " + file.getAbsolutePath());
                        } else {
                            System.out.println("File didn't remove");
                        }
                    }
                }
            }
        }
    }



    private void rename(File file, String trigger) {
        System.out.println("Rename " + file.getAbsolutePath());
        File file2 = new File(file.getParent() + File.separator + getName(file.getName(), trigger));
        file.renameTo(file2);
    }

    private String getName(String name, String trigger) {
        int start = name.indexOf(trigger);
        int end = start + trigger.length();
        return name.substring(0, start) + name.substring(end);
    }

    public void removeFilesWithExtension(File rootFile, String extension) {
        if (rootFile.isDirectory()) {
            File[] files = rootFile.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(extension)) {
                    file.delete();
                    countF++;
                }
            }
        }
    }
}