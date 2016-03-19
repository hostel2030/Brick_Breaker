package com.finalproject.brickbreaker.models;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SingleScore {

	//highscore related
	Screen screen;

	public SingleScore(Screen screen) {
		this.screen = screen;
	}


	public int load_localscore_simple(String identifier) {
		// load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(screen.getApplicationContext());
		int score = hiscores.getInt("score" + identifier, 0);
		return score;
	}

	public void save_localscore_simple(int score, String identifier) {
		//load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(screen.getApplicationContext());
		SharedPreferences.Editor hiscores_editor = hiscores.edit();

		hiscores_editor.putInt("score" + identifier, score);

		hiscores_editor.commit();
	}

	public void save_localscore_smaller(int score, String identifier) {

		if (score < load_localscore_simple(identifier) || load_localscore_simple(identifier) == 0)
			save_localscore_simple(score, identifier);
	}

}
