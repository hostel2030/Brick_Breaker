package com.finalproject.brickbreaker.models;

import android.graphics.RectF;

public class Physics {
	RectF a = null, b = null;

	//collisions
	public boolean intersect(int x, int y, int width, int height, int x2, int y2, int width2, int height2) {
		a = new RectF(x, y, x + width, y + height);
		b = new RectF(x2, y2, x2 + width2, y2 + height2);
		return a.intersect(b);
	}

	//touching
	public boolean intersect(int x, int y, int width, int height, int pointx, int pointy) {
		a = new RectF(x, y, x + width, y + height);
		if ((pointx > x) && (pointy > y) && (pointx < (x + width)) && (pointy < (y + height))) {
			return (true);
		}
		return false;
	}

}
