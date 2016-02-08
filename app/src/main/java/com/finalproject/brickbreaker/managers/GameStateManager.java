package com.finalproject.brickbreaker.managers;



public class GameStateManager {

    public enum GameState{
        menu,gameplay,levelMenu,gameOver
    }

    //states
    public GameState state = GameState.menu;



}
