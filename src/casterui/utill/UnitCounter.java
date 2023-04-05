package casterui.utill;

import mindustry.game.Team;
import mindustry.type.UnitType;

public class UnitCounter {
    public int amount = 0;
    public UnitType type;
    public Team team;

    public void setAmount(Integer amount){
        this.amount = amount;
    }

    UnitCounter(Team team, UnitType type, Integer amount){
        this.amount = amount;
        this.type = type;
        this.team = team;
    }

}
