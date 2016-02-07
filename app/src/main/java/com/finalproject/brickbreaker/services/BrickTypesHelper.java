package com.finalproject.brickbreaker.services;

import com.finalproject.brickbreaker.R;

public class BrickTypesHelper{
    public static int GetImageId(BrickTypes type){
        switch (type){
            case Empty:
                return R.drawable.brick0;
            case Normal1:
                return R.drawable.brick1;
            case Normal2:
                return R.drawable.brick2;
            case Normal3:
                return R.drawable.brick3;
            case Normal4:
                return R.drawable.brick4;
            case Wall:
                return R.drawable.special1;
            case Big:
                return R.drawable.special2;
            case Ball:
                return R.drawable.special3;
            case Life:
                return R.drawable.special4;
        }
        return 0;
    }

    public static String GetDisplayName(BrickTypes type){
        switch (type){
            case Empty:
                return "Empty";
            case Normal1:
                return "Yellow";
            case Normal2:
                return "Green";
            case Normal3:
                return "Purple";
            case Normal4:
                return "Red";
            case Wall:
                return "Wall";
            case Big:
                return "Big Bat";
            case Ball:
                return "Add Ball";
            case Life:
                return "Add Life";
        }
        return "";
    }
}
