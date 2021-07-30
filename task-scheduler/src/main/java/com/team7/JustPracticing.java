package com.team7;

import java.io.File;

public class JustPracticing {

    public static void main(String[] args) {
        File directory = new File("src/20testdotfiles");
        for (File file : directory.listFiles()) {
            System.out.println("file = " + file);
        }

    }
}
