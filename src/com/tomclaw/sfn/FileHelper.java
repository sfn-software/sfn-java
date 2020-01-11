package com.tomclaw.sfn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

}
