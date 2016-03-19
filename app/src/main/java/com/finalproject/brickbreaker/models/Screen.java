package com.finalproject.brickbreaker.models;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class Screen extends Activity implements Runnable, OnTouchListener {
	private SurfaceHolder holder;
	private boolean locker = true, initialised = false;
	private Thread thread;
	private int width = 0, height = 0;

	public Activity activity = this;
	private long now = SystemClock.elapsedRealtime(), lastRefresh, lastfps;
	public SurfaceView surface;

	//layout
	public RelativeLayout layout;
	public LinearLayout linear_layout;

	//canvas
	Canvas canvas;

	//recalculate screen dimensions
	int recalculateScreenCounter = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = this;

		//full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//create surface
		layout = new RelativeLayout(this);
		surface = new SurfaceView(this);
		layout.addView(surface);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		layout.setLayoutParams(params);

		linear_layout = new LinearLayout(this);
		linear_layout.setOrientation(LinearLayout.VERTICAL);
		linear_layout.addView(layout);

		setContentView(linear_layout);
		holder = surface.getHolder();

		//listeners
		surface.setOnTouchListener(this);

		// start game loop
		thread = new Thread(this);
		thread.start();
	}

	/* Main game loop.......................................................... */
	@Override
	public void run() {
		synchronized (ACCESSIBILITY_SERVICE) {
			while (locker) {
				now = SystemClock.elapsedRealtime();
				if (now - lastRefresh > 37) {
					lastRefresh = SystemClock.elapsedRealtime();
					if (!holder.getSurface().isValid()) {
						continue;
					}

					//fps
					if (now - lastfps > 1000) {
						lastfps = SystemClock.elapsedRealtime();
					}

					//step
					if (initialised)
						Step();

					//draw screen
					canvas = holder.lockCanvas();
					if (initialised)
						Draw(canvas);
					else {
						//initialise game
						width = canvas.getWidth();
						height = canvas.getHeight();
						Start();
						initialised = true;
					}
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	/* Detect and override back press */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			BackPressed();
			return false;
		}
		return false;
	}

	/* Events.................................................................. */
	public void Start() {

	}

	synchronized public void Step() {
		//used to refresh screen after a delay to allow for changes to occur before grabbing the new height/width
		if (recalculateScreenCounter != -1) {
			if (recalculateScreenCounter > 0)
				recalculateScreenCounter--;
			else {
				initialised = false;
				recalculateScreenCounter = -1;
			}
		}
	}

	public int dpToPx(int dp) {
		float density = getApplicationContext().getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}

	public void Draw(Canvas canvas) {

	}

	public void Pause() {
		locker = false;

		while (true) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		thread = null;
	}

	public void Resume() {
		locker = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void BackPressed() {

	}

	public synchronized void onTouch(float TouchX, float TouchY, MotionEvent event) {
	}

	/* Functions............................................................... */
	public void Exit() {
		locker = false;

		while (true) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		thread = null;

		System.exit(0);
		activity.finish();
	}


	//screen related
	public int ScreenWidth() {
		return width;
	}

	public int ScreenHeight() {
		return height;
	}


	/* Touch events.......................................................... */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (initialised) {
			onTouch(event.getX(), event.getY(), event);
		}
		return true;
	}

	/* pause, resume................................................ */
	@Override
	protected void onResume() {
		super.onResume();
		Resume();
	}

	@Override
	protected void onPause() {
		Pause();
		super.onPause();
	}
}
