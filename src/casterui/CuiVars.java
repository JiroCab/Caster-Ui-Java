package casterui;

import arc.Core;
import arc.util.Log;
import arc.util.Time;
import casterui.io.CuiBinding;
import casterui.io.CuiInputs;
import casterui.io.ui.*;
import casterui.io.ui.dialog.CuiSettingsDialog;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.input.Binding;
import mindustry.world.Tile;

public class CuiVars {
    public static CuiWorldRenderer renderer = new CuiWorldRenderer();
    public static CuiFragment fragment = new CuiFragment();
    public static CuiInputs inputs = new CuiInputs();

    public static boolean showCoreUnits = true, initialized = false, unitTableCollapse = true;
    public static Player hoveredPlayer, clickedPlayer;
    public static Unit heldUnit, hoveredEntity, clickedEntity;
    public static float  timer = 0, nextUpdate = 100;
    public static Tile lastCoreDestroyEvent;
    public static int updateDelay = Core.settings.getInt("cui-unitsPlayerTableUpdateRate");


    public static void init(){
        CuiBinding.load();
        CuiSettingsDialog.buildCategory();
        renderer.worldRenderer();
        Log.info("Caster user interface loaded! happy casting!! owo");
    }

    public static void postInt(){
        fragment.clearTables();
        renderer.circleQueue.clear();

        CuiVars.heldUnit = null;
        initialized = false;
        lastCoreDestroyEvent = null;
        nextUpdate = timer + (updateDelay * 5);
        updateDelay = Core.settings.getInt("cui-unitsPlayerTableUpdateRate");
        fragment.BuildTables(Vars.ui.hudGroup);
    }

    public static void update(){
        inputs.update();

        timer = Time.globalTime;
        if(Vars.state.isPlaying() && timer >= nextUpdate && !initialized){
            nextUpdate = timer + (updateDelay * 5);

            fragment.UpdateTables();
        };
    }

}
