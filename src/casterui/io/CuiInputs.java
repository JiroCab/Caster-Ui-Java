package casterui.io;

import arc.Core;
import arc.KeyBinds;
import arc.KeyBinds.*;
import arc.func.*;
import arc.input.KeyCode;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.struct.Seq;
import arc.util.*;
import casterui.CuiVars;
import mindustry.Vars;
import mindustry.game.Teams;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;
import mindustry.world.Build;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.CoreBlock.*;

import static arc.Core.*;
import static casterui.io.CuiBinding.*;
import static mindustry.Vars.*;

public class CuiInputs {
    public boolean tracking = false, keepMouseTracking = false;
    public int trackingType = 4, slot = 0;
    Seq<Player> ply = new Seq<>();
    Seq<CoreBuild> cor = new Seq<>();
    public int playerNumber = 0, coreNumber = 0;
    public Vec2 out = new Vec2() ;

    public void update(){
        if (settings.getBool("cui-respectCommandMode") && control.input.commandMode) return;
        if (settings.getBool("cui-respectTyping") && (ui.chatfrag.shown() || scene.getKeyboardFocus() != null) || ui.consolefrag.shown())return;
        if (settings.getBool("cui-respectLockInputs") && control.input.locked())return;
        if(settings.getBool("cui-respectDialog") && scene.hasDialog()) return;
        if (state.isMenu()) return;

        if(cuiKeyTap(toggle_cui_menu) && !settings.getBool("cui-hideWithMenus")) CuiVars.globalHidden = !CuiVars.globalHidden;
        else if(settings.getBool("cui-hideWithMenus")) CuiVars.globalHidden = ui.hudfrag.shown;

        if(cuiKeyTap(change_teams)) CuiVars.teamManger.show();
        if(cuiKeyTap(toggle_unit_hp_bars)) settings.put("cui-showUnitBar", !settings.getBool("cui-showUnitBar"));
        if(cuiKeyTap(toggle_player_cursor)) settings.put("cui-TrackPlayerCursor", !settings.getBool("cui-TrackPlayerCursor"));
        if(cuiKeyTap(toggle_track_logic)){
            if(settings.getInt("cui-logicLineAlpha") == 0) settings.put("cui-logicLineAlpha", settings.getInt("cui-logicLineAlphaPreferred", 100));
            else{
                settings.put("cui-logicLineAlphaPreferred", settings.getInt("cui-logicLineAlpha"));
                settings.put("cui-logicLineAlpha", 0);
        }}
        if(cuiKeyTap(toggle_unit_cmd)){
            if(settings.getInt("cui-unitscommands") == 0) settings.put("cui-unitscommands", settings.getInt("cui-unitscommandsPreferred", 1));
            else{
                settings.put("cui-unitscommandsPreferred", settings.getInt("cui-unitscommands"));
                settings.put("cui-unitscommands", 0);
        }}
        if(cuiKeyTap(toggle_shorten_items_info)) settings.put("cui-BlockInfoShortenItems", !settings.getBool("cui-BlockInfoShortenItems"));
        if(cuiKeyTap(toggle_block_hp)) settings.put("cui-ShowBlockHealth", !settings.getBool("cui-ShowBlockHealth"));
        if(cuiKeyTap(toggle_units_player_table_controls)) settings.put("cui-playerunitstablecontols", !settings.getBool("cui-playerunitstablecontols"));
        if(cuiKeyTap(toggle_table_core_units)) settings.put("cui-unitsTableCoreUnits", !settings.getBool("cui-unitsTableCoreUnits"));
        if(cuiKeyTap(toggle_table_summarize_players)) settings.put("cui-playerTableSummarizePlayers", !settings.getBool("cui-playerTableSummarizePlayers"));
        if(cuiKeyTap(toggle_team_items)) settings.put("cui-ShowTeamItems", !settings.getBool("cui-ShowTeamItems"));
        if(cuiKeyTap(toggle_shorten_team_items)) settings.put("cui-TeamItemsShortenItems", !settings.getBool("cui-TeamItemsShortenItems"));
        if(cuiKeyTap(toggle_factory_style)){
            if(settings.getInt("cui-showFactoryProgressStyle") == 1) settings.put("cui-showFactoryProgressStyle", settings.getInt("cui-showFactoryProgressStylePreferred", 2));
            else{
                settings.put("cui-showFactoryProgressStylePreferred", settings.getInt("cui-showFactoryProgressStyle"));
                settings.put("cui-showFactoryProgressStyle", 1);
        }}
        if(cuiKeyTap(toggle_alerts_circle)) settings.put("cui-ShowAlertsCircles", !settings.getBool("cui-ShowAlertsCircles"));
        if(cuiKeyTap(toggle_alerts_circle_reverse_growth)) settings.put("cui-alertReverseGrow", !settings.getBool("cui-alertReverseGrow"));
        if(cuiKeyTap(toggle_alerts_toast)) settings.put("cui-ShowAlerts", !settings.getBool("cui-ShowAlerts"));
        if(cuiKeyTap(toggle_alerts_toast_bottom)) settings.put("cui-AlertsUseBottom", !settings.getBool("cui-AlertsUseBottom"));
        if(cuiKeyTap(toggle_cui_kill_switch)) settings.put("cui-killswitch", !settings.getBool("cui-killswitch")); //haha this will be one way but lulz
        if(cuiKeyTap(toggle_unit_Cmd_type)) settings.put("cui-unitCmdNonMv", !settings.getBool("cui-unitCmdNonMv"));




        /*TODO: something more elegant? */
        if(cuiKeyTap(map_player_1) && CuiVars.mappedPlayers.get(1) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(1);
        if(cuiKeyTap(map_player_2) && CuiVars.mappedPlayers.get(2) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(2);
        if(cuiKeyTap(map_player_3) && CuiVars.mappedPlayers.get(3) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(3);
        if(cuiKeyTap(map_player_4) && CuiVars.mappedPlayers.get(4) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(4);
        if(cuiKeyTap(map_player_5) && CuiVars.mappedPlayers.get(5) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(5);
        if(cuiKeyTap(map_player_6) && CuiVars.mappedPlayers.get(6) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(6);
        if(cuiKeyTap(map_player_7) && CuiVars.mappedPlayers.get(7) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(7);
        if(cuiKeyTap(map_player_8) && CuiVars.mappedPlayers.get(8) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(8);
        if(cuiKeyTap(map_player_9) && CuiVars.mappedPlayers.get(9) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(9);

        if (scene.hasField()) return;
        tracking = false;

        float cameraFloat = 0.085F; //TODO:ALLOW THIS TO BE CHANGED
        if (!settings.getBool("smoothcamera")){ cameraFloat = 1;}

        if(cuiKeyTap(spectate_next_player)) cyclePlayers(true);
        if(cuiKeyTap(spectate_previous_player)) cyclePlayers(false);
        if(cuiKeyTap(spectate_next_core)) cycleCore(true);
        if(cuiKeyTap(spectate_previous_core)) cycleCore(false);

        if (CuiVars.lastCoreDestroyEvent != null && cuiKeyDown(last_destroyed_core) && !tracking){
            if(control.input instanceof DesktopInput input) input.panning = true;
            stopTracking();

            camera.position.set(CuiVars.lastCoreDestroyEvent);
            tracking = true;
        }

        if(CuiVars.clickedCore != null && !CuiVars.clickedCore.dead() && state.isPlaying() && !tracking){
            startTracking();

            tracking = true;
            if(CuiVars.clickedCore != null) camera.position.lerpDelta(CuiVars.clickedCore, cameraFloat);
        }

        if(state.isPlaying() && !tracking){
            boolean move = CuiVars.rebindDialog.cuiKeyBinds.get(move_camera).key == KeyCode.unknown || cuiKeyDown(move_camera);
            boolean save = CuiVars.rebindDialog.cuiKeyBinds.get(save_camera).key != null && cuiKeyDown(save_camera);
            if(move){
                if(cuiKeyTap(map_camera_1)) handSavedCams(1, save);
                else if(cuiKeyTap(map_camera_2)) handSavedCams(2, save);
                else if(cuiKeyTap(map_camera_3)) handSavedCams(3, save);
                else if(cuiKeyTap(map_camera_4)) handSavedCams(4, save);
                else if(cuiKeyTap(map_camera_5)) handSavedCams(5, save);
                else if(cuiKeyTap(map_camera_6)) handSavedCams(6, save);
                else if(cuiKeyTap(map_camera_7)) handSavedCams(7, save);
                else if(cuiKeyTap(map_camera_8)) handSavedCams(8, save);
                else if(cuiKeyTap(map_camera_9)) handSavedCams(9, save);
                else if(cuiKeyTap(map_camera_10)) handSavedCams(10, save);
            }


            if(out != null && !out.isZero()){
                startTracking();
                tracking = true;

                if(!out.isZero())camera.position.lerpDelta(out, cameraFloat);
            }
        }

        if (CuiVars.clickedPlayer != null && CuiVars.clickedPlayer.unit() != null && state.isPlaying() && !tracking){
            startTracking();
            trackingType = 4;

            //workaround for when in multiplayer, sometimes respawning puts you in 0,0 during the animation before moving your unit
            if (CuiVars.clickedPlayer != null && (CuiVars.clickedPlayer.unit() == null || CuiVars.clickedPlayer.unit().x == 0 && CuiVars.clickedPlayer.unit().y == 0) && CuiVars.clickedPlayer.team().data().hasCore()) trackingType = 3;
            if (CuiVars.clickedPlayer != null && CuiVars.clickedPlayer.unit() != null && (CuiVars.clickedPlayer.unit().x != 0 && CuiVars.clickedPlayer.unit().y != 0)) trackingType = 1;
            if (cuiKeyDown(track_cursor) && settings.getBool("cui-playerHoldTrackMouse")) trackingType = 2;
            if (cuiKeyTap(track_cursor) && !settings.getBool("cui-playerHoldTrackMouse")) keepMouseTracking = !keepMouseTracking;
            if (keepMouseTracking && !settings.getBool("cui-playerHoldTrackMouse")) trackingType = 2;
            if (!keepMouseTracking && !settings.getBool("cui-playerHoldTrackMouse") && CuiVars.clickedPlayer.unit() != null) trackingType = 1;
            if (!keepMouseTracking && !settings.getBool("cui-playerHoldTrackMouse") && CuiVars.clickedPlayer.unit() == null) trackingType = 3;

            /* so many if statements, enjoy >;3c */
            switch (trackingType) {
                case 1 -> {
                    tracking = true;
                    camera.position.lerpDelta(CuiVars.clickedPlayer.unit(), cameraFloat);
                }
                case 2 -> {
                    tracking = true;
                    camera.position.lerpDelta(CuiVars.clickedPlayer.mouseX, CuiVars.clickedPlayer.mouseY, cameraFloat);
                }
                case 3 -> {
                    tracking = true;
                    camera.position.lerpDelta(CuiVars.clickedPlayer.bestCore(), cameraFloat);
                }
                case 4 -> {
                    tracking = false;
                    CuiVars.clickedPlayer = null;
                }
            }

        }


    }
    void handSavedCams(int num, boolean save){
        if(save) CuiVars.savedCameras[num] = new Vec2(player.mouseX(), player.mouseY());
        else if(CuiVars.savedCameras[num] != null && !CuiVars.savedCameras[num].isZero()) out.set(CuiVars.savedCameras[num]);

    }

    void cyclePlayers(boolean increment){
        ply.clear();
        for (Player p : Groups.player) {
            if (settings.getBool("cui-hideNoUnitPlayers") && (p.unit() == null && !p.team().data().hasCore())) continue;
            if (p != player) ply.add(p);
        }
        ply.remove(player);

        if (ply.size < 1) return;
        int number = playerNumber;

        if(increment) number++;
        else number--;

        if(number >= ply.size ) number = 0;
        if(number <= -1) number = ply.size - 1;

        playerNumber = number;

        CuiVars.clickedPlayer = ply.get(playerNumber);
    }


    void cycleCore(boolean increment){
        cor.clear();
        for (TeamData t : state.teams.present) cor.addAll(t.cores);
        cor.sort(c-> c.team.id);

        if (cor.size < 1) return;
        int number = coreNumber;

        if(increment) number++;
        else number--;

        if(number >= cor.size ) number = 0;
        if(number <= -1) number = cor.size - 1;

        coreNumber = number;

        CuiVars.clickedCore = cor.get(coreNumber);
    }

    public void stopTracking(){
        if(CuiVars.clickedPlayer != null) CuiVars.clickedPlayer = null;
        if(CuiVars.clickedCore != null) CuiVars.clickedCore = null;
        slot = 0;
        out.setZero();
    }

    public void startTracking(){
        if((Math.abs(input.axis(Binding.move_x)) > 0 || Math.abs(input.axis(Binding.move_y)) > 0 || input.keyTap(Binding.mouse_move) || input.keyTap(Binding.pan)) && (!scene.hasField())){
            stopTracking();
            return;
        }

        if(control.input instanceof DesktopInput input) input.panning = true;
    }

    public boolean cuiKeyTap(KeyBind key){
        return CuiVars.rebindDialog.cuiKeyBinds.get(key).key != null && input.keyTap(CuiVars.rebindDialog.cuiKeyBinds.get(key).key);
    }

    public boolean cuiKeyDown(KeyBind key){
        return CuiVars.rebindDialog.cuiKeyBinds.get(key).key != null && input.keyDown(CuiVars.rebindDialog.cuiKeyBinds.get(key).key);
    }


}
