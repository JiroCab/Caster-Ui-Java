package casterui.modules;

import arc.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import casterui.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;

public class CuiHoverBlockInfo extends CuiBaseModule{
    public int
        displayHealth, //off, %, % + raw, raw
        displayItems, //off, shorten, full

        alignSide,
        tableStyle
    ;

    public boolean
        displayLastInteractor
    ;

    public @Nullable Table table, cont;

    @Override
    public void update(){
        Vec2 mouse = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
        Tile mouseTile = Vars.world.tileWorld(mouse.x, mouse.y);

        if(mouseTile != null && mouseTile.build != null){
            Building build = mouseTile.build;

        } else if(table != null)table.clear();
    }

    @Override
    public void reset(){
        removeFragement();
        addFragment();
    }


    @Override
    public void addFragment(){
        Vars.ui.hudGroup.fill( parent -> {
            cont = parent;
            table = new Table();
            handleFragment(parent);
            cont.add(table);
        });
    }

    public void removeFragement(){
        if(cont != null)cont.remove();
    }
}
