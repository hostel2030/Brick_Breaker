package com.finalproject.brickbreaker.activities;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.interfaces.ICurrentLevelChangedListener;
import com.finalproject.brickbreaker.interfaces.IOnLevelAddedListener;
import com.finalproject.brickbreaker.managers.GameStateManager;
import com.finalproject.brickbreaker.managers.GameplayManager;
import com.finalproject.brickbreaker.managers.LevelsManager;
import com.finalproject.brickbreaker.managers.LevelsPatternsManager;
import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.services.Settings;
import com.finalproject.brickbreaker.models.Button;
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


	//menu buttons
	Button btn_Play, btn_Replay, btn_Next;

	int top_border, side_borders;

	//TODO: Speed and score Controls.

	//int[] Top_scores;

	Sprite gamewon, gameover;

	Typeface specialFont;

	//managers
	LevelsManager levelsManager;
	com.finalproject.brickbreaker.managers.AudioManager audioManager;
	GameplayManager gameplayManager;
	GameStateManager gameStateManager;



	public void OnLevelAdded(){
		levelsManager.LoadPatterns();
		levelsManager.populateLevelButtons(specialFont);

		if (gameStateManager.state == GameStateManager.GameState.levelMenu) {
			Levelmenu();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		gameStateManager = new GameStateManager();
		audioManager = new com.finalproject.brickbreaker.managers.AudioManager(this,gameStateManager);
		levelsManager = new LevelsManager(this, audioManager, new ICurrentLevelChangedListener() {
			@Override
			public void OnCurrentLevelChanged() {
				StartGame();
			}
		});
		levelsManager.LoadPatterns();
		gameplayManager = new GameplayManager(this,levelsManager,audioManager,gameStateManager);


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
		side_borders = Settings.getSideBorders(this);

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

		gameplayManager.initialize(wall_sprite);

		//set world origin
		setOrigin(BOTTOM_LEFT);

		//initialise score image
		gamewon = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.score), ScreenWidth() * 0.3f);
		gameover = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.gameover), ScreenWidth() * 0.3f);

		//state = MENU;

		//just in case of ad refresh
		if (gameStateManager.state == GameStateManager.GameState.levelMenu) {
			Levelmenu();
		}

	}



	@Override
	synchronized public void Step() {
		super.Step();
		if (gameStateManager.state == GameStateManager.GameState.menu) {

		} else if (gameStateManager.state == GameStateManager.GameState.gameplay) {
			gameplayManager.step(wall_sprite);
		}

	}

	@Override
	public synchronized void onAccelerometer(PointF point) {

	}

	@Override
	public synchronized void BackPressed() {
		if (gameStateManager.state == GameStateManager.GameState.gameplay) {
			audioManager.stopMusic();
			Levelmenu();
		} else if (gameStateManager.state == GameStateManager.GameState.levelMenu) {
			gameStateManager.state = GameStateManager.GameState.menu;
		} else if (gameStateManager.state == GameStateManager.GameState.menu) {
			audioManager.stopMusic();
			Exit();

		} else if (gameStateManager.state == GameStateManager.GameState.gameOver) {
			gameStateManager.state = GameStateManager.GameState.menu;
		}
	}

	@Override
	public synchronized void onTouch(float TouchX, float TouchY, MotionEvent event) {
		audioManager.onTouch(event, gameplayManager.isGamePaused());

		if (gameStateManager.state == GameStateManager.GameState.menu) {
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
		} else if (gameStateManager.state == GameStateManager.GameState.levelMenu) {
			levelsManager.onTouch(event);
		} else if (gameStateManager.state == GameStateManager.GameState.gameOver) {
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
		} else if (gameStateManager.state == GameStateManager.GameState.gameplay) {
			gameplayManager.onTouch(event);
		}
	}

	//..................................................Game Functions..................................................................................................................................

	private void StartGame(){
		gameplayManager.StartGame(wall_sprite);
	}

	public void Levelmenu() {
		gameStateManager.state = GameStateManager.GameState.levelMenu;
		levelsManager.updateLevels();
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

		if (gameStateManager.state == GameStateManager.GameState.menu) {
			//draw title
			Rect Title_Paint_bounds = new Rect();
			Title_Paint.getTextBounds(getResources().getString(R.string.app_name), 0, getResources().getString(R.string.app_name).length(), Title_Paint_bounds);
			canvas.drawText(getResources().getString(R.string.app_name), (ScreenWidth() / 2) - (Title_Paint_bounds.width() / 2), (top_border * 0.75f) + (Title_Paint_bounds.height() / 2), Title_Paint);
			//draw buttons
			btn_Play.draw(canvas);
			//btn_Highscores.draw(canvas);

		} else if (gameStateManager.state == GameStateManager.GameState.levelMenu) {
			levelsManager.draw(canvas, Title_Paint);
		} else if (gameStateManager.state == GameStateManager.GameState.gameplay) {
			gameplayManager.draw(canvas,Title_Paint);
		} else if (gameStateManager.state == GameStateManager.GameState.gameOver) {
			if (gameplayManager.getLivesLeft() > 0) {
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
		gameplayManager.pause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
