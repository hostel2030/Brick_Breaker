package com.finalproject.brickbreaker.managers;


import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.models.Button;
import com.finalproject.brickbreaker.models.Instance;
import com.finalproject.brickbreaker.models.Screen;
import com.finalproject.brickbreaker.models.ScaledImage;
import com.finalproject.brickbreaker.services.BrickTypes;
import com.finalproject.brickbreaker.services.BrickTypesHelper;
import com.finalproject.brickbreaker.services.Settings;

import java.util.ArrayList;

public class GameplayManager {

    private int top_border;
    //lives
    int lives_left;
    ScaledImage life;


    boolean pause = false, notstarted = true, firstTimerUpdateRemoved = false;

    Button btn_pause;

    //onscreen bricks
    Instance[][] bricks_current_level;
    private Screen screen;
    private LevelsManager levelsManager;
    private AudioManager audioManager;
    private GameStateManager gameStateManager;

    //ball and bat
    Instance bat;
    ArrayList<Instance> balls = new ArrayList<Instance>();

    ArrayList<Integer> infinite_loop_timer = new ArrayList<Integer>();


    //time keeping
    private long now = SystemClock.elapsedRealtime(), lastTick;

    public GameplayManager(Screen screen, LevelsManager levelsManager, AudioManager audioManager, GameStateManager gameStateManager){

        this.screen = screen;
        this.levelsManager = levelsManager;
        this.audioManager = audioManager;
        this.gameStateManager = gameStateManager;
    }

    public void initialize(ScaledImage relativeSprite){

        top_border = Settings.getTopBorder(screen.ScreenHeight());

        //bat
        bat = new Instance(new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.bat), screen.ScreenWidth() * 0.2f), 0, 0, screen, BrickTypes.Empty);
        bat.x = screen.ScreenWidth() / 2 - bat.getWidth() / 2;
        bat.y = screen.ScreenHeight() - (relativeSprite.getHeight()) - (bat.getHeight() * 1.2f);

        //pause button
        btn_pause = new Button(new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.pause), screen.ScreenWidth() * 0.08f), 0, 0, screen);
        btn_pause.x = screen.ScreenWidth() / 2 - btn_pause.getWidth() / 2;
        btn_pause.y = (top_border / 4) - btn_pause.getHeight() * 0.5f;

        //life sprite
        life = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.life), top_border * 0.2f);
    }

    public void draw(Canvas canvas, Paint paint){
        for (int i = 0; i <= lives_left; i++) {
            life.draw(canvas, screen.ScreenWidth() - (i * life.getWidth() * 1.5f), (top_border * 0.75f) - (life.getHeight() / 2));
        }
        //draw bricks
        if (bricks_current_level != null) {
            for (int y = 0; y < bricks_current_level[0].length; y++) {
                for (int x = 0; x < bricks_current_level.length; x++) {
                    if (bricks_current_level[x][y] != null)
                        bricks_current_level[x][y].draw(canvas);
                }
            }
        }

        //balls and bat
        for (int i = 0; i < balls.size(); i++)
            balls.get(i).draw(canvas);
        bat.draw(canvas);

        //draw score
        String scoreDisplay = levelsManager.getCurrentScoreDisplay();
        Rect Title_Paint_bounds = new Rect();
        paint.getTextBounds(scoreDisplay, 0, scoreDisplay.length(), Title_Paint_bounds);
        canvas.drawText(scoreDisplay, 0, (top_border * 0.75f) + (Title_Paint_bounds.height() / 2), paint);

        //pause button
        btn_pause.draw(canvas);
    }


    public void step(ScaledImage relativeSprite){
        //things to pause
        if (!notstarted && !pause) {

            bat.Update();
            for (int i = 0; i < balls.size(); i++) {

                //test if ball is stuck
                if (infinite_loop_timer.get(i) > Settings.INFINITELOOP_TIMEOUT) {
                    //refresh ball
                    balls.remove(i);
                    infinite_loop_timer.remove(i);
                    add_ball();
                }

                balls.get(i).Update();
                //ball physics
                if (balls.get(i).CollidedWith(bat)) {
                    balls.get(i).speedx = -(bat.x + (bat.getWidth() / 2) - balls.get(i).x) / 4;
                    balls.get(i).speedy = -Math.abs(balls.get(i).speedy);
                    infinite_loop_timer.set(i, 0);
                }

                //top border
                if (balls.get(i).y < top_border) {
                    balls.get(i).speedy = Math.abs(balls.get(i).speedy);
                }
                //side border left
                if (balls.get(i).x < Settings.getSideBorders(screen)) {
                    balls.get(i).speedx = Math.abs(balls.get(i).speedx);
                }
                //side border right
                if (balls.get(i).x + (balls.get(i).getWidth()) > screen.ScreenWidth() - Settings.getSideBorders(screen)) {
                    balls.get(i).speedx = -Math.abs(balls.get(i).speedx);
                }

                //collision to bricks
                //draw bricks
                BrickTypes[][] currentBrickPattern = levelsManager.getCurrentBrickPattern();
                for (int y = 0; y < currentBrickPattern.length; y++) {
                    for (int x = 0; x < currentBrickPattern[0].length; x++) {
                        if (bricks_current_level[x][y] != null) {
                            if (balls.get(i).CollidedWith(bricks_current_level[x][y])) {

                                //ball collided from top of block
                                if (balls.get(i).speedy > 0 && between(bricks_current_level[x][y].y, bricks_current_level[x][y].getHeight() * 0.1f, balls.get(i).y, balls.get(i).getHeight())) {
                                    balls.get(i).speedy = -Math.abs(balls.get(i).speedy);
                                }
                                //ball collided from bottom of block
                                if (balls.get(i).speedy < 0 && between(bricks_current_level[x][y].y + bricks_current_level[x][y].getHeight() * 0.9f, bricks_current_level[x][y].getHeight() * 0.1f, balls.get(i).y, balls.get(i).getHeight())) {
                                    balls.get(i).speedy = Math.abs(balls.get(i).speedy);
                                }

                                //ball collided from left of block
                                if (balls.get(i).speedx > 0 && between(bricks_current_level[x][y].x, bricks_current_level[x][y].getWidth() * 0.1f, balls.get(i).x, balls.get(i).getWidth())) {
                                    balls.get(i).speedx = -Math.abs(balls.get(i).speedx);
                                } else
                                    //ball collided from right of block
                                    if (balls.get(i).speedx < 0 && between(bricks_current_level[x][y].x + bricks_current_level[x][y].getWidth() * 0.9f, bricks_current_level[x][y].getWidth() * 0.1f, balls.get(i).x, balls.get(i).getWidth())) {
                                        balls.get(i).speedx = Math.abs(balls.get(i).speedx);
                                    }

                                //brick specific code
                                if (bricks_current_level[x][y].type == BrickTypes.Normal1 || bricks_current_level[x][y].type == BrickTypes.Normal2 || bricks_current_level[x][y].type == BrickTypes.Normal3 || bricks_current_level[x][y].type == BrickTypes.Normal4) {
                                    //collided to brick 1, 2, 3, 4
                                    bricks_current_level[x][y] = null;

                                    infinite_loop_timer.set(i, 0);
                                    audioManager.playBounce();

                                } else if (bricks_current_level[x][y].type == BrickTypes.Wall) {
                                    //collided to special brick 1 - black brick
                                    infinite_loop_timer.set(i, infinite_loop_timer.get(i) + 1);
                                } else if (bricks_current_level[x][y].type == BrickTypes.Big) {
                                    //collided to special brick 2 - enlarge board
                                    //bat
                                    bat.sprite = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.bat), screen.ScreenWidth() * 0.3f);
                                    bat.x = screen.ScreenWidth() / 2 - bat.getWidth() / 2;
                                    bat.y = screen.ScreenHeight() - (relativeSprite.getHeight()) - (bat.getHeight() * 1.2f);
                                    bricks_current_level[x][y] = null;

                                    infinite_loop_timer.set(i, 0);
                                    audioManager.playBounce();

                                } else if (bricks_current_level[x][y].type == BrickTypes.Ball) {
                                    //collided to special brick 3 - add ball
                                    bricks_current_level[x][y] = null;
                                    //add ball
                                    add_ball();

                                    infinite_loop_timer.set(i, 0);
                                    audioManager.playBounce();
                                } else if (bricks_current_level[x][y].type == BrickTypes.Life) {
                                    //collided to special brick 3 - add life
                                    bricks_current_level[x][y] = null;
                                    if (lives_left < Settings.MAX_LIVES)
                                        lives_left++;

                                    infinite_loop_timer.set(i, 0);
                                    audioManager.playBounce();
                                }

                                //test if not level passed
                                if (isLevelPassed()) {
                                    GameOver();
                                }
                            }
                        }
                    }
                }

                //ball out of screen
                if (balls.get(i).y > screen.ScreenHeight()) {
                    if (!(balls.size() > 1)) {
                        //reduce life
                        lives_left--;
                        if (lives_left <= 0) {
                            GameOver();
                        }

                        //refresh ball
                        add_ball();

                        notstarted = true;

                        audioManager.playBallOut();

                    } else {
                        balls.remove(i);
                    }
                }
            }

            //update timer
            now = SystemClock.elapsedRealtime();
            if (now - lastTick > 10) {//every 10ms

                //add time to score
                if (firstTimerUpdateRemoved)
                    levelsManager.updateCurrentLevelScore((int) (now - lastTick));
                else
                    firstTimerUpdateRemoved = true;
                lastTick = SystemClock.elapsedRealtime();
            }

        }
    }


    public synchronized void GameOver() {
        if (lives_left > 0) {
            levelsManager.onCurrentLevelPassed();
            audioManager.playSuccess();
        } else {
            //game not passed
            audioManager.playGameOver();

        }

        audioManager.stopMusic();

        gameStateManager.state = GameStateManager.GameState.gameOver;
    }

    public boolean isLevelPassed() {
        BrickTypes[][] currentBrickPattern = levelsManager.getCurrentBrickPattern();
        for (int y = 0; y < currentBrickPattern.length; y++) {
            for (int x = 0; x < currentBrickPattern[0].length; x++) {
                if (bricks_current_level[x][y] != null) {
                    if (bricks_current_level[x][y].type != BrickTypes.Wall) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void add_ball() {
        balls.add(new Instance(new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.ball), screen.ScreenWidth() * 0.04f), 0, 0, screen, BrickTypes.Empty));
        balls.get(balls.size() - 1).x = screen.ScreenWidth() / 2 - balls.get(0).getWidth() / 2;
        balls.get(balls.size() - 1).y = screen.ScreenHeight() * 0.7f;
        balls.get(balls.size() - 1).speedy = -screen.dpToPx(10);
        balls.get(balls.size() - 1).speedx = 0;
        infinite_loop_timer.add(0);
    }

    public boolean between(float x_y, float width_height, float ball_x_y, int ball_width_height) {
        RectF a = new RectF(x_y, 0, x_y + width_height, 1);
        RectF b = new RectF(ball_x_y, 0, ball_x_y + ball_width_height, 1);

        return a.intersect(b);
    }

    public void StartGame(ScaledImage relativeSprite) {
        gameStateManager.state = GameStateManager.GameState.gameplay;

        //refresh score
        levelsManager.updateCurrentLevelScore(0);

        //not started
        notstarted = true;
        firstTimerUpdateRemoved = false;

        //refresh ball
        balls.clear();
        infinite_loop_timer.clear();
        add_ball();

        //refresh bat
        bat.sprite = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.bat), screen.ScreenWidth() * 0.2f);
        bat.x = screen.ScreenWidth() / 2 - bat.getWidth() / 2;
        bat.y = screen.ScreenHeight() - (relativeSprite.getHeight()) - (bat.getHeight() * 1.2f);

        BrickTypes[][] currentBrickPattern = levelsManager.getCurrentBrickPattern();

        //create bricks
        bricks_current_level = new Instance[currentBrickPattern.length][currentBrickPattern.length];

        //refresh lives
        lives_left = 3;

        //initialise bricks
        for (int y = 0; y <currentBrickPattern.length; y++) {
            for (int x = 0; x < currentBrickPattern[0].length; x++) {
                if (currentBrickPattern[y][x] == BrickTypes.Empty)
                    bricks_current_level[x][y] = null;
                else {
                    ScaledImage brick = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(),  BrickTypesHelper.GetImageId(currentBrickPattern[y][x])), (screen.ScreenWidth() * 0.1f) - ((float) Settings.getSideBorders(screen) / 5));
                    bricks_current_level[x][y] = new Instance(brick, x * brick.getWidth() + Settings.getSideBorders(screen), (y * brick.getHeight()) + top_border, screen, currentBrickPattern[y][x]);
                }
            }
        }

        //pause off
        pause = false;

        audioManager.playMusic();
    }

    public boolean isGamePaused(){
        return pause;
    }

    public void onTouch(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (btn_pause.isTouched(event)) {
                btn_pause.Highlight(screen.getResources().getColor(R.color.red));
            }

            //start game
            if (notstarted) {
                notstarted = false;
                firstTimerUpdateRemoved = false;
            }

            //turn off pause
            if (pause) {
                togglePause();
            }

        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            btn_pause.LowLight();

            if (btn_pause.isTouched(event)) {
                if (!pause) {
                    togglePause();
                    audioManager.playBounce();
                }
            }
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE && !btn_pause.isTouched(event) && audioManager.areButtonsTouched(event)) {
            bat.x = event.getX() - bat.getWidth() / 2;
        }
    }

    public void pause() {
        if (gameStateManager.state == GameStateManager.GameState.gameplay && !notstarted) {
            pause = true;
            audioManager.stopMusic();
        }
    }

    public void togglePause() {
        if (gameStateManager.state == GameStateManager.GameState.gameplay) {
            if (pause) {
                pause = false;
                if (!audioManager.isMusicMuted())
                    audioManager.playMusic();
                firstTimerUpdateRemoved = false;
            } else {
                pause();
            }
        }
    }

    public int getLivesLeft(){
        return lives_left;
    }

}
