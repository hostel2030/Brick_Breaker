package com.finalproject.brickbreaker.managers;



public class GameStateManager {

    public enum GameState{
        menu,gameplay,levelMenu,gameOver
    }

    //states
    final int MENU = 0, GAMEPLAY = 1, LEVELMENU = 2, GAMEOVER = 3;
    public GameState state = GameState.menu;



}
