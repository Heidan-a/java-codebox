package com.example.ojsandbox.unsafecode;

import java.util.ArrayList;
import java.util.List;

public class MemoryError {
    public static void main(String[] args) {
        List<byte[]> list = new ArrayList<>();
        while (true){
            list.add(new byte[10000]);
        }
    }
}
