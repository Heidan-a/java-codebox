package com.example.ojsandbox.unsafecode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ReadFileError {
    public static void main(String[] args) throws IOException {
       String userDir = System.getProperty("user.dir");
       String filePath = userDir + File.separator + "src/main/resources/application.yml";
       List<String> allLine = Files.readAllLines(Paths.get(filePath));
        System.out.println(String.join("\n",allLine));
    }
}