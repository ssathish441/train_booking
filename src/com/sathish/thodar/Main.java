package com.sathish.thodar;

import com.sathish.thodar.features.auth.AuthView;

public class Main {

    public static final String VERSION_NAME = "1.0.1";


    public static void main(String[] args) {
        new com.sathish.thodar.features.filemanagement.FileView().autoLoadOnStartup();

        System.out.println("       Welcome to Thodar      ");
        System.out.println("=========================================");
        new AuthView().showLandingMenu();
    }
}