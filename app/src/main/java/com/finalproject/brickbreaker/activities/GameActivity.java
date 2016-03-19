package com.finalproject.brickbreaker.activities;

import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.interfaces.IInitiateGameListener;
import com.finalproject.brickbreaker.interfaces.IOnLevelAddedListener;
import com.finalproject.brickbreaker.managers.GameOverManager;
import com.finalproject.brickbreaker.managers.GameStateManager;
import com.finalproject.brickbreaker.managers.GameplayManager;
import com.finalproject.brickbreaker.managers.LevelsManager;
import com.finalproject.brickbreaker.managers.LevelsPatternsManager;
import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.services.Settings;
import com.finalproject.brickbreaker.models.Button;
import com.finalproject.brickbreaker.models.Screen;
import com.finalproject.brickbreaker.models.ScaledImage;

public class GameActivity extends Screen  implements IOnLevelAddedListener, IInitiateGameListener {

	//paints
	Paint Title_Paint = new Paint();
	Paint Instruction_Paint = new Paint();
	Paint Black_shader = new Paint();
	Paint Black_dark_shader = new Paint();

	ScaledImage wall_sprite;

	Button btn_Play;

	int top_border, side_borders;

	Typeface specialFont;

	//managers
	LevelsManager levelsManager;
	com.finalproject.brickbreaker.managers.AudioManager audioManager;
	GameplayManager gameplayManager;
	GameStateManager gameStateManager;
	GameOverManager gameOverManager;

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
		levelsManager = new LevelsManager(this, audioManager, this);
		levelsManager.LoadPatterns();
		gameplayManager = new GameplayManager(this,levelsManager,audioManager,gameStateManager);
		gameOverManager = new GameOverManager(this,audioManager,levelsManager,gameplayManager,this);

		LevelsPatternsManager.GetInstance(getBaseContext()).RegisterLevelAdded(this);
	}

	@Override
	public void Start() {
		super.Start();

		specialFont = Typeface.createFromAsset(getAssets(), "forte.ttf");

		Title_Paint.setTextSize(dpToPx(38));
		Title_Paint.setAntiAlias(true);
		Title_Paint.setColor(getResources().getColor(R.color.blue));
		Title_Paint.setTypeface(specialFont);

		//Wall Instruction Paint
		Instruction_Paint.setTextSize(dpToPx(38));
		Instruction_Paint.setAntiAlias(true);
		Instruction_Paint.setColor(getResources().getColor(R.color.trans_black));
		Instruction_Paint.setTypeface(specialFont);

		//shaders
		Black_shader.setColor(getResources().getColor(R.color.black));
		Black_dark_shader.setColor(getResources().getColor(R.color.black_dark));

		audioManager.initialize();
		levelsManager.initialize();
		top_border = (int) Settings.getTopBorder(ScreenHeight());

		//initialise borders
		side_borders = Settings.getSideBorders(this);

		//initialise wall sprite
		wall_sprite = new ScaledImage(BitmapFactory.decodeResource(getResources(), R.drawable.bluewall), ScreenHeight() * 0.18f, true);

		//play button
		btn_Play = new Button(getResources().getString(R.string.Play), ScreenWidth() / 10, specialFont, getResources().getColor(R.color.black), ScreenWidth() / 2, ScreenHeight() * 0.45f, this);
		btn_Play.x = ScreenWidth() / 2 - btn_Play.getWidth() / 2;


		levelsManager.populateLevelButtons(specialFont);
		gameOverManager.initialize(wall_sprite,specialFont);
		gameplayManager.initialize(wall_sprite);
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
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				btn_Play.LowLight();

				if (btn_Play.isTouched(event)) {
					audioManager.playBounce();
					Levelmenu();
				}
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {

			}
		} else if (gameStateManager.state == GameStateManager.GameState.levelMenu) {
			levelsManager.onTouch(event);
		} else if (gameStateManager.state == GameStateManager.GameState.gameOver) {
			gameOverManager.onTouch(event);
		} else if (gameStateManager.state == GameStateManager.GameState.gameplay) {
			gameplayManager.onTouch(event);
		}
	}

	//..................................................Game Functions..................................................................................................................................

	public void startGame(){
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

		} else if (gameStateManager.state == GameStateManager.GameState.levelMenu) {
			levelsManager.draw(canvas, Title_Paint);
		} else if (gameStateManager.state == GameStateManager.GameState.gameplay) {
			gameplayManager.draw(canvas,Title_Paint);
		} else if (gameStateManager.state == GameStateManager.GameState.gameOver) {
			gameOverManager.draw(canvas, Title_Paint);
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
	public void onPause() {
		super.onPause();
		gameplayManager.pause();
	}
}
