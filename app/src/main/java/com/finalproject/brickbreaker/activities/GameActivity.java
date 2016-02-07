package com.finalproject.brickbreaker.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.interfaces.ICurrentLevelChangedListener;
import com.finalproject.brickbreaker.interfaces.IOnLevelAddedListener;
import com.finalproject.brickbreaker.managers.LevelsManager;
import com.finalproject.brickbreaker.managers.LevelsPatternsManager;
import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.services.BrickTypes;
import com.finalproject.brickbreaker.services.BrickTypesHelper;
import com.finalproject.brickbreaker.services.Settings;
import com.finalproject.brickbreaker.models.Button;
import com.finalproject.brickbreaker.models.Instance;
import com.finalproject.brickbreaker.models.Screen;
import com.finalproject.brickbreaker.models.Sprite;

public class GameActivity extends Screen  implements IOnLevelAddedListener {

	//paints
	Paint Title_Paint = new Paint();

	Paint Gameover_Score_Paint = new Paint();
	Paint Instruction_Paint = new Paint();
	Paint Black_shader = new Paint();
	Paint Black_dark_shader = new Paint();

	//background
	Bitmap background;

	Sprite wall_sprite;

	//ball and bat
	Instance bat;
	ArrayList<Instance> balls = new ArrayList<Instance>();
	ArrayList<Integer> infinite_loop_timer = new ArrayList<Integer>();

	//states
	final int MENU = 0, GAMEPLAY = 1, LEVELMENU = 2, GAMEOVER = 3;
	int state = MENU;
	boolean pause = false, notstarted = true, firstTimerUpdateRemoved = false;

	//menu buttons
	Button btn_Play, btn_Replay, btn_pause, btn_Next;

	int top_border, side_borders;

	//time keeping
	private long now = SystemClock.elapsedRealtime(), lastTick;

	//TODO: Speed and score Controls.

	int maximum_lifes = 4;//the maximum lifes a user can get.
	int infiniteloop_timout = 10;//times the ball collides with black tiles before reset

	//int[] Top_scores;


	//score
	Sprite gamewon, gameover;


	//onscreen bricks
	Instance[][] bricks_current_level;

	//lives
	int lives_left;
	Sprite life;

	Typeface specialFont;

	//managers
	LevelsManager levelsManager;
	com.finalproject.brickbreaker.managers.AudioManager audioManager;



	public void OnLevelAdded(){
		levelsManager.LoadPatterns();
		levelsManager.populateLevelButtons(specialFont);

		if (state == LEVELMENU) {
			Levelmenu();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		audioManager = new com.finalproject.brickbreaker.managers.AudioManager(this);
		levelsManager = new LevelsManager(this, audioManager, new ICurrentLevelChangedListener() {
			@Override
			public void OnCurrentLevelChanged() {
				StartGame();
			}
		});

		levelsManager.LoadPatterns();

		LevelsPatternsManager.GetInstance(getBaseContext()).RegisterLevelAdded(this);

		//setDebugMode(true);
		initialiseAccelerometer();


	}

	@Override
	public void Start() {
		super.Start();
		//TODO: Change Fonts and font sizes from here
		//fonts
		specialFont = Typeface.createFromAsset(getAssets(), "forte.ttf");

		//set paints
		//title
		Title_Paint.setTextSize(dpToPx(38));
		Title_Paint.setAntiAlias(true);
		Title_Paint.setColor(getResources().getColor(R.color.blue));
		Title_Paint.setTypeface(specialFont);



		//gameover score Paint
		Gameover_Score_Paint.setTextSize(dpToPx(50));
		Gameover_Score_Paint.setAntiAlias(true);
		Gameover_Score_Paint.setColor(getResources().getColor(R.color.black));
		Gameover_Score_Paint.setTypeface(specialFont);

		//Wall Instruction Paint
		Instruction_Paint.setTextSize(dpToPx(38));
		Instruction_Paint.setAntiAlias(true);
		Instruction_Paint.setColor(getResources().getColor(R.color.trans_black));
		Instruction_Paint.setTypeface(specialFont);

		//shaders
		Black_shader.setColor(getResources().getColor(R.color.black));
		Black_dark_shader.setColor(getResources().getColor(R.color.black_dark));

		//Global stuff_____________________________________________________________________________
		audioManager.initialize();
		levelsManager.initialize();

		top_border = (int) Settings.getTopBorder(ScreenHeight());

		//initialise borders
		side_borders = dpToPx(5);

		//initialise wall sprite
		wall_sprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.bluewall), ScreenHeight() * 0.18f, true);



		//Menu stuff________________________________________________________________________________
		//play button
		btn_Play = new Button(getResources().getString(R.string.Play), ScreenWidth() / 10, specialFont, getResources().getColor(R.color.black), ScreenWidth() / 2, ScreenHeight() * 0.45f, this, false);
		btn_Play.x = ScreenWidth() / 2 - btn_Play.getWidth() / 2;

		//highscores button
		//btn_Highscores = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.highscore), ScreenWidth() * 0.17f), 0, ScreenHeight() * 0.63f, this, false);
		//btn_Highscores.x = ScreenWidth() / 2 - btn_Highscores.getWidth() / 2;
		//btn_Highscores.y = ScreenHeight() - (wall_sprite.getHeight() / 2) - (btn_Highscores.getHeight() / 2);


		levelsManager.populateLevelButtons(specialFont);



		//replay button
		btn_Replay = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.replay), ScreenWidth() * 0.13f), 0, 0, this, false);
		btn_Replay.x = ScreenWidth() / 2 - btn_Replay.getWidth() * 2f;
		btn_Replay.y = ScreenHeight() - (wall_sprite.getHeight() / 2) - (btn_Replay.getHeight() / 2);

		//next button
		btn_Next = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.next), ScreenWidth() * 0.12f), 0, 0, this, false);
		btn_Next.x = ScreenWidth() / 2 + btn_Next.getWidth();
		btn_Next.y = ScreenHeight() - (wall_sprite.getHeight() / 2) - (btn_Next.getHeight() / 2);

		//gameplay stuff________________________________________________________________________________

		//bat
		bat = new Instance(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.bat), ScreenWidth() * 0.2f), 0, 0, this, false);
		bat.x = ScreenWidth() / 2 - bat.getWidth() / 2;
		bat.y = ScreenHeight() - (wall_sprite.getHeight()) - (bat.getHeight() * 1.2f);

		//pause button
		btn_pause = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.pause), ScreenWidth() * 0.08f), 0, 0, this, false);
		btn_pause.x = ScreenWidth() / 2 - btn_pause.getWidth() / 2;
		btn_pause.y = (top_border / 4) - btn_pause.getHeight() * 0.5f;

		//life sprite
		life = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.life), top_border * 0.2f);

		//set world origin
		setOrigin(BOTTOM_LEFT);

		//initialise score image
		gamewon = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.score), ScreenWidth() * 0.3f);
		gameover = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.gameover), ScreenWidth() * 0.3f);

		//state = MENU;

		//just in case of ad refresh
		if (state == LEVELMENU) {
			Levelmenu();
		}

	}

	public boolean between(float x_y, float width_height, float ball_x_y, int ball_width_height) {
		RectF a = new RectF(x_y, 0, x_y + width_height, 1);
		RectF b = new RectF(ball_x_y, 0, ball_x_y + ball_width_height, 1);

		return a.intersect(b);
	}

	@Override
	synchronized public void Step() {
		super.Step();
		if (state == MENU) {

		} else if (state == GAMEPLAY) {

			//things to pause
			if (!notstarted && !pause) {

				bat.Update();
				for (int i = 0; i < balls.size(); i++) {

					//test if ball is stuck
					if (infinite_loop_timer.get(i) > infiniteloop_timout) {
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
					if (balls.get(i).x < side_borders) {
						balls.get(i).speedx = Math.abs(balls.get(i).speedx);
					}
					//side border right
					if (balls.get(i).x + (balls.get(i).getWidth()) > ScreenWidth() - side_borders) {
						balls.get(i).speedx = -Math.abs(balls.get(i).speedx);
					}

					//collision to bricks
					//draw bricks
					BrickTypes[][] currentBrickPattern = levelsManager.getCurrentBrickPattern();
					for (int y = 0; y < currentBrickPattern.length; y++) {
						for (int x = 0; x < currentBrickPattern[0].length; x++) {
							if (bricks_current_level[x][y] != null) {
								if (balls.get(i).CollidedWith(bricks_current_level[x][y])) {

									//									//top bottom collision
									//									if (balls.get(i).y > bricks_current_level[x][y].y) {
									//										//ball collided from bottom of block
									//										balls.get(i).speedy = Math.abs(balls.get(i).speedy);
									//									} else {
									//										//ball collided from top of block
									//										balls.get(i).speedy = -Math.abs(balls.get(i).speedy);
									//									}
									//									//left right collision
									//									if (balls.get(i).x > bricks_current_level[x][y].x && balls.get(i).x < bricks_current_level[x][y].x) {
									//										//ball collided from right of block
									//										balls.get(i).speedx = Math.abs(balls.get(i).speedx);
									//
									//									} else {
									//										//ball collided from left of block
									//										balls.get(i).speedx = -Math.abs(balls.get(i).speedx);
									//									}

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

									//balls.get(i).speedx = -balls.get(i).speedx;

									//brick specific code
									if (bricks_current_level[x][y].type == BrickTypes.Normal1 || bricks_current_level[x][y].type == BrickTypes.Normal2 || bricks_current_level[x][y].type == BrickTypes.Normal3 || bricks_current_level[x][y].type == BrickTypes.Normal4) {
										//collided to brick 1, 2, 3, 4
										bricks_current_level[x][y] = null;

										audioManager.playBounce();

									} else if (bricks_current_level[x][y].type == BrickTypes.Wall) {
										//collided to special brick 1 - black brick
										infinite_loop_timer.set(i, infinite_loop_timer.get(i) + 1);
									} else if (bricks_current_level[x][y].type == BrickTypes.Big) {
										//collided to special brick 2 - enlarge board
										//bat
										bat.sprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.bat), ScreenWidth() * 0.3f);
										bat.x = ScreenWidth() / 2 - bat.getWidth() / 2;
										bat.y = ScreenHeight() - (wall_sprite.getHeight()) - (bat.getHeight() * 1.2f);
										bricks_current_level[x][y] = null;

										audioManager.playBounce();

									} else if (bricks_current_level[x][y].type == BrickTypes.Ball) {
										//collided to special brick 3 - add ball
										bricks_current_level[x][y] = null;
										//add ball
										add_ball();

										audioManager.playBounce();
									} else if (bricks_current_level[x][y].type == BrickTypes.Life) {
										//collided to special brick 3 - add life
										bricks_current_level[x][y] = null;
										if (lives_left < maximum_lifes)
											lives_left++;

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
					if (balls.get(i).y > ScreenHeight()) {
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

	}

	@Override
	public synchronized void onAccelerometer(PointF point) {

	}

	@Override
	public synchronized void BackPressed() {
		if (state == GAMEPLAY) {
			audioManager.stopMusic();
			Levelmenu();
		} else if (state == LEVELMENU) {
			state = MENU;
		} else if (state == MENU) {
			audioManager.stopMusic();
			Exit();

		} else if (state == GAMEOVER) {
			state = MENU;
		}
	}

	@Override
	public synchronized void onTouch(float TouchX, float TouchY, MotionEvent event) {
		audioManager.onTouch(event,pause,state == GAMEPLAY);

		if (state == MENU) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (btn_Play.isTouched(event)) {
					btn_Play.Highlight(getResources().getColor(R.color.red));
				}
				//if (btn_Highscores.isTouched(event)) {
				//	btn_Highscores.Highlight(getResources().getColor(R.color.red));
				//}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				//refresh all
				btn_Play.LowLight();
				//btn_Highscores.LowLight();

				if (btn_Play.isTouched(event)) {
					audioManager.playBounce();
					Levelmenu();
				}
				//if (btn_Highscores.isTouched(event)) {
				//	if (sound_bounce != 0 && !sound_muted)
				//		sp.play(sound_bounce, 1, 1, 0, 0, 1);
				// open leaderboard
				//}
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {

			}
		} else if (state == LEVELMENU) {
			levelsManager.onTouch(event);
		} else if (state == GAMEOVER) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				//refresh all

				if (btn_Replay.isTouched(event)) {
					StartGame();
					audioManager.playBounce();
				}

				if (btn_Next.isTouched(event)) {
					levelsManager.advanceToNextLevel();
				}
			}
		} else if (state == GAMEPLAY) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (btn_pause.isTouched(event)) {
					btn_pause.Highlight(getResources().getColor(R.color.red));
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
	}

	//..................................................Game Functions..................................................................................................................................

	public void StartGame() {
		//refresh score
		levelsManager.updateCurrentLevelScore(0);

		//refresh camera
		cameraY = 0;

		//not started
		notstarted = true;
		firstTimerUpdateRemoved = false;
		state = GAMEPLAY;
		PlayMusic();

		//refresh ball
		balls.clear();
		infinite_loop_timer.clear();
		add_ball();

		//refresh bat
		bat.sprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.bat), ScreenWidth() * 0.2f);
		bat.x = ScreenWidth() / 2 - bat.getWidth() / 2;
		bat.y = ScreenHeight() - (wall_sprite.getHeight()) - (bat.getHeight() * 1.2f);

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
					Sprite brick = new Sprite(BitmapFactory.decodeResource(getResources(),  BrickTypesHelper.GetImageId(currentBrickPattern[y][x])), (ScreenWidth() * 0.1f) - ((float) side_borders / 5));
					bricks_current_level[x][y] = new Instance(brick, x * brick.getWidth() + side_borders, (y * brick.getHeight()) + top_border, this, false, currentBrickPattern[y][x]);
				}
			}
		}

		//pause off
		pause = false;
	}

	public void Levelmenu() {
		state = LEVELMENU;
		levelsManager.updateLevels();
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
		state = GAMEOVER;

	}

	public void pause() {
		if (state == GAMEPLAY && !notstarted) {
			pause = true;
			audioManager.stopMusic();
		}
	}

	public void togglePause() {
		if (state == GAMEPLAY) {
			if (pause) {
				pause = false;
				if (!audioManager.isMusicMuted())
					PlayMusic();
				firstTimerUpdateRemoved = false;
			} else {
				pause();
			}
		}
	}

	private void PlayMusic(){
		audioManager.playMusic(state == GAMEPLAY);
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

	void add_ball() {
		balls.add(new Instance(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.ball), ScreenWidth() * 0.04f), 0, 0, this, false));
		balls.get(balls.size() - 1).x = ScreenWidth() / 2 - balls.get(0).getWidth() / 2;
		balls.get(balls.size() - 1).y = ScreenHeight() * 0.7f;
		balls.get(balls.size() - 1).speedy = -dpToPx(10);
		balls.get(balls.size() - 1).speedx = 0;
		infinite_loop_timer.add(0);
	}

	//...................................................Rendering of screen............................................................................................................................
	@Override
	public void Draw(Canvas canvas) {
		//draw background
		renderBackground(canvas);

		//draw background
		for (int x = 0; x < (ScreenWidth() / wall_sprite.getWidth()) + 1; x++) {
			wall_sprite.draw(canvas, x * wall_sprite.getWidth(), ScreenHeight() - wall_sprite.getHeight());
		}

		//draw borders
		canvas.drawRect(0, 0, side_borders, ScreenHeight(), Black_shader);
		canvas.drawRect(ScreenWidth() - side_borders, 0, ScreenWidth(), ScreenHeight(), Black_shader);
		canvas.drawRect(0, 0, ScreenWidth(), top_border, Black_shader);
		canvas.drawRect(0, 0, ScreenWidth(), top_border / 2, Black_dark_shader);

		if (state == MENU) {
			//draw title
			Rect Title_Paint_bounds = new Rect();
			Title_Paint.getTextBounds(getResources().getString(R.string.app_name), 0, getResources().getString(R.string.app_name).length(), Title_Paint_bounds);
			canvas.drawText(getResources().getString(R.string.app_name), (ScreenWidth() / 2) - (Title_Paint_bounds.width() / 2), (top_border * 0.75f) + (Title_Paint_bounds.height() / 2), Title_Paint);
			//draw buttons
			btn_Play.draw(canvas);
			//btn_Highscores.draw(canvas);

		} else if (state == LEVELMENU) {
			levelsManager.draw(canvas, Title_Paint);
		} else if (state == GAMEPLAY) {

			for (int i = 0; i <= lives_left; i++) {
				life.draw(canvas, ScreenWidth() - (i * life.getWidth() * 1.5f), (top_border * 0.75f) - (life.getHeight() / 2));
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
			Title_Paint.getTextBounds(scoreDisplay, 0, scoreDisplay.length(), Title_Paint_bounds);
			canvas.drawText(scoreDisplay, 0, (top_border * 0.75f) + (Title_Paint_bounds.height() / 2), Title_Paint);

			//pause button
			btn_pause.draw(canvas);

		} else if (state == GAMEOVER) {
			if (lives_left > 0) {
				//level passed
				Rect Title_Paint_bounds = new Rect();
				Title_Paint.getTextBounds(getResources().getString(R.string.Level_passed), 0, getResources().getString(R.string.Level_passed).length(), Title_Paint_bounds);
				canvas.drawText(getResources().getString(R.string.Level_passed), (ScreenWidth() / 2) - (Title_Paint_bounds.width() / 2), (top_border * 0.75f) + (Title_Paint_bounds.height() / 2), Title_Paint);
				gamewon.draw(canvas, (ScreenWidth() / 2) - (gamewon.getWidth() / 2), (float) (ScreenHeight() * 0.30));
			} else {
				//game over 
				Rect Title_Paint_bounds = new Rect();
				Title_Paint.getTextBounds(getResources().getString(R.string.game_over), 0, getResources().getString(R.string.game_over).length(), Title_Paint_bounds);
				canvas.drawText(getResources().getString(R.string.game_over), (ScreenWidth() / 2) - (Title_Paint_bounds.width() / 2), (top_border * 0.75f) + (Title_Paint_bounds.height() / 2), Title_Paint);
				gameover.draw(canvas, (ScreenWidth() / 2) - (gameover.getWidth() / 2), (float) (ScreenHeight() * 0.25));
			}
			String scoreDisplay = levelsManager.getCurrentScoreDisplay();
			canvas.drawText(scoreDisplay, (ScreenWidth() / 2) - (Gameover_Score_Paint.measureText(scoreDisplay) / 2), (float) (ScreenHeight() * 0.55), Gameover_Score_Paint);

			btn_Replay.draw(canvas);
			btn_Next.draw(canvas);

		}
		//draw sound buttons
		audioManager.draw(canvas);


		super.Draw(canvas);
	}

	//Rendering of background
	public void renderBackground(Canvas canvas) {
		//draw background
		canvas.drawColor(getResources().getColor(R.color.blue));
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		pause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
