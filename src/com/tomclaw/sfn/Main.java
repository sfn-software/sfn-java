package com.tomclaw.sfn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.tomclaw.sfn.FileHelper.listFiles;
import static com.tomclaw.sfn.Sfn.DEFAULT_PORT_NUMBER;

public class Main {

    public static void main(String[] args) {
        try {
            boolean withIntegrityCheck = false;
            boolean isListen = false;
            String host = null;
            File dir = null;
            int port = DEFAULT_PORT_NUMBER;
            List<File> files = new ArrayList<>();
            for (int c = 0; c < args.length; c++) {
                String arg = args[c];
                switch (arg) {
                    case "--connect":
                    case "-c":
                        if (c + 1 < args.length) {
                            host = args[++c];
                        }
                        break;
                    case "--listen":
                    case "-l":
                        isListen = true;
                        break;
                    case "--port":
                    case "-p":
                        if (c + 1 < args.length) {
                            port = Integer.parseInt(args[++c]);
                        }
                        break;
                    case "--dir":
                    case "-d":
                        if (c + 1 < args.length) {
                            File file = new File(args[++c]);
                            if (!file.exists() || !file.isDirectory()) {
                                System.out.printf("Directory %s doesn't exist!", file.getAbsolutePath());
                                return;
                            }
                            dir = file;
                        }
                        break;
                    case "--md5":
                    case "-m":
                        withIntegrityCheck = true;
                        break;
                    case "--file":
                    case "-f":
                        if (c + 1 < args.length) {
                            File file = new File(args[++c]);
                            if (!file.exists()) {
                                System.out.printf("File %s doesn't exist!", file.getAbsolutePath());
                                return;
                            }
                            if (file.isDirectory()) {
                                files.addAll(listFiles(file));
                            } else {
                                files.add(file);
                            }
                        }
                        break;
                    case "--help":
                    case "-h":
                        println("Usage:");
                        println("");
                        println("    siphon --listen [options]");
                        println("    siphon --connect <address> [options]");
                        println("");
                        println("Options:");
                        println("");
                        println("    --version,   -v     Show sfn version and exit.");
                        println("    --help,      -h     Show this text and exit.");
                        println("    --port,      -p     Use specified port. Defaults to 3214.");
                        println("    --file,      -f     Send specified files of directories after connection." +
                                "                        Use \"-f file1 -f file2\" to send multiple files.");
                        println("    --directory, -d     Use specified directory to store received files.\n" +
                                "                        Format is: /home/user/folder/.\n");
                        return;
                }
            }
            Sfn sfn = new Sfn(withIntegrityCheck);
            if (isListen) {
                sfn.listen(port);
            } else if (host != null) {
                sfn.connect(host, port);
            }
            if (dir != null) {
                sfn.receiveFiles(dir);
            }
            if (!files.isEmpty()) {
                sfn.sendFiles(files);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static void println(String s) {
        System.out.println(s);
    }

}
