package casterui;

import arc.Core;
import arc.util.Log;
import arc.util.Time;
import casterui.io.CuiInputs;
import casterui.io.ui.CuiFragment;
import casterui.io.ui.CuiWorldRenderer;
import casterui.io.ui.dialog.*;
import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;

import java.util.HashMap;
import java.util.Map;

public class CuiVars {
    public static CuiWorldRenderer renderer = new CuiWorldRenderer();
    public static CuiFragment fragment = new CuiFragment();
    public static CuiInputs inputs = new CuiInputs();
    public static CuiTeamMangerDialog teamManger = new CuiTeamMangerDialog();
    public static CuiRebindDialog rebindDialog = new CuiRebindDialog();

    public static boolean initialized = false, unitTableCollapse = true, fastUpdate = false, drawRally = false;
    public static Player hoveredPlayer, clickedPlayer;
    public static Unit heldUnit, hoveredEntity, clickedEntity;
    public static float  timer = 0, nextUpdate = 100, nextUpdateFast = 50;
    public static Tile lastCoreDestroyEvent;
    public static Map<Integer, Player> mappedPlayers = new HashMap<>();


    public static void init(){
        CuiSettingsDialog.buildCategory();
        renderer.worldRenderer();
        rebindDialog.load();
        Log.info("Caster user interface loaded! happy casting!! owo");
    }

    public static void postInt(){
        fragment.clearTables();
        renderer.circleQueue.clear();

        CuiVars.heldUnit = null;
        initialized = false;
        lastCoreDestroyEvent = null;
        nextUpdate = timer + (Core.settings.getInt("cui-unitsPlayerTableUpdateRate") * 5);
        fragment.BuildTables(Vars.ui.hudGroup);
    }

    public static void update(){
        inputs.update();
        fragment.UpdateTables();

        timer = Time.globalTime;
        if(Vars.state.isPlaying() && !initialized) {
            if (timer >= nextUpdate ) {
                nextUpdate = timer + (Core.settings.getInt("cui-unitsPlayerTableUpdateRate") * 5);

                if(!fastUpdate)fragment.StutteredUpdateTables();
            } else if (timer >= nextUpdateFast ){
                nextUpdateFast = timer + (Core.settings.getInt("cui-unitsPlayerTableUpdateRate") * 2);
                fragment.slowUpdateTables();
                if(fastUpdate)fragment.StutteredUpdateTables();
            }
        }
    }

}
