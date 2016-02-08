package com.finalproject.brickbreaker.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.widget.Spinner;

public class Sprite {
	Bitmap unrotated_image_sequence[], image_sequence[];
	int current_image = 0;
	long start_time = SystemClock.uptimeMillis();
	int animation_speed;
	public Paint imagePaint = new Paint();
	private int pause_image_number = -1;

	/**
	 * @param image
	 *            The bitmap image to attach to Sprite
	 * @param scale
	 *            scale factor of image. Eg: ScreenWidth * 0.15f. put a negative number to leave unscaled
	 */
	public Sprite(Bitmap image, float scale) {
		init(image,scale,false);
	}

	/**
	 * @param image
	 *            The bitmap image to attach to Sprite
	 * @param scale
	 *            scale factor of image. Eg: ScreenWidth * 0.15f. put a negative number to leave unscaled
	 * @param scale_height
	 *            if true the scale factor is calculated on the image height
	 */
	public Sprite(Bitmap image, float scale, boolean scale_height) {
		init(image,scale,scale_height);
	}

	private void init(Bitmap image, float scale, boolean scale_height){
		image_sequence = new Bitmap[1];

		unrotated_image_sequence = new Bitmap[1];
		if (scale >= 0) {
			if(scale_height)
				image_sequence[0] = unrotated_image_sequence[0] = Bitmap.createScaledBitmap(image, (int) (((scale) / image.getHeight()) * image.getWidth()), (int) (scale), true);
			else
				image_sequence[0] = unrotated_image_sequence[0] = Bitmap.createScaledBitmap(image, (int)(scale), (int) (( scale / image.getWidth()) * image.getHeight()), true);

		}
		else
			image_sequence[0] = unrotated_image_sequence[0] = image;
	}

	public int getWidth() {
		return image_sequence[0].getWidth();
	}

	public int getHeight() {
		return image_sequence[0].getHeight();
	}

	//draw the sprite to screen
	public void draw(Canvas canvas, float x, float y) {
		draw(canvas, x, y, imagePaint);
	}

	public void draw(Canvas canvas, float x, float y, Paint paint) {

		if (pause_image_number >= 0) {
			//pause
			canvas.drawBitmap(image_sequence[pause_image_number], x, y, paint);
		} else {
			//draw image
			canvas.drawBitmap(image_sequence[current_image], x, y, paint);
			//update to next image
			if (image_sequence.length > 1) {
				long now = SystemClock.uptimeMillis();
				if (now > start_time + (500 - animation_speed)) {
					start_time = SystemClock.uptimeMillis();
					current_image++;
					if (current_image + 1 > image_sequence.length)
						current_image = 0;
				}
			}
		}
	}
}
