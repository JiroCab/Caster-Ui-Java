package casterui;

import arc.Core;
import arc.Graphics;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.math.geom.*;
import arc.util.*;
import casterui.io.CuiInputs;
import casterui.io.ui.CuiFragment;
import casterui.io.ui.CuiWorldRenderer;
import casterui.io.ui.dialog.*;
import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.mod.Mods;
import mindustry.ui.Fonts;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static arc.Core.settings;

public class CuiVars {
    public static CuiWorldRenderer renderer = new CuiWorldRenderer();
    public static CuiFragment fragment = new CuiFragment();
    public static CuiInputs inputs = new CuiInputs();
    public static CuiTeamMangerDialog teamManger = new CuiTeamMangerDialog();
    public static CuiRebindDialog rebindDialog = new CuiRebindDialog();

    public static boolean initialized = false, globalHidden = true, fastUpdate = false, drawRally = false, globalShow = true, animateCats = Core.settings.getBool("cui-animateSettings"), killswitch = false;
    public static Player clickedPlayer;
    public static CoreBlock.CoreBuild clickedCore;
    public static Unit heldUnit, hoveredEntity;
    public static float  timer = 0, nextUpdate = 100, nextUpdateFast = 50;
    public static Tile lastCoreDestroyEvent;
    public static Map<Integer, Player> mappedPlayers = new HashMap<>();
    public static Vec2[] savedCameras = new Vec2[11];

    public static DecimalFormat decFor = new DecimalFormat("#.##"), decForMini = new DecimalFormat("#.#");
    public static boolean[] dominationSettings = new boolean[14];
    public static boolean
            showBlockInfo = false, showCountersUnits = false, showCountersPlayers = false, showCountersButton = false,
            countersSeparateTeams = false, countersCoreUnits = false, countersCoreFlagged = false, countersTotals = false,
            dominationVertical = false, dominationColoured = false,
            showTeamItems = false, showDomination = false;


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
        updateSettings(true);

        CuiVars.heldUnit = null;
        initialized = false;
        lastCoreDestroyEvent = null;
        savedCameras = new Vec2[11];
        if(Core.settings.getBool("cui-auto-toggle-menu"))Vars.ui.hudfrag.shown = false;
        animateCats = Core.settings.getBool("cui-animateSettings");
        nextUpdate = timer + (Core.settings.getInt("cui-unitsPlayerTableUpdateRate") * 5);
        fragment.BuildTables(Vars.ui.hudGroup);
    }

    public static void update(){
        globalShow = !Core.settings.getBool("cui-killswitch");
        if (!globalShow) return;
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


    public static void updateSettings(boolean full){
        dominationColoured = settings.getInt("cui-domination-trans") > 0;
        dominationVertical = settings.getBool("cui-domination-vertical");

        dominationSettings[0] = settings.getBool("cui-domination-totals");
        dominationSettings[1] = settings.getBool("cui-domination-turret");
        dominationSettings[2] = settings.getBool("cui-domination-production");
        dominationSettings[3] = settings.getBool("cui-domination-distribution");
        dominationSettings[4] = settings.getBool("cui-domination-liquid");
        dominationSettings[5] = settings.getBool("cui-domination-power");
        dominationSettings[6] = settings.getBool("cui-domination-defence");
        dominationSettings[7] = settings.getBool("cui-domination-crafting");
        dominationSettings[8] = settings.getBool("cui-domination-units");
        dominationSettings[9] = settings.getBool("cui-domination-effect");
        dominationSettings[10] = settings.getBool("cui-domination-logic");
        dominationSettings[11] = settings.getBool("cui-domination-core");
        dominationSettings[12] = settings.getBool("cui-domination-percent");
        dominationSettings[13] = settings.getBool("cui-domination-raw");

        if(!full) return;

        showTeamItems = settings.getBool("cui-ShowTeamItems");
        showBlockInfo = settings.getBool("cui-ShowBlockInfo");
        showCountersUnits = settings.getBool("cui-ShowUnitTable");
        showCountersPlayers = settings.getBool("cui-ShowPlayerList");
        showDomination = settings.getBool("cui-domination-toggle");
        showCountersButton = settings.getBool("cui-playerunitstablecontols");
        countersSeparateTeams = settings.getBool("cui-separateTeamsUnit");
        countersCoreUnits = settings.getBool("cui-unitsTableCoreUnits");
        countersTotals = settings.getBool("cui-teamtotalunitcount");
    }
}
