package com.tomclaw.sfn;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

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

    public static List<File> listFiles(File source) {
        List<File> allFiles = new ArrayList<>();
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    allFiles.addAll(listFiles(file));
                } else {
                    allFiles.add(file);
                }
            }
        }
        return allFiles;
    }

}
