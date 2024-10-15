package casterui;

import arc.Core;
import arc.Graphics;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.util.*;
import casterui.io.CuiInputs;
import casterui.io.ui.CuiFragment;
import casterui.io.ui.CuiWorldRenderer;
import casterui.io.ui.dialog.*;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.mod.Mods;
import mindustry.ui.Fonts;
import mindustry.world.Tile;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class CuiVars {
    public static CuiWorldRenderer renderer = new CuiWorldRenderer();
    public static CuiFragment fragment = new CuiFragment();
    public static CuiInputs inputs = new CuiInputs();
    public static CuiTeamMangerDialog teamManger = new CuiTeamMangerDialog();
    public static CuiRebindDialog rebindDialog = new CuiRebindDialog();

    public static boolean initialized = false, unitTableCollapse = true, fastUpdate = false, drawRally = false, globalShow = true, animateCats = Core.settings.getBool("cui-animateSettings");
    public static Player hoveredPlayer, clickedPlayer;
    public static Unit heldUnit, hoveredEntity, clickedEntity;
    public static float  timer = 0, nextUpdate = 100, nextUpdateFast = 50;
    public static Tile lastCoreDestroyEvent;
    public static Map<Integer, Player> mappedPlayers = new HashMap<>();
    public static DecimalFormat decFor = new DecimalFormat("#.##");


    public static void init(){
        CuiSettingsDialog.buildCategory();
        renderer.worldRenderer();
        rebindDialog.load();
        if(Core.settings.getBool("cui-minimalCursor")) overrideCursors();
        animateCats = Core.settings.getBool("cui-animateSettings");
        Log.info("Caster user interface loaded! happy casting!! owo");
        //Vars.ui.join.connect("localhost", 6567);  stream lining testing
    }

    public static void postInt(){
        fragment.clearTables();
        renderer.circleQueue.clear();

        CuiVars.heldUnit = null;
        initialized = false;
        lastCoreDestroyEvent = null;
        animateCats = Core.settings.getBool("cui-animateSettings");
        nextUpdate = timer + (Core.settings.getInt("cui-unitsPlayerTableUpdateRate") * 5);
        fragment.BuildTables(Vars.ui.hudGroup);
    }

    public static void update(){
        if (Core.settings.getBool("cui-killswitch")){
            globalShow = false;
            return;
        }
        globalShow = true;
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

    /*stolen from https://github.com/guiYMOUR/mindustry-Extra-Utilities-mod/blob/main/src/ExtraUtilities*/
    public static void overrideCursors(){
        Graphics.Cursor.SystemCursor.arrow.set(newCursor("cursor.png", Fonts.cursorScale()));
        Graphics.Cursor.SystemCursor.hand.set(newCursor("hand.png", Fonts.cursorScale()));
        Graphics.Cursor.SystemCursor.ibeam.set(newCursor("ibeam.png", Fonts.cursorScale()));
        Vars.ui.drillCursor = newCursor("drill.png", Fonts.cursorScale());
        Vars.ui.unloadCursor = newCursor("unload.png", Fonts.cursorScale());
        Vars.ui.targetCursor = newCursor("target.png", Fonts.cursorScale());
    }

    public static Graphics.Cursor newCursor(String filename){
        Mods.LoadedMod mod = Vars.mods.getMod(CuiMain.class);
        Pixmap p = new Pixmap(mod.root.child("cursors").child(filename));
        return Core.graphics.newCursor(p, p.width /2, p.height /2);
    }

    public static Graphics.Cursor newCursor(String filename, int scale){
        Mods.LoadedMod mod = Vars.mods.getMod(CuiMain.class);
        if(scale == 1 || OS.isAndroid || OS.isIos) return newCursor(filename);
        Pixmap base = new Pixmap(mod.root.child("cursors").child(filename));
        Pixmap result = Pixmaps.scale(base, base.width * scale, base.height * scale);
        base.dispose();
        return Core.graphics.newCursor(result, result.width /2, result.height /2);
    }

}
