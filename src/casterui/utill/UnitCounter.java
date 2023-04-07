package casterui.utill;

import casterui.CuiVars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

import java.util.HashMap;

public class UnitCounter {
    public Iterable<Unit> unitsIterator = Groups.unit;
    public HashMap<Integer, UnitType> top = new HashMap<>();

    public  void count(Unit unit){
        if (unit.spawnedByCore && CuiVars.showCoreUnits) return;
    }

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
