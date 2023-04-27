package casterui;

import arc.Core;
import arc.util.Log;
import arc.util.Time;
import casterui.io.CuiBinding;
import casterui.io.CuiInputs;
import casterui.io.ui.CuiFragment;
import casterui.io.ui.CuiWorldRenderer;
import casterui.io.ui.dialog.CuiSettingsDialog;
import casterui.io.ui.dialog.CuiTeamMangerDialog;
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

    public static boolean showCoreUnits = true, initialized = false, unitTableCollapse = true, fastUpdate = false;
    public static Player hoveredPlayer, clickedPlayer;
    public static Unit heldUnit, hoveredEntity, clickedEntity;
    public static float  timer = 0, nextUpdate = 100, nextUpdateFast = 50;
    public static Tile lastCoreDestroyEvent;
    public static int updateDelay = Core.settings.getInt("cui-unitsPlayerTableUpdateRate");
    public static Map<Integer, Player> mappedPlayers = new HashMap<>();


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
        fragment.UpdateTables();

        timer = Time.globalTime;
        if(Vars.state.isPlaying() && !initialized) {
            if (timer >= nextUpdate ) {
                nextUpdate = timer + (updateDelay * 5);

                if(!fastUpdate)fragment.StutteredUpdateTables();
            } else if (timer >= nextUpdateFast ){
                nextUpdateFast = timer + (updateDelay * 2);
                fragment.slowUpdateTables();
                if(fastUpdate)fragment.StutteredUpdateTables();
            }
        }
    }

}
