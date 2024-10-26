package com.tomclaw.sfn;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;

import static com.tomclaw.sfn.StreamHelper.readLine;

public class Sfn {

    public static final int DEFAULT_PORT_NUMBER = 3214;

    static final byte FILE = 0x01;
    private static final byte DONE = 0x02;
    private static final byte MD5_WITH_FILE = 0x03;
    static final byte FILE_WITH_MD5 = 0x04;
    static final byte FILE_WITH_PATH = 0x05;

    private final int transferType;
    private Socket socket;
    private final ProgressBar progressBar;

    public Sfn(int transferType) {
        this.transferType = transferType;
        this.progressBar = new ProgressBar();
    }

    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
    }

    public void listen(int port) throws IOException {
        socket = new ServerSocket(port).accept();
    }

    public void sendFiles(List<FileHelper.FileWrapper> files) throws Exception {
        ByteBuffer fileLength = ByteBuffer.allocate(8);
        fileLength.order(ByteOrder.LITTLE_ENDIAN);
        try (DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(
                        socket.getOutputStream()
                )
        )) {
            for (FileHelper.FileWrapper wrapper : files) {
                progressBar.setupBar(wrapper.file.getName(), wrapper.file.length());
                boolean withIntegrityCheck = false;
                switch (transferType) {
                    case FILE_WITH_MD5:
                    case FILE_WITH_PATH:
                        withIntegrityCheck = true;
                    case FILE:
                        output.writeByte(transferType);
                        break;
                    default:
                        throw new Exception("Unsupported file transfer type");
                }
                output.writeBytes(wrapper.file.getName() + '\n');
                fileLength.putLong(wrapper.file.length());
                output.write(fileLength.array());
                fileLength.clear();
                if (transferType == FILE_WITH_PATH) {
                    String relPath = wrapper.file.getParent();
                    if (relPath.startsWith(wrapper.baseDir)) {
                        relPath = relPath.substring(wrapper.baseDir.length());
                    }
                    if (relPath.startsWith("/")) {
                        relPath = relPath.substring(1);
                    }
                    output.writeBytes(relPath + '\n');

                    output.writeBoolean(wrapper.file.canExecute());
                }
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                try (InputStream input = new DigestInputStream(new FileInputStream(wrapper.file), messageDigest)) {
                    byte[] buf = new byte[10240];
                    int len;
                    long sent = 0;
                    while ((len = input.read(buf)) > 0) {
                        output.write(buf, 0, len);
                        sent += len;
                        progressBar.showBar(sent);
                    }
                }
                progressBar.done();
                if (withIntegrityCheck) {
                    byte[] digest = messageDigest.digest();
                    output.writeBytes(arrayToHex(digest) + '\n');
                }
            }
            output.writeByte(DONE);
            output.flush();
        }
    }

    public void receiveFiles(File dir) throws IOException, SfnProtocolException, NoSuchAlgorithmException {
        ByteBuffer fileLength = ByteBuffer.allocate(8);
        fileLength.order(ByteOrder.LITTLE_ENDIAN);
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            boolean isActive = true;
            do {
                int type = input.readByte();
                switch (type) {
                    case FILE:
                    case MD5_WITH_FILE:
                    case FILE_WITH_MD5:
                    case FILE_WITH_PATH:
                        String fileName = readLine(input);
                        if (fileName == null || fileName.isEmpty()) {
                            throw new SfnProtocolException();
                        }
                        input.readFully(fileLength.array(), 0, fileLength.capacity());
                        long fileSize = fileLength.getLong();
                        fileLength.clear();
                        String filePath = "";
                        boolean executable = false;
                        File fileDir = dir;
                        if (type == FILE_WITH_PATH) {
                            filePath = readLine(input);
                            if (filePath == null) {
                                throw new SfnProtocolException();
                            }
                            filePath = filePath.replaceAll("/", Matcher.quoteReplacement(File.separator));
                            fileDir = new File(fileDir, filePath);
                            if (!fileDir.exists() && !fileDir.mkdirs()) {
                                throw new IOException("Failed to create dirs");
                            }
                            executable = input.readBoolean();
                        }
                        String md5 = null;
                        if (type == MD5_WITH_FILE) {
                            md5 = readLine(input);
                        }
                        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                        File file = new File(fileDir, fileName);
                        try (OutputStream output = new FileOutputStream(file)) {
                            byte[] buf = new byte[10240];
                            int len;
                            long totalRead = 0;
                            do {
                                int read = (int) Math.min(fileSize - totalRead, buf.length);
                                len = input.read(buf, 0, read);
                                if (len == -1) {
                                    throw new EOFException();
                                }
                                messageDigest.update(buf, 0, len);
                                output.write(buf, 0, len);
                            } while ((totalRead += len) < fileSize);
                        }
                        if (!file.setExecutable(executable)) {
                            System.out.printf(" => Unable to mark file %s executable\n", file.getName());
                        }
                        if (type == FILE_WITH_MD5 || type == FILE_WITH_PATH) {
                            md5 = readLine(input);
                        }
                        if (md5 != null) {
                            byte[] digest = messageDigest.digest();
                            String md5hex = arrayToHex(digest);
                            boolean checkPassed = md5.equals(md5hex);
                            if (checkPassed) {
                                System.out.printf(" => MD5 [%s] check passed\n", md5hex);
                            } else {
                                System.out.printf(" => MD5 check failed!\n remote: %s\n local:  %s\n", md5, md5hex);
                            }
                        }
                        break;
                    case DONE:
                        isActive = false;
                        break;
                }
            } while (isActive);
        }
    }

    private static String arrayToHex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte b : data) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

}
