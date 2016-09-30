/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.systemusage;

import java.io.File;

/**
 * To retrieve the usage of the resources of the system like the CPU, memory and disk space.
 */
public class FileSystemUsage {

    /**
     * This is to retrieve the file system information of any partition. It is giving the live values on each call.
     */
    public static class FileSystemInfo {
        private File file;

        public FileSystemInfo(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        /**
         * The number of unallocated bytes on the partition.
         * 
         * @return the number of unallocated bytes on the partition
         */
        public long getFreeSpace() {
            return file.getFreeSpace();
        }

        /**
         * The percentage of unallocated bytes on the partition.
         * 
         * @return the percentage of unallocated bytes on the partition from 0 to 100
         */
        public double getFreeSpacePercent() {
            long totalSpace = getTotalSpace();
            if (totalSpace == 0) {
                return 0;
            }
            return getFreeSpace() * 100.0 / totalSpace;
        }

        /**
         * The size, in bytes, of the partition.
         * 
         * @return the size, in bytes, of the partition
         */
        public long getTotalSpace() {
            return file.getTotalSpace();
        }

        /**
         * The number of allocated bytes on the partition.
         * 
         * @return the number of allocated bytes on the partition
         */
        public long getUsedSpace() {
            return getTotalSpace() - getFreeSpace();
        }

        /**
         * The percentage of allocated bytes on the partition.
         * 
         * @return the percentage of allocated bytes on the partition from 0 to 100
         */
        public double getUsedSpacePercent() {
            long totalSpace = getTotalSpace();
            if (totalSpace == 0) {
                return 0;
            }
            return getUsedSpace() * 100.0 / totalSpace;
        }

    }

    /**
     * Get the information of the root directories.
     * 
     * @return an array of FileSystemInfo
     */
    public static FileSystemInfo[] getRootFileSystemInfos() {
        File[] roots = File.listRoots();

        FileSystemInfo[] result = new FileSystemInfo[roots.length];
        int i = 0;
        for (File root : roots) {
            result[i++] = new FileSystemInfo(root);
        }
        return result;
    }

    public static void main(String[] args) {
        for (FileSystemInfo fsi : getRootFileSystemInfos()) {
            System.out.println("Path: " + fsi.getFile().getAbsolutePath());
            System.out.println("Free space: " + fsi.getFreeSpace() + " " + fsi.getFreeSpacePercent() + "%");
            System.out.println("Used space: " + fsi.getUsedSpace() + " " + fsi.getUsedSpacePercent() + "%");
            System.out.println("Total space: " + fsi.getTotalSpace());
            System.out.println();
        }
    }

    private FileSystemUsage() {
    }

}
