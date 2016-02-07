package com.finalproject.brickbreaker.models;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.models.Button;
import com.finalproject.brickbreaker.models.Screen;
import com.finalproject.brickbreaker.models.Sprite;

public class level_button extends Button {
	int TopScore = 0;
	boolean islocked = true;
	Paint level_paint;
	Screen screen;
	Sprite lock, unlock;

	public level_button(String text, int dpSize, Typeface font, int color, float x, float y, float radius, int BackColor, Screen screen, boolean world, Paint level_paint) {
		super(text, dpSize, font, color, x, y, radius, BackColor, screen, world);
		this.level_paint = level_paint;
		this.screen = screen;
		lock = new Sprite(BitmapFactory.decodeResource(screen.getResources(), R.drawable.lock), radius / 2);
		unlock = new Sprite(BitmapFactory.decodeResource(screen.getResources(), R.drawable.unlock), radius / 2);

	}

	public boolean isLocked(){
		return islocked;
	}

	public void setTopScore(int TopScore) {
		this.TopScore = TopScore;
		if (TopScore > 0) {
			setLock(false);
		}
	}

	public void setLock(boolean islocked) {
		this.islocked = islocked;
	}

	public String IntegerToScore(int score) {
		return ((double) score / 1000) + screen.getResources().getString(R.string.time_suffix);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (islocked)
			lock.draw(canvas, x + width - lock.getWidth(), y + height - lock.getHeight());
		else {
			if (TopScore == 0)
				unlock.draw(canvas, x + width - unlock.getWidth(), y + height - unlock.getHeight());
			else
				canvas.drawText(IntegerToScore(TopScore), x + (width / 2) - level_paint.measureText(IntegerToScore(TopScore)) * 0.5f, y + height + level_paint.getTextSize(), level_paint);
		}
	}
}
