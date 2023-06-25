package com.example.scoreboard;

public class Score {
    public String teamName;
    public int score = 0;

    public Score(byte score) {
        this.teamName = "None";
        this.score = score;
    }

//    byte[] getBytes(){
//        byte[] ret = {0};
////        ret[0] = teamName;
//        ret[1] = score;
//        return ret;
//    }
}
