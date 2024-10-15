package casterui.util;

import arc.graphics.Color;
import arc.math.geom.Position;
import mindustry.game.Team;
import mindustry.gen.Unit;

public class CuiPointerHelper {
    public Position pos;
    public Color color;
    public Position unit;

    public CuiPointerHelper(Position pos, Color colour, Unit unit){
        this.color = colour;
        this.pos = pos;
        this.unit = unit;
    }

    public Position pos(){return pos;}
    public Color color(){return color;}
    public Position unit(){return unit;}

    public float x(){return pos.getX();}
    public float y(){return pos.getY();}

    public float ux(){return unit.getX();}
    public float uy(){return unit.getY();}

}
