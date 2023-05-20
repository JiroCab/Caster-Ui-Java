package casterui.util;

import arc.math.geom.Position;
import mindustry.game.Team;

public class CuiCircleObjectHelper {
    public Position pos;
    public Team team;
    public float startTime;
    public int size;

    public CuiCircleObjectHelper(Position pos, Team team, Float startTime, Integer size){
        this.pos = pos;
        this.team = team;
        this.startTime = startTime;
        this.size = size;
    }
    public Position pos(){return pos;}
    public Team team(){return team;}
    public Float startTime(){return startTime;}
    public Integer size(){return size;}
}
