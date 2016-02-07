package com.finalproject.brickbreaker.services;

public class Settings {
    public static final int MAX_COLUMS = 10;
    public static final int MAX_ROWS = 12;
    public static final int MAX_LOCKED_LEVELS = 8;

    public static int getTopBorder(int screenHeight){
        return (int) (screenHeight * 0.15f);
    }
}
