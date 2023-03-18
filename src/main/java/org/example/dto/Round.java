package org.example.dto;

import java.util.LinkedList;

public class Round {
    private int id;
    private int inProgress;

    public LinkedList<Game> gameList=new LinkedList<>();

    public Round(int id, int inProgress) {
        this.id = id;
        this.inProgress = inProgress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInProgress() {
        return inProgress;
    }

    public void setInProgress(int inProgress) {
        this.inProgress = inProgress;
    }

    public void setGameList(LinkedList<Game> gameList) {
        this.gameList = gameList;
    }

    public LinkedList<Game> getGameList() {
        return gameList;
    }
}