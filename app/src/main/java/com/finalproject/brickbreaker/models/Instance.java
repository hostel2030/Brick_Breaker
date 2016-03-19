package com.finalproject.brickbreaker.models;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.services.BrickTypes;

public class Instance {
	public float x, y, speedx = 0, speedy = 0, accelerationx = 0, accelerationy = 0;
	public ScaledImage sprite;
	Screen screen;
	Physics physics = new Physics();
	public BrickTypes type = BrickTypes.Empty;

	public Instance(ScaledImage sprite, float x, float y, Screen screen, BrickTypes type) {
		this.sprite = sprite;
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.type = type;
	}

	//update the Object
	public void Update() {
		x += speedx;
		y += speedy;
		speedx += accelerationx;
		speedy += accelerationy;
	}

	public int getHeight() {
		return sprite.getHeight();
	}

	public int getWidth() {
		return sprite.getWidth();
	}

	//draw the sprite to screen
	public void draw(Canvas canvas) {
		//draw image
		sprite.draw(canvas, x, y);

	}

	public boolean isTouched(MotionEvent event) {
		return physics.intersect((int) x, (int) y, sprite.getWidth(), (int) sprite.getHeight(), (int) event.getX(), (int) event.getY());
	}


	public boolean CollidedWith(Instance b) {
		return physics.intersect((int) x, (int) y, sprite.getWidth(), (int) sprite.getHeight(), (int) b.x, (int) b.y, b.sprite.getWidth(), (int) b.sprite.getHeight());
	}
}
