package casterui;

import arc.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.ui.layout.*;
import arc.util.*;

public abstract class CuiBaseModule {
    public int version = - 1;

    public String getName(){
        return CuiBaseModule.class.getSimpleName();
    }

    public abstract void update();

    public abstract void reset();

    public abstract void addFragment();

    public void handleFragment(Table parentCont){
        //marginHandler(parentCont, settings.getInt("cui-playerunitstables-x"), settings.getInt("cui-playerunitstables-y"), settings.getBool("cui-playerunitstables-x-abs"), settings.getBool("cui-playerunitstables-y-abs"));
        //parentCont.align(alignSides.get(settings.getInt("cui-PlayerUnitsTableSide")));
        parentCont.align(Align.center);
        //unitPlayerTable.background(tableStyles.get(settings.getInt("cui-playerunitstablestyle")));


        parentCont.clear();
    }

    public void marginHandler(Table tab, int x, int y, boolean xPer, boolean yPer){
        float fx = x, fy = y;
        int xp = Math.round(Mathf.clamp(fx, -1, 1)),
        yp = Math.round(Mathf.clamp(fy, -1, 1));


        //todo: make this work
        if(xPer) fx = Mathf.lerp( Core.graphics.getWidth(), 0,  Math.abs((x -100)/100)) * xp;
        if(yPer) fy = Mathf.lerp( Core.graphics.getHeight(), 0,  Math.abs((y -100)/100)) * yp;

        tab.moveBy(fx, fy);
    }
}