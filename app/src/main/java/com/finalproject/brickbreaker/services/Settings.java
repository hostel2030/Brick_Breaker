package com.finalproject.brickbreaker.services;

import com.finalproject.brickbreaker.models.Screen;

public class Settings {
    public static final int MAX_COLUMS = 10;
    public static final int MAX_ROWS = 12;
    public static final int MAX_LOCKED_LEVELS = 8;
    public static final int MAX_LIVES = 4;
    public static final int INFINITELOOP_TIMEOUT = 4;

    public static int getTopBorder(int screenHeight){
        return (int) (screenHeight * 0.15f);
    }

    public static int getSideBorders(Screen screen){
        return screen.dpToPx(5);
    }
}
