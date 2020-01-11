package com.tomclaw.sfn;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static com.tomclaw.sfn.Sfn.DEFAULT_PORT_NUMBER;

public class Main {

    public static void main(String[] args) {
        new Thread(Main::sendFiles).start();
        receiveFiles();
    }

    private static void receiveFiles() {
        try {
            File receiveDir = new File("/Users/solkin/Downloads/sfn-files");
            Sfn receive = new Sfn(true);
            receive.listen(DEFAULT_PORT_NUMBER);
            receive.receiveFiles(receiveDir);
        } catch (IOException | SfnProtocolException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void sendFiles() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Sfn send = new Sfn(true);
            send.connect("127.0.0.1", DEFAULT_PORT_NUMBER);
            File sendDir = new File("/Users/solkin/Pictures/Test");
            send.sendFiles(Objects.requireNonNull(sendDir.listFiles()));
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
