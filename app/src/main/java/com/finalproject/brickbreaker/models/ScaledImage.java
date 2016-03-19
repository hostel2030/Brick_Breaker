package com.finalproject.brickbreaker.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class ScaledImage {
	Bitmap bitmapImage;
	public Paint imagePaint = new Paint();

	// create scaled image by width
	public ScaledImage(Bitmap image, float scale) {
		init(image,scale,false);
	}

	//create scaled image by width/height.
	public ScaledImage(Bitmap image, float scale, boolean scale_height) {
		init(image,scale,scale_height);
	}

	private void init(Bitmap image, float scale, boolean scale_height){
		if (scale >= 0) {
			if(scale_height)
				bitmapImage = Bitmap.createScaledBitmap(image, (int) (((scale) / image.getHeight()) * image.getWidth()), (int) (scale), true);
			else
				bitmapImage = Bitmap.createScaledBitmap(image, (int)(scale), (int) (( scale / image.getWidth()) * image.getHeight()), true);

		}
		else
			bitmapImage = image;
	}

	public int getWidth() {
		return bitmapImage.getWidth();
	}

	public int getHeight() {
		return bitmapImage.getHeight();
	}

	//draw the sprite to screen
	public void draw(Canvas canvas, float x, float y) {
		canvas.drawBitmap(bitmapImage, x, y, imagePaint);
	}

}
