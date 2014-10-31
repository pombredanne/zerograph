package org.zerograph;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A graph database on disk. This may or may not exist as a file, unlike a
 * Graph which must exist on disk.
 *
 */
public class GraphDirectory {

    public static File getHome() {
        return Environment.getInstance().getDataDirectory();
    }

    /**
     * Fetch a list of all existing graph database directories.
     *
     * @return
     */
    public static Set<File> getAllExisting() {
        HashSet<File> directories = new HashSet<>();
        File home = getHome();
        if (home.isDirectory()) {
            File[] files = home.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        String name = file.getName();
                        if (Pattern.matches("[0-9]{1,5}", name)) {
                            directories.add(file);
                        }
                    }
                }
            }
        }
        return directories;
    }

    final private String host;
    final private int port;
    final private File file;

    public GraphDirectory(String host, int port) {
        this.host = host;
        this.port = port;
        this.file = new File(getHome().getPath() + "/" + port);
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getPath() {
        return this.file.getPath();
    }

    public File getFile() {
        return this.file;
    }

    public boolean exists() {
        return this.file.exists();
    }

}
