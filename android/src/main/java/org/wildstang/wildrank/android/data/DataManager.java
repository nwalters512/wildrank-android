package org.wildstang.wildrank.android.data;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.wildstang.wildrank.android.utils.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataManager {

    public static final int DIRECTORY_NONSYNCED = 0;
    public static final int DIRECTORY_SYNCED = 1;
    public static final int DIRECTORY_QUEUE = 2;
    public static final int DIRECTORY_FIRST_FOUND = 4;
    public static final int DIRECTORY_FLASH_SYNCED = 5;
    public static final int DIRECTORY_FLASH_UNINTEGRATED = 6;
    public static final int DIRECTORY_FLASH_ROOT = 7;

    private static DataManager instance;

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void saveChangedFile(Context context, DataFile data) throws IOException {
        File queueFile = new File(getQueueDirectory(context) + data.getRelativeFile());

        if (queueFile.exists()) {
            Log.d("error", "Queued file already exists! Deleting...");
            queueFile.delete();
        }

        queueFile.getParentFile().mkdirs();
        queueFile.createNewFile();

        // Write data to newly created files
        Writer queueOutput = new BufferedWriter(new FileWriter(queueFile));
        queueOutput.write(data.getContent());
        queueOutput.flush();
        queueOutput.close();
    }

    public static boolean loadDataIfExists(DataFile data, Context context, int location) {
        File file;
        if (location != DIRECTORY_FIRST_FOUND) {
            file = new File(getDirectory(location, context) + File.separator + data.getRelativeFile());
        } else {
            file = new File(getDirectory(DIRECTORY_QUEUE, context) + File.separator + data.getRelativeFile());
            if (!file.exists()) {
                file = new File(getDirectory(DIRECTORY_SYNCED, context) + File.separator + data.getRelativeFile());
                if (!file.exists()) {
                    file = new File(getDirectory(DIRECTORY_NONSYNCED, context) + File.separator + data.getRelativeFile());
                }
            }
        }
        if (!file.exists()) {
            return false;
        }
        try {
            BufferedReader fileContents = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = fileContents.readLine()) != null) {
                content.append(line);
                content.append('\n');
            }
            data.setContent(content.toString());
            fileContents.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void syncWithFlashDrive(Context context) throws IOException {
        // First, copy everything from the queue directory to the flash drive
        List<File> queuedFiles = new ArrayList<>();
        listFilesInDirectory(getQueueDirectory(context), queuedFiles);
        for (File file : queuedFiles) {
            File destinationFile;
            if (!file.getAbsolutePath().contains("notes")) {
                destinationFile = new File(getFlashDriveSyncedDirectory() + File.separator + getRelativePathFromInternalStorage(context, file));
                FileUtils.copyFile(file, destinationFile);
            }
        }

        syncFlashToLocal(context);

        // Now we copy the contents of the unintegrated files on the flash drive to the tablet
        // We append the data instead of overwriting it
        List<File> unintegratedFiles = new ArrayList<>();
        listFilesInDirectory(getFlashDriveUnintegratedDirectory(), unintegratedFiles);
        for (File file : unintegratedFiles) {
            File destinationFile;
            if (file.getAbsolutePath().contains("notes")) {
                destinationFile = new File(getQueueDirectory(context) + File.separator + getRelativePathFromFlashDriveStorage(file));
                Log.d("sync:unintegrate", "source file: " + file.getAbsolutePath() + "; destination file: " + destinationFile.getAbsolutePath());
                copyFileWithAppend(file, destinationFile);
            }
        }

        // Next, copy out the newly updated unintegrated files to the flash drive
        File[] updatedIntegratedFiles = getQueueDirectory(context).listFiles();
        for (File file : updatedIntegratedFiles) {
            if (file.isFile() && file.getAbsolutePath().contains("notes")) {
                FileUtils.copyFile(file, getFlashDriveUnintegratedDirectory());
            } else if (file.isDirectory() && file.getAbsolutePath().contains("notes")) {
                FileUtils.copyDirectoryToDirectory(file, getFlashDriveUnintegratedDirectory());
            }
        }

        // Finally, empty the queue directory
        FileUtils.cleanDirectory(getQueueDirectory(context));
    }

    private static void syncFlashToLocal(Context c) {

        long startTime = System.currentTimeMillis();
        int totalFiles = 0;
        // Get lists of files in both directories
        List<File> localSyncedFiles = new ArrayList<>();
        listFilesInDirectory(getDirectory(DIRECTORY_SYNCED, c), localSyncedFiles);
        List<File> flashSyncedFiles = new ArrayList<>();
        listFilesInDirectory(getFlashDriveSyncedDirectory(), flashSyncedFiles);
        for (File f : localSyncedFiles) {
            Log.d("sync", "local: " + f.getAbsolutePath());
        }
        for (File f : flashSyncedFiles) {
            Log.d("sync", "flash: " + f.getAbsolutePath());
        }
        // Filter each list to have only relative locations

        List<String> localPaths = new ArrayList<>();
        List<String> flashPaths = new ArrayList<>();
        for (File file : localSyncedFiles) {
            localPaths.add(getRelativePathForLocal(c, file));
        }
        for (File file : flashSyncedFiles) {
            flashPaths.add(getRelativePathForFlashDrive(c, file));
        }


        Iterator<String> flashIterator = flashPaths.iterator();
        while (flashIterator.hasNext()) {
            String flashPath = flashIterator.next();
            totalFiles++;
            if (localPaths.contains(flashPath)) {
                File flashFile = new File(getFlashDriveSyncedDirectory() + File.separator + flashPath);
                File localFile = new File(getDirectory(DIRECTORY_SYNCED, c) + File.separator + flashPath);
                syncFile(localFile, flashFile);
                flashIterator.remove();
                localPaths.remove(flashPath);
            } else {
                File flashFile = new File(getFlashDriveSyncedDirectory() + File.separator + flashPath);
                File localFile = new File(getDirectory(DIRECTORY_SYNCED, c) + File.separator + flashPath);
                localFile.getParentFile().mkdirs();
                try {
                    localFile.createNewFile();
                    FileUtils.copyFile(flashFile, localFile);
                    Log.d("sync", "new file created!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                flashIterator.remove();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        Log.d("sync", "Total time for sync: " + totalTime + "ms");
        if (totalFiles != 0) {
            Log.d("sync", "Average time per file: " + (totalTime / totalFiles) + "ms");
        }
    }

    private static void syncFile(File localFile, File flashFile) {
        long localTimestamp = localFile.lastModified();
        long flashTimestamp = flashFile.lastModified();
        try {
            if (localTimestamp < flashTimestamp) {
                FileUtils.copyFile(flashFile, localFile);
                Log.d("sync", "file" + localFile.getAbsolutePath() + " updated! copying to local");
            } else {
                // If timestamp is the same, we can assume that the files are identical
                Log.d("sync", "file" + localFile.getAbsolutePath() + " not updated! skipping ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRelativePathForLocal(Context c, File file) {
        String absolutePath = file.getAbsolutePath();

        // First we find the length of the root path
        int startIndex = getDirectory(DIRECTORY_SYNCED, c).getAbsolutePath().replace("synced", "").length();
        // Next, we search for the next file separator character after that
        int fileSeparatorIndex = absolutePath.indexOf(File.separator, startIndex + 1);
        // If we remove all of the string before that character, we have the relative path!
        String relativePath = absolutePath.substring(fileSeparatorIndex);
        Log.d("getRelativePath", "local relative path: " + relativePath);
        return relativePath;
    }

    public static String getRelativePathForFlashDrive(Context c, File file) {
        String absolutePath = file.getAbsolutePath();
        // First we find the length of the root path
        int startIndex = getDirectory(DIRECTORY_FLASH_SYNCED, c).getAbsolutePath().replace("synced", "").length();
        // Next, we search for the next file separator character after that
        int fileSeparatorIndex = absolutePath.indexOf(File.separator, startIndex + 1);
        // If we remove all of the string before that character, we have the relative path!
        String relativePath = absolutePath.substring(fileSeparatorIndex);
        Log.d("getRelativePath", "flash relative path: " + relativePath);
        return relativePath;
    }

    private static void copyFileWithAppend(File source, File destination) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source file must exist!");
        }
        if (!destination.exists()) {
            destination.createNewFile();
        }
        BufferedReader sourceReader = new BufferedReader(new FileReader(source));
        BufferedWriter destinationWriter = new BufferedWriter(new FileWriter(destination, true));
        String line;
        while ((line = sourceReader.readLine()) != null) {
            destinationWriter.write(line);
            destinationWriter.newLine();
        }
        destinationWriter.flush();
        destinationWriter.close();
        sourceReader.close();
    }

    public static String getRelativePathFromInternalStorage(Context context, File file) {
        String absolutePath = file.getAbsolutePath();

        // Works on the assumption that all files are
        // stored in /data/data/org.wildstang.wildrank/files
        // Also assumes that all files are stored in subdirectories of that
        // that mirror the structue of the other folders
        // First we find the length of that portion of the path
        int startIndex = context.getFilesDir().getAbsolutePath().length();
        // Next, we search for the next file separator character after that
        int fileSeparatorIndex = absolutePath.indexOf(File.separator, startIndex + 1);
        // If we remove all of the string before that character, we have the relative path!
        String relativePath = absolutePath.substring(fileSeparatorIndex);
        Log.d("getRelativePath", "path: " + relativePath);
        return relativePath;
    }

    public static String getRelativePathFromFlashDriveStorage(File file) {
        String absolutePath = file.getAbsolutePath();

        if (absolutePath.contains("unintegrated")) {
            int startIndex = absolutePath.indexOf("unintegrated") + "unintegrated".length() + 1;
            return absolutePath.substring(startIndex);
        } else if (absolutePath.contains("synced")) {
            int startIndex = absolutePath.indexOf("synced") + "synced".length() + 1;
            return absolutePath.substring(startIndex);
        } else {
            return "";
        }
    }

    public static File getDirectory(int directory, Context context) {
        switch (directory) {
            case DIRECTORY_NONSYNCED:
                return getNonsyncedDirectory(context);
            case DIRECTORY_QUEUE:
                return getQueueDirectory(context);
            case DIRECTORY_SYNCED:
                return getSyncedDirectory(context);
            case DIRECTORY_FLASH_UNINTEGRATED:
                return getFlashDriveUnintegratedDirectory();
            case DIRECTORY_FLASH_SYNCED:
                return getFlashDriveSyncedDirectory();
            case DIRECTORY_FLASH_ROOT:
                return new File(Constants.USB_FLASH_DRIVE_ROOT_PATH);
            default:
                Log.w("getDirectory", "getDirectory() must be called with a valid integer directory identifier!");
                return new File("");
        }
    }

    public static File getDataFileFromDirectory(DataFile data, Context context, int location) {
        File file;
        if (location != DIRECTORY_FIRST_FOUND) {
            file = new File(getDirectory(location, context) + File.separator + data.getRelativeFile());
        } else {
            file = new File(getDirectory(DIRECTORY_QUEUE, context) + File.separator + data.getRelativeFile());
            if (!file.exists()) {
                file = new File(getDirectory(DIRECTORY_SYNCED, context) + File.separator + data.getRelativeFile());
                if (!file.exists()) {
                    file = new File(getDirectory(DIRECTORY_NONSYNCED, context) + File.separator + data.getRelativeFile());
                    if (!file.exists()) {
                        file = null;
                    }
                }
            }
        }
        return file;
    }

    private static File getSyncedDirectory(Context context) {
        File directory = new File(context.getFilesDir() + "/synced/");
        directory.mkdirs();
        return directory;
    }

    private static File getQueueDirectory(Context context) {
        File directory = new File(context.getFilesDir() + "/queue/");
        directory.mkdirs();
        return directory;
    }

    private static File getNonsyncedDirectory(Context context) {
        File directory = new File(context.getFilesDir() + "/nonsynced/");
        directory.mkdirs();
        return directory;
    }

    private static File getFlashDriveSyncedDirectory() {
        File directory = new File(Constants.USB_FLASH_DRIVE_ROOT_PATH + "/synced/");
        directory.mkdirs();
        return directory;
    }

    private static File getFlashDriveUnintegratedDirectory() {
        File directory = new File(Constants.USB_FLASH_DRIVE_ROOT_PATH + "/unintegrated/");
        directory.mkdirs();
        return directory;
    }

    public static void listFilesInDirectory(File directory, List<File> list) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    list.add(file);
                } else {
                    listFilesInDirectory(file, list);
                }
            }
        }
    }

    /*
     * Returns true if the flash drive is connected. This is checked by
     * attempting to create a temporary file in the root of the flash drive and
     * checking to see if it exists.
     */
    public static boolean isFlashDriveConnected(Context context) {
        // Test if USB is connected
        File testFile = new File(getDirectory(DIRECTORY_FLASH_SYNCED, context) + "text.wild");
        boolean couldCreate = true;
        try {
            testFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            couldCreate = false;
        }
        if (testFile.exists() && couldCreate) {
            testFile.delete();
            return true;
        } else {
            return false;
        }
    }

    /*
     * Returns true if the flash drive contains the file "/event/event.json"
     */
    public static boolean isFlashDriveConfigured(Context context) {
        // Test if USB is connected
        File testFile = new File(getDirectory(DIRECTORY_FLASH_SYNCED, context) + "/event/event.json");
        return testFile.exists();
    }

    public static void prepareForEject() {
        // Sync the buffer with the underlying filesystem so
        // we can safely remove the flash drive
        String[] cmds = {"sync; sync"};
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMatchScouted(Context context, int matchNumber, int teamNumber) {
        MatchData data = new MatchData();
        data.setMatchNumber(matchNumber);
        data.setTeamNumber(teamNumber);
        File syncedFile = new File(getSyncedDirectory(context) + data.getRelativeFile());
        File queuedFile = new File(getQueueDirectory(context) + data.getRelativeFile());
        return syncedFile.exists() || queuedFile.exists();
    }

    public static boolean isTeamPitScouted(Context context, int teamNumber) {
        PitData data = new PitData();
        data.setTeamNumber(teamNumber);
        File syncedFile = new File(getSyncedDirectory(context) + data.getRelativeFile());
        File queuedFile = new File(getQueueDirectory(context) + data.getRelativeFile());
        return syncedFile.exists() || queuedFile.exists();
    }

    public static ArrayList<MatchData> getAllMatchResultsForTeam(Context context, int teamNumber) {
        ArrayList<File> files = new ArrayList<>();
        listFilesInDirectory(new File(getDirectory(DIRECTORY_SYNCED, context) + File.separator + "matches"), files);
        Log.d("getMatchResults", "file list length: " + files.size());
        ArrayList<File> filteredFiles = new ArrayList<>();
        // Filter out only the team we're looking for
        for (File file : files) {
            String path = file.getAbsolutePath();
            int currentTeamNumber = Integer.parseInt(path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf(".")));
            if (teamNumber == currentTeamNumber) {
                filteredFiles.add(file);
            }
        }
        ArrayList<MatchData> matchData = new ArrayList<>();
        for (File file : filteredFiles) {
            String path = file.getAbsolutePath();
            int currentTeamNumber = Integer.parseInt(path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf(".")));
            int currentMatchNumber = Integer.parseInt(path.substring(path.lastIndexOf(File.separator, path.lastIndexOf(File.separator) - 1) + 1, path.lastIndexOf(File.separator)));
            MatchData data = new MatchData();
            data.setMatchNumber(currentMatchNumber);
            data.setTeamNumber(currentTeamNumber);
            loadDataIfExists(data, context, DIRECTORY_SYNCED);
            matchData.add(data);
        }
        return matchData;
    }

}
