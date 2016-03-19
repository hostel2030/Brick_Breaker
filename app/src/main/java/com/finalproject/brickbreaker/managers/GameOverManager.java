package com.finalproject.brickbreaker.managers;


import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.interfaces.IInitiateGameListener;
import com.finalproject.brickbreaker.models.Button;
import com.finalproject.brickbreaker.models.Screen;
import com.finalproject.brickbreaker.models.ScaledImage;
import com.finalproject.brickbreaker.services.Settings;

public class GameOverManager {

    ScaledImage gamewon, gameover;
    Button btn_Replay, btn_Next;
    private Screen screen;
    private AudioManager audioManager;
    private LevelsManager levelsManager;
    private GameplayManager gameplayManager;
    private IInitiateGameListener initiateGameListener;

    Paint Gameover_Score_Paint = new Paint();

    public GameOverManager(Screen screen, AudioManager audioManager, LevelsManager levelsManager, GameplayManager gameplayManager, IInitiateGameListener initiateGameListener){

        this.screen = screen;
        this.audioManager = audioManager;
        this.levelsManager = levelsManager;
        this.gameplayManager = gameplayManager;
        this.initiateGameListener = initiateGameListener;
    }

    public void initialize(ScaledImage relativeSprite, Typeface font){
        //replay button
        btn_Replay = new Button(new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.replay), screen.ScreenWidth() * 0.13f), 0, 0, screen);
        btn_Replay.x = screen.ScreenWidth() / 2 - btn_Replay.getWidth() * 2f;
        btn_Replay.y = screen.ScreenHeight() - (relativeSprite.getHeight() / 2) - (btn_Replay.getHeight() / 2);

        //next button
        btn_Next = new Button(new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.next), screen.ScreenWidth() * 0.12f), 0, 0, screen);
        btn_Next.x = screen.ScreenWidth() / 2 + btn_Next.getWidth();
        btn_Next.y = screen.ScreenHeight() - (relativeSprite.getHeight() / 2) - (btn_Next.getHeight() / 2);

        //initialize score image
        gamewon = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.score), screen.ScreenWidth() * 0.3f);
        gameover = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.gameover), screen.ScreenWidth() * 0.3f);

        //gameover score Paint
        Gameover_Score_Paint.setTextSize(screen.dpToPx(50));
        Gameover_Score_Paint.setAntiAlias(true);
        Gameover_Score_Paint.setColor(screen.getResources().getColor(R.color.black));
        Gameover_Score_Paint.setTypeface(font);
    }

    public void onTouch(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //refresh all

            if (btn_Replay.isTouched(event)) {
                initiateGameListener.startGame();
                audioManager.playBounce();
            }

            if (btn_Next.isTouched(event)) {
                levelsManager.advanceToNextLevel();
            }
        }
    }

    public void draw(Canvas canvas, Paint paint){
        if (gameplayManager.getLivesLeft() > 0) {
            //level passed
            Rect Title_Paint_bounds = new Rect();
            paint.getTextBounds(screen.getResources().getString(R.string.Level_passed), 0, screen.getResources().getString(R.string.Level_passed).length(), Title_Paint_bounds);
            canvas.drawText(screen.getResources().getString(R.string.Level_passed), (screen.ScreenWidth() / 2) - (Title_Paint_bounds.width() / 2), (Settings.getTopBorder(screen.ScreenHeight()) * 0.75f) + (Title_Paint_bounds.height() / 2), paint);
            gamewon.draw(canvas, (screen.ScreenWidth() / 2) - (gamewon.getWidth() / 2), (float) (screen.ScreenHeight() * 0.30));
        } else {
            //game over
            Rect Title_Paint_bounds = new Rect();
            paint.getTextBounds(screen.getResources().getString(R.string.game_over), 0, screen.getResources().getString(R.string.game_over).length(), Title_Paint_bounds);
            canvas.drawText(screen.getResources().getString(R.string.game_over), (screen.ScreenWidth() / 2) - (Title_Paint_bounds.width() / 2), (Settings.getTopBorder(screen.ScreenHeight()) * 0.75f) + (Title_Paint_bounds.height() / 2), paint);
            gameover.draw(canvas, (screen.ScreenWidth() / 2) - (gameover.getWidth() / 2), (float) (screen.ScreenHeight() * 0.25));
        }
        String scoreDisplay = levelsManager.getCurrentScoreDisplay();
        canvas.drawText(scoreDisplay, (screen.ScreenWidth() / 2) - (Gameover_Score_Paint.measureText(scoreDisplay) / 2), (float) (screen.ScreenHeight() * 0.55), Gameover_Score_Paint);

        btn_Replay.draw(canvas);
        btn_Next.draw(canvas);
    }

}
