package casterui.utill;

import mindustry.game.Team;
import mindustry.type.UnitType;

public class UnitCounter {
    public Integer amount = 0;
    public UnitType type = null;
    public Team team = Team.get(0);

    public UnitCounter(Integer amount, UnitType type, Team team){
        this.amount = amount;
        this.type = type;
        this.team = team;
    }
    public  UnitCounter(Integer amount, UnitType type, Integer team){
        new UnitCounter(amount, type, Team.get(team));
    }

}
