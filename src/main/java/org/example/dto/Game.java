package org.example.dto;

import java.sql.Timestamp;

public class Game {
    private int id;
    private int roundId;
    private int gameId;
    private int answer;
    private Timestamp time;
    private int guess;

    public Game(int id, int roundId, int gameId, int answer, Timestamp time, int guess) {
        this.id = id;
        this.roundId = roundId;
        this.gameId = gameId;
        this.answer = answer;
        this.time = time;
        this.guess = guess;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoundId() {
        return roundId;
    }

    public void setRoundId(int roundId) {
        this.roundId = roundId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getGuess() {
        return guess;
    }

    public void setGuess(int guess) {
        this.guess = guess;
    }
}

