package com.finalproject.brickbreaker.managers;


import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;

import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.services.Settings;
import com.finalproject.brickbreaker.models.Button;
import com.finalproject.brickbreaker.models.Screen;
import com.finalproject.brickbreaker.models.ScaledImage;

public class AudioManager {

    boolean sound_muted = false, music_muted = false;
    int sound_gameover, sound_success, sound_ballout, sound_bounce, sound_reject;
    SoundPool sp;
    MediaPlayer music;

    Button btn_sound_mute, btn_music_mute;

    //sound
    ScaledImage sound_on, sound_off, music_on, music_off;
    private Screen screen;
    private GameStateManager gameStateManager;

    public AudioManager(Screen screen, GameStateManager gameStateManager){
        this.screen = screen;
        this.gameStateManager = gameStateManager;

        //initialise sound
        screen.setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
        sp = new SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        sound_ballout = sp.load(screen, R.raw.ballout, 1);
        sound_bounce = sp.load(screen, R.raw.bounce, 1);
        sound_gameover = sp.load(screen, R.raw.gameover, 1);
        sound_success = sp.load(screen, R.raw.success, 1);
        sound_reject = sp.load(screen, R.raw.reject, 1);

        //initialise music
        music = MediaPlayer.create(screen, R.raw.music);


    }

    public void initialize(){
        //sound buttons
        music_on = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.music_on), screen.ScreenWidth() * 0.1f);
        music_off = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.music_off), screen.ScreenWidth() * 0.1f);
        sound_off = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.sound_off), screen.ScreenWidth() * 0.1f);
        sound_on = new ScaledImage(BitmapFactory.decodeResource(screen.getResources(), R.drawable.sound_on), screen.ScreenWidth() * 0.1f);

        //music mute
        btn_music_mute = new Button(music_on, 0, 0, screen);
        btn_music_mute.x = (screen.ScreenWidth() / 2) + btn_music_mute.getWidth() * 1.5f;
        btn_music_mute.y = (Settings.getTopBorder(screen.ScreenHeight()) / 4) - btn_music_mute.getHeight() * 0.5f;

        //sound mute
        btn_sound_mute = new Button(sound_on, 0, 0, screen);
        btn_sound_mute.x = (screen.ScreenWidth() / 2) - btn_sound_mute.getWidth() * 2.5f;
        btn_sound_mute.y = (Settings.getTopBorder(screen.ScreenHeight()) / 4) - btn_sound_mute.getHeight() * 0.5f;
    }


    public void playBounce(){
        if (sound_bounce != 0 && !sound_muted)
            sp.play(sound_bounce, 1, 1, 0, 0, 1);
    }

    public void playReject() {
        if (sound_reject != 0 && !sound_muted)
            sp.play(sound_reject, 1, 1, 0, 0, 1);
    }

    public void playBallOut() {
        if (sound_ballout != 0 && !sound_muted)
            sp.play(sound_ballout, 1, 1, 0, 0, 1);
    }

    public void playSuccess() {
        if (sound_success != 0 && !sound_muted)
            sp.play(sound_success, 1, 1, 0, 0, 1);
    }

    public void playGameOver() {
        if (sound_gameover != 0 && !sound_muted)
            sp.play(sound_gameover, 1, 1, 0, 0, 1);
    }

    public void playMusic() {
        if (!music_muted && gameStateManager.state == GameStateManager.GameState.gameplay) {
            music = MediaPlayer.create(screen, R.raw.music);
            music.start();
            music.setVolume(0.5f, 0.5f);
            music.setLooping(true);
        }
    }

    public void stopMusic() {
        music.stop();
    }

    public void toggleMusic(boolean pause) {
        if (music_muted) {
            music_muted = false;
            btn_music_mute.sprite = music_on;
            if (!pause) {
                playMusic();
            }
        } else {
            music_muted = true;
            btn_music_mute.sprite = music_off;
            stopMusic();
        }
    }

    public void toggleSoundFx() {
        if (sound_muted) {
            sound_muted = false;
            btn_sound_mute.sprite = sound_on;
        } else {
            sound_muted = true;
            btn_sound_mute.sprite = sound_off;
        }
    }

    public void onTouch(MotionEvent event,boolean pause){
        //handle constant events like sound buttons
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (btn_sound_mute.isTouched(event)) {
                btn_sound_mute.Highlight(screen.getResources().getColor(R.color.red));
            }
            if (btn_music_mute.isTouched(event)) {
                btn_music_mute.Highlight(screen.getResources().getColor(R.color.red));
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //refresh all
            btn_music_mute.LowLight();
            btn_sound_mute.LowLight();

            if (btn_sound_mute.isTouched(event)) {
                toggleSoundFx();
            }
            if (btn_music_mute.isTouched(event)) {
                toggleMusic(pause);
            }
        }
    }

    public boolean isMusicMuted(){
        return music_muted;
    }

    public boolean areButtonsTouched(MotionEvent event){
        return !btn_sound_mute.isTouched(event) && !btn_music_mute.isTouched(event);
    }


    public void draw(Canvas canvas) {
        btn_sound_mute.draw(canvas);
        btn_music_mute.draw(canvas);
    }
}
