package com.finalproject.brickbreaker.managers;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.services.BrickTypes;
import com.finalproject.brickbreaker.interfaces.IInitiateGameListener;
import com.finalproject.brickbreaker.activities.LevelBuilderActivity;
import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.services.Settings;
import com.finalproject.brickbreaker.models.level_button;
import com.finalproject.brickbreaker.models.Screen;
import com.finalproject.brickbreaker.models.SingleScore;

import java.util.ArrayList;

public class LevelsManager {

    ArrayList<BrickTypes[][]> Brick_Pattern;
    level_button[] Level_Buttons;
    int[] score_times;
    private LevelsPatternsManager levelsPatternsManager;
    int level_circles_border_dp;
    private Screen screen;
    private AudioManager audioManager;
    private IInitiateGameListener initiateGameListener;
    Paint SubTitle_Paint = new Paint();
    int level_circles_border = 30;//level menu circles border
    int currentLevel = 0;
    SingleScore scoreManager;
    int unlocked = 12145;//just an unlock code to store in preferences

    public LevelsManager(Screen screen, AudioManager audioManager, IInitiateGameListener initiateGameListener){
        this.screen = screen;
        this.audioManager = audioManager;
        this.initiateGameListener = initiateGameListener;
        this.levelsPatternsManager = LevelsPatternsManager.GetInstance(screen.getBaseContext());
    }

    public void initialize(){

        //level menu score
        SubTitle_Paint.setTextSize(screen.dpToPx(20));
        SubTitle_Paint.setAntiAlias(true);
        SubTitle_Paint.setColor(screen.getResources().getColor(R.color.red));
        SubTitle_Paint.setTypeface(Typeface.DEFAULT_BOLD);

        scoreManager = new SingleScore(screen);

        //unlock level 1
        scoreManager.save_localscore_simple(unlocked, "unlock" + 0);

        level_circles_border_dp = screen.dpToPx(level_circles_border);
    }

    public void LoadPatterns(){
        Brick_Pattern = levelsPatternsManager.GetLevelsPatterns();
        Level_Buttons = new level_button[Brick_Pattern.size() +1];
        score_times = new int[Brick_Pattern.size()];
    }

    public void populateLevelButtons(Typeface font) {
        int levelsNum = Brick_Pattern.size() + 1;
        int circles_in_x = (int) Math.ceil(Math.sqrt(levelsNum));
        int circles_in_y = (int) Math.ceil((float) levelsNum / (float) circles_in_x);
        int radius = (int) (((screen.ScreenWidth() - ((float) level_circles_border_dp * (float) (circles_in_x + 1))) / (float) circles_in_x) / 2);

        int total_circles_height = (circles_in_y * ((radius * 2) + level_circles_border_dp)) + level_circles_border_dp;

        int current_lvl = 1;
        for (int y = 0; y < circles_in_y; y++) {
            for (int x = 0; x < circles_in_x; x++) {
                if (current_lvl <=levelsNum){
                    String text = current_lvl ==  levelsNum ? "+" : String.valueOf(current_lvl);

                    Level_Buttons[current_lvl - 1] = new level_button(text, (int) (radius / 1.5f), font,screen.getResources().getColor(R.color.black), level_circles_border_dp + (x * ((radius * 2) + level_circles_border_dp)), (screen.ScreenHeight() / 2) - (total_circles_height / 2) + (level_circles_border_dp + (y * ((radius * 2) + level_circles_border_dp))), radius, screen.getResources().getColor(R.color.green_bright), screen, SubTitle_Paint);
                }
                current_lvl++;
            }
        }
    }

    public BrickTypes[][] getCurrentBrickPattern(){
        return Brick_Pattern.get(currentLevel);
    }

    public void updateCurrentLevelScore(int value){
        score_times[currentLevel] += value;
    }

    public void onTouch(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < Level_Buttons.length; i++) {
                if (Level_Buttons[i].isTouched(event)) {
                    Level_Buttons[i].Highlight(screen.getResources().getColor(R.color.red));
                }
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //refresh all
            for (int i = 0; i < Level_Buttons.length; i++) {
                Level_Buttons[i].LowLight();
                if (Level_Buttons[i].isTouched(event)) {

                    if(Level_Buttons.length - 1 == i){
                        //add level button
                        Intent intent = new Intent(screen,LevelBuilderActivity.class);
                        screen.startActivity(intent);
                    }
                    else if (!Level_Buttons[i].isLocked()) {
                        currentLevel = i;
                        initiateGameListener.startGame();
                        audioManager.playBounce();
                    } else {
                        audioManager.playReject();

                    }
                }
            }
        }
    }

    public void updateLevels(){
        //get top scores
        for (int i = 0; i < Level_Buttons.length; i++) {
            Level_Buttons[i].setTopScore(scoreManager.load_localscore_simple("" + i));
            boolean isLocked =  i<Settings.MAX_LOCKED_LEVELS && scoreManager.load_localscore_simple("unlock" + i) != unlocked;
            Level_Buttons[i].setLock(isLocked);
            System.out.println(scoreManager.load_localscore_simple("unlock" + i));
        }
    }

    public void advanceToNextLevel(){
        updateLevels();

        //go to next level
        if (Level_Buttons.length - 1 > currentLevel + 1) {
            if (!Level_Buttons[currentLevel + 1].isLocked()) {
                currentLevel = currentLevel + 1;
                initiateGameListener.startGame();
                audioManager.playBounce();
            } else {
                audioManager.playReject();
            }
        } else {
            audioManager.playReject();
        }
    }


    public void onCurrentLevelPassed(){
            //success! - game passed - save score
            scoreManager.save_localscore_smaller(score_times[currentLevel], "" + currentLevel);

            if (Level_Buttons.length >= currentLevel + 1)
                scoreManager.save_localscore_simple(unlocked, "unlock" + (currentLevel + 1));
    }


    public void draw(Canvas canvas, Paint paint){
        for (int i = 0; i < Level_Buttons.length; i++) {
            Level_Buttons[i].draw(canvas);
        }

        //draw title
        Rect Title_Paint_bounds = new Rect();
        paint.getTextBounds(screen.getResources().getString(R.string.SelectLevel), 0, screen.getResources().getString(R.string.SelectLevel).length(), Title_Paint_bounds);
        canvas.drawText(screen.getResources().getString(R.string.SelectLevel), (screen.ScreenWidth() / 2) - (Title_Paint_bounds.width() / 2), (Settings.getTopBorder(screen.ScreenHeight()) * 0.75f) + (Title_Paint_bounds.height() / 2), paint);
    }

    public String getCurrentScoreDisplay(){
        return integerToScore(score_times[currentLevel]);
    }

    public String integerToScore(int score) {
        return "Score: " + ((double) score / 1000) + screen.getResources().getString(R.string.time_suffix);
    }

}
