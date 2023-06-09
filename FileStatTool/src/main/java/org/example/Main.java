package org.example;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Main {

    public static String autoDiscernEncoding(File file){
        String encoding = "GBK";
        try {
            Path path = Paths.get(file.getPath());
            byte[] data = Files.readAllBytes(path);
            CharsetDetector detector = new CharsetDetector();
            detector.setText(data);
            CharsetMatch match = detector.detect();
            if(match == null) return "null";
            encoding = match.getName();
            return encoding;
        } catch (IOException e) {

            return "null";
        }
    }
    public static void main(String[] args) {
        String fileName = new String("/Users/jasonwu/cockroach");
        File file = new File(fileName);
        List<FSTFileObject> fstFileObjects = new ArrayList<FSTFileObject>();
        traverseFolder(file,fstFileObjects);
        System.out.println(fstFileObjects.size());
        int iTestFileCount=0;
        long rowTestFileCount =0;
        int iSourceFileCount = 0;
        long rowSourceFileCount = 0;
        for(FSTFileObject fstFileObject:fstFileObjects) {
            if(fstFileObject.isSourceFile()){
                System.out.println(iSourceFileCount+"\t"+fstFileObject.getFileName()+"\t"+fstFileObject.isFile()+"\t"+ fstFileObject.getRowCount()+"\t" + "Source:"+fstFileObject.isSourceFile());
                iSourceFileCount++;
                rowSourceFileCount = rowSourceFileCount+fstFileObject.getRowCount();
            }else if(fstFileObject.isTestFile()) {
                //System.out.println(iTestFileCount+"\t"+fstFileObject.getFileName()+"\t"+fstFileObject.isFile()+"\t"+ fstFileObject.getRowCount()+"\t" + "Test:"+fstFileObject.isTestFile());
                iTestFileCount++;
                rowTestFileCount = rowTestFileCount+fstFileObject.getRowCount();
            }
        }
        System.out.println("Test File Count = "+iTestFileCount+"\t"+"Row Count = "+ rowTestFileCount);
        System.out.println("Source File Count = "+iSourceFileCount+"\t"+"Row Count = "+ rowSourceFileCount);

    }
    public static void traverseFolder(File file, List<FSTFileObject> fstFileObjects) {

            if (file.isDirectory()) {
                FSTFileObject fstFileObject = new FSTFileObject();
                fstFileObject.setFile(false);
                fstFileObject.setFileName(file.getAbsolutePath());
                fstFileObjects.add(fstFileObject);
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        traverseFolder(f, fstFileObjects);
                    }
                }
            } else {
                // 如果是文件，则输出文件名
                //try{
                    FSTFileObject fstFileObject = new FSTFileObject();
                    fstFileObject.setFile(true);
                    fstFileObject.setFileName(file.getAbsolutePath());

                    long row = getFileRowCout(file);
                    fstFileObject.setRowCount(row);
                    String encoding = autoDiscernEncoding(file);
                    if(isEncodingInBlacklist(encoding)) {
                        fstFileObject.setRowCount(0);
                    }else {
                        try{
                            if (!file.getName().contains(".")) {
                                fstFileObject.setRowCount(Files.lines(file.toPath(), Charset.forName(encoding)).count());
                                //  System.out.println(file + "\t\t" + Long.toString(Files.lines(file.toPath(), Charset.forName(encoding)).count()));
                            } else {
                                String postFix = file.getName().substring(file.getName().indexOf("."));
                                if (!inBlackList(postFix)) {
                                    fstFileObject.setRowCount(Files.lines(file.toPath(), Charset.forName(encoding)).count());
                                } else  fstFileObject.setRowCount(0);
                            }
                            if(isTestFile(file)) fstFileObject.setTestFile(true); else fstFileObject.setTestFile(false);
                            if(isSourceFile(file)) fstFileObject.setSourceFile(true); else fstFileObject.setSourceFile(false);

                        }catch(Exception ec){

                        }

                    }
                    fstFileObjects.add(fstFileObject);
            }
    }
    public static long getFileRowCout(File file){
        long rowCount = 0;
        try {

            String encoding = autoDiscernEncoding(file);
            //System.out.println("encoding = "+encoding);
            if (isEncodingInBlacklist(encoding)) return rowCount;
            if (!file.getName().contains(".")) {
                // System.out.println(file + "\t\t" + Long.toString(Files.lines(file.toPath(), Charset.forName(encoding)).count()));
            } else {
                String postFix = file.getName().substring(file.getName().indexOf("."));
                if (!inBlackList(postFix)) {
                    //   System.out.println(file + "\t\t" + Long.toString(Files.lines(file.toPath(), Charset.forName(encoding)).count()));
                }
                //else System.out.println(file.getName());
            }



        }catch(Exception ioException){

        }
        return rowCount;
    }
    public static boolean inBlackList(String postFix) {
        return Arrays.asList(badPostfix).contains(postFix);
    }
    public static boolean isEncodingInBlacklist(String encoding) {
        return Arrays.asList(blackListForEncoding).contains(encoding);
    }
    /*
    规则
    1. there is a "test/tests/testdata" keyword in path string
    2. there is a "_test.go" in file name;
    3. .bazel or .bzl file doesn't belong to either test or source files;
    4. there is a "." in path string
    5. file without suffix should not be test or source files;
    6. file under licenses/docs folder should not be source
    7. 如何判断三方库？是否要过滤掉？tiflash 还有 header 文件数量的问题；
     */
    public static boolean isTestFile(File file){
        String path = file.getPath();
        path = path.substring(0,path.lastIndexOf("/"));
       // System.out.println("path = "+ path);
        String name = file.getName();
       // System.out.println("name = "+ name);
        if(name.toLowerCase().contains(".bazel")||name.toLowerCase().contains(".bzl")) return false;
        if(!name.contains(".")) return false;
        if(name.toLowerCase().contains("_test.go")) return true;
        if(path.toLowerCase().contains("test")) return true;
        return false;
    }
    public static boolean isSourceFile(File file) {
        String path = file.getPath();
        path = path.substring(0,path.lastIndexOf("/"));
        // System.out.println("path = "+ path);
        String name = file.getName();
        if(isTestFile(file)) return false;
        if(path.contains("doc") || path.contains("license")||path.contains("build")) return false;
        if(name.toLowerCase().contains(".bazel")||name.toLowerCase().contains(".bzl")) return false;
        if(!name.contains(".")|| name.contains(".h")) return false;

        return true;
    }

    static String[] badPostfix = {".zip",".png",".jpeg",".snappy",".gz",".zst"};
    static String[] blackListForEncoding ={"Shift_JIS","UTF-8","windows-1251","EUC-KR","UTF-32LE", "windows-1252","UTF-16BE","Big5","windows-1250","null","UTF-16LE","windows-1253"};

}