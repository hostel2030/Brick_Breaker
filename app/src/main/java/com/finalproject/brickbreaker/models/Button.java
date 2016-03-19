package com.finalproject.brickbreaker.models;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.services.BrickTypes;

public class Button extends Instance {
	public final int TEXT_BTN = 0, SPRITE_BTN = 1, TEXT_BOX_BTN = 2, TEXT_CIRCLE_BTN = 3;
	public int type;
	public Paint textPaint = new Paint();
	public String text;
	public Paint BackPaint = new Paint();
	public float height, width;

	//create image button
	public Button(ScaledImage sprite, float x, float y, Screen screen) {
		super(sprite, x, y, screen, BrickTypes.Empty);
		type = SPRITE_BTN;
	}

	//create text button
	public Button(String text, int dpSize, Typeface font, int color, float x, float y, Screen screen) {
		super(null, x, y, screen, BrickTypes.Empty);
		type = TEXT_BTN;
		textPaint = new Paint();
		textPaint.setTextSize(screen.dpToPx(dpSize));
		textPaint.setAntiAlias(true);
		textPaint.setColor(color);
		textPaint.setTypeface(font);
		this.text = text;
	}

	//create round button
	public Button(String text, int dpSize, Typeface font, int color, float x, float y, float radius, int BackColor, Screen screen) {
		super(null, x, y, screen, BrickTypes.Empty);
		type = TEXT_CIRCLE_BTN;
		textPaint = new Paint();
		textPaint.setTextSize(screen.dpToPx(dpSize));
		textPaint.setAntiAlias(true);
		textPaint.setColor(color);
		textPaint.setTypeface(font);
		BackPaint.setColor(BackColor);
		BackPaint.setAntiAlias(true);
		this.text = text;
		this.height = radius * 2;
		this.width = radius * 2;
	}

	public void Highlight(int color) {
		ColorFilter filter = new LightingColorFilter(1, color);
		if (type == SPRITE_BTN)
			sprite.imagePaint.setColorFilter(filter);
		else
			textPaint.setColorFilter(filter);
	}

	//clear highlight
	public void LowLight() {
		ColorFilter filter = null;
		if (type == SPRITE_BTN)
			sprite.imagePaint.setColorFilter(filter);
		else
			textPaint.setColorFilter(filter);
	}

	@Override
	public int getWidth() {
		if (type == SPRITE_BTN)
			return super.getWidth();
		else if (height != 0 && width != 0) {
			return (int) width;
		} else {
			Rect bounds = new Rect();
			textPaint.getTextBounds(text, 0, text.length(), bounds);
			return bounds.width();
		}
	}

	@Override
	public int getHeight() {
		if (type == SPRITE_BTN)
			return super.getHeight();
		else if (height != 0 && width != 0) {
			return (int) height;
		} else {
			Rect bounds = new Rect();
			textPaint.getTextBounds(text, 0, text.length(), bounds);
			return bounds.height();
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (type == TEXT_BOX_BTN) {

			Rect bounds = new Rect();
			textPaint.getTextBounds(text, 0, text.length(), bounds);

			canvas.drawRect(x, y, x + width, y + height, BackPaint);
			canvas.drawText(text, x + (getWidth() / 2) - (bounds.width() / 2), y + (getHeight() / 2), textPaint);

		} else if (type == TEXT_CIRCLE_BTN) {

			Rect bounds = new Rect();
			textPaint.getTextBounds(text, 0, text.length(), bounds);

			final RectF rect = new RectF();
			rect.set(x, y, x + width, y + height);
			canvas.drawRoundRect(rect, 15, 15, BackPaint);

			canvas.drawText(text, x + (getWidth() / 2) - (bounds.width() / 2), y + (getHeight() / 2) + (bounds.height() / 2), textPaint);

		} else if (type == SPRITE_BTN) {
			super.draw(canvas);
		} else {
			canvas.drawText(text, x, y + getHeight(), textPaint);
		}


	}

	@Override
	public boolean isTouched(MotionEvent event) {
		return physics.intersect((int) x, (int) y, getWidth(), (int) getHeight(), (int) event.getX(), (int) event.getY());
	}

}
