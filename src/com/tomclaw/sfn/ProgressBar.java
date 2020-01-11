package com.tomclaw.sfn;

public class ProgressBar {

    private String fileName;
    private long fileSize;
    private long totalRead;
    private int percent;
    private static final char[] trailer = new char[42];
    private static final char[] progress = new char[22];

    public void setupBar(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public void showBar(long totalRead) {
        int percent = (int) ((totalRead * 100) / fileSize);
        String metrics = "Byte";
        if (totalRead < 1024) {
            metrics = "Byte";
        }
        if (totalRead >= 1024) {
            totalRead /= 1024;
            metrics = "KiB ";
        }
        if (totalRead >= 1024) {
            totalRead /= 1024;
            metrics = "MiB ";
        }
        if (totalRead >= 1024) {
            totalRead /= 1024;
            metrics = "GiB ";
        }
        if (totalRead >= 1024) {
            totalRead /= 1024;
            metrics = "TiB ";
        }
        if (this.totalRead != totalRead || this.percent != percent) {
            this.totalRead = totalRead;
            this.percent = percent;
            for (int c = 0; c < progress.length; c += 1) {
                if (c * 100 / progress.length <= percent) {
                    progress[c] = '#';
                } else {
                    progress[c] = '-';
                }
            }
            for (int c = 0; c < trailer.length; c += 1) {
                if (c < fileName.length()) {
                    trailer[c] = fileName.charAt(c);
                } else {
                    trailer[c] = ' ';
                }
            }
            System.out.printf(
                    " %s %4d %s [%s] %3d %%\r",
                    String.valueOf(trailer),
                    (int) totalRead,
                    metrics,
                    String.valueOf(progress),
                    percent
            );
        }
    }

    public void done() {
        showBar(fileSize);
        System.out.println();
    }

}
