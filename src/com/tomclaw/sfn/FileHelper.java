package com.tomclaw.sfn;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static class FileWrapper {
        public String baseDir;
        public File file;

        public FileWrapper(String baseDir, File file) {
            this.baseDir = baseDir;
            this.file = file;
        }
    }

    public static String calculateMD5(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        try (InputStream fileStream = new FileInputStream(file)) {
            byte[] buf = new byte[10240];
            int len;
            while ((len = fileStream.read(buf)) > 0) {
                messageDigest.update(buf, 0, len);
            }
        }
        byte[] digest = messageDigest.digest();

        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static List<FileWrapper> listFiles(String baseDir, File source) {
        List<FileWrapper> allFiles = new ArrayList<>();
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    allFiles.addAll(listFiles(baseDir, file));
                } else {
                    allFiles.add(new FileWrapper(baseDir, file));
                }
            }
        }
        return allFiles;
    }

}
