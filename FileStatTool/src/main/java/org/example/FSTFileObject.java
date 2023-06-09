package org.example;

public class FSTFileObject {
    boolean isTestFile;

    public boolean isTestFile() {
        return isTestFile;
    }

    public void setTestFile(boolean testFile) {
        isTestFile = testFile;
    }

    public boolean isSourceFile() {
        return isSourceFile;
    }

    public void setSourceFile(boolean sourceFile) {
        isSourceFile = sourceFile;
    }

    boolean isSourceFile;
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    String fileName;
    long rowCount;

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    boolean isFile;

}
