package casterui.io;

import arc.Core;
import arc.KeyBinds;
import arc.input.KeyCode;
import arc.struct.Seq;
import casterui.CuiVars;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;

import static arc.Core.*;
import static casterui.io.CuiBinding.*;
import static mindustry.Vars.*;

public class CuiInputs {
    public boolean tracking = false, keepMouseTracking = false;
    public int trackingType = 4;
    Seq<Player> ply = new Seq<>();
    public int playerNumber = 0;

    public void update(){
        if (settings.getBool("cui-respectCommandMode") && control.input.commandMode) return;
        if (settings.getBool("cui-respectTyping") && ui.chatfrag.shown())return;
        if (settings.getBool("cui-respectLockInputs") && control.input.locked())return;
        if(settings.getBool("cui-respectDialog") && scene.hasDialog()) return;
        if (state.isMenu()) return;

        if(cuiKeyTap(toggle_cui_menu) && !settings.getBool("cui-hideWithMenus")) CuiVars.unitTableCollapse = !CuiVars.unitTableCollapse;
        else if(settings.getBool("cui-hideWithMenus")) CuiVars.unitTableCollapse = ui.hudfrag.shown;

        if(cuiKeyTap(change_teams)) CuiVars.teamManger.show();
        if(cuiKeyTap(toggle_unit_hp_bars)) Core.settings.put("cui-showUnitBar", !Core.settings.getBool("cui-showUnitBar"));
        if(cuiKeyTap(toggle_player_cursor)) Core.settings.put("cui-TrackPlayerCursor", !Core.settings.getBool("cui-TrackPlayerCursor"));
        if(cuiKeyTap(toggle_track_logic)){
            if(Core.settings.getInt("cui-logicLineAlpha") == 0) Core.settings.put("cui-logicLineAlpha", Core.settings.getInt("cui-logicLineAlphaPreferred", 100));
            else{
                Core.settings.put("cui-logicLineAlphaPreferred", Core.settings.getInt("cui-logicLineAlpha"));
                Core.settings.put("cui-logicLineAlpha", 0);
        }}
        if(cuiKeyTap(toggle_unit_cmd)){
            if(Core.settings.getInt("cui-unitscommands") == 0) Core.settings.put("cui-unitscommands", Core.settings.getInt("cui-unitscommandsPreferred", 1));
            else{
                Core.settings.put("cui-unitscommandsPreferred", Core.settings.getInt("cui-unitscommands"));
                Core.settings.put("cui-unitscommands", 0);
        }}
        if(cuiKeyTap(toggle_shorten_items_info)) Core.settings.put("cui-BlockInfoShortenItems", !Core.settings.getBool("cui-BlockInfoShortenItems"));
        if(cuiKeyTap(toggle_block_hp)) Core.settings.put("cui-ShowBlockHealth", !Core.settings.getBool("cui-ShowBlockHealth"));
        if(cuiKeyTap(toggle_units_player_table_controls)) Core.settings.put("cui-playerunitstablecontols", !Core.settings.getBool("cui-playerunitstablecontols"));
        if(cuiKeyTap(toggle_table_core_units)) Core.settings.put("cui-unitsTableCoreUnits", !Core.settings.getBool("cui-unitsTableCoreUnits"));
        if(cuiKeyTap(toggle_table_summarize_players)) Core.settings.put("cui-playerTableSummarizePlayers", !Core.settings.getBool("cui-playerTableSummarizePlayers"));
        if(cuiKeyTap(toggle_team_items)) Core.settings.put("cui-ShowTeamItems", !Core.settings.getBool("cui-ShowTeamItems"));
        if(cuiKeyTap(toggle_shorten_team_items)) Core.settings.put("cui-TeamItemsShortenItems", !Core.settings.getBool("cui-TeamItemsShortenItems"));
        if(cuiKeyTap(toggle_factory_style)){
            if(Core.settings.getInt("cui-showFactoryProgressStyle") == 1) Core.settings.put("cui-showFactoryProgressStyle", Core.settings.getInt("cui-showFactoryProgressStylePreferred", 2));
            else{
                Core.settings.put("cui-showFactoryProgressStylePreferred", Core.settings.getInt("cui-showFactoryProgressStyle"));
                Core.settings.put("cui-showFactoryProgressStyle", 1);
        }}
        if(cuiKeyTap(toggle_alerts_circle)) Core.settings.put("cui-ShowAlertsCircles", !Core.settings.getBool("cui-ShowAlertsCircles"));
        if(cuiKeyTap(toggle_alerts_circle_reverse_growth)) Core.settings.put("cui-alertReverseGrow", !Core.settings.getBool("cui-alertReverseGrow"));
        if(cuiKeyTap(toggle_alerts_toast)) Core.settings.put("cui-ShowAlerts", !Core.settings.getBool("cui-ShowAlerts"));
        if(cuiKeyTap(toggle_alerts_toast_bottom)) Core.settings.put("cui-AlertsUseBottom", !Core.settings.getBool("cui-AlertsUseBottom"));
        if(cuiKeyTap(toggle_cui_kill_switch)) Core.settings.put("cui-killswitch", !Core.settings.getBool("cui-killswitch")); //haha this will be one way but lulz
        if(cuiKeyTap(toggle_unit_Cmd_type)) Core.settings.put("cui-unitCmdNonMv", !Core.settings.getBool("cui-unitCmdNonMv"));




        /*TODO: something more elegant? */
        if(input.keyTap(KeyCode.num1) && CuiVars.mappedPlayers.get(1) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(1);
        if(input.keyTap(KeyCode.num2) && CuiVars.mappedPlayers.get(2) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(2);
        if(input.keyTap(KeyCode.num3) && CuiVars.mappedPlayers.get(3) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(3);
        if(input.keyTap(KeyCode.num4) && CuiVars.mappedPlayers.get(4) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(4);
        if(input.keyTap(KeyCode.num5) && CuiVars.mappedPlayers.get(5) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(5);
        if(input.keyTap(KeyCode.num6) && CuiVars.mappedPlayers.get(6) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(6);
        if(input.keyTap(KeyCode.num7) && CuiVars.mappedPlayers.get(7) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(7);
        if(input.keyTap(KeyCode.num8) && CuiVars.mappedPlayers.get(8) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(8);
        if(input.keyTap(KeyCode.num9) && CuiVars.mappedPlayers.get(9) != null) CuiVars.clickedPlayer = CuiVars.mappedPlayers.get(9);

        if (scene.hasField()) return;
        tracking = false;

        float cameraFloat = 0.085F; //TODO:ALLOW THIS TO BE CHANGED
        if (!Core.settings.getBool("smoothcamera")){ cameraFloat = 1;}

        if(cuiKeyTap(spectate_next_player)) cyclePlayers(true);
        if(cuiKeyTap(spectate_previous_player)) cyclePlayers(false);

        if (CuiVars.lastCoreDestroyEvent != null && cuiKeyDown(last_destroyed_core) && !tracking){
            if(control.input instanceof DesktopInput input) input.panning = true;
            if(CuiVars.clickedPlayer != null) CuiVars.clickedPlayer = null;

            Core.camera.position.set(CuiVars.lastCoreDestroyEvent);
            tracking = true;
        }

        if (CuiVars.clickedPlayer != null && CuiVars.clickedPlayer.unit() != null && state.isPlaying() && !tracking){
            if((Math.abs(Core.input.axis(Binding.move_x)) > 0 || Math.abs(Core.input.axis(Binding.move_y)) > 0 || Core.input.keyTap(Binding.mouse_move) || Core.input.keyTap(Binding.pan)) && (!scene.hasField())){
                CuiVars.clickedPlayer = null;
                return;
            }

            if(control.input instanceof DesktopInput input) input.panning = true;
            trackingType = 4;

            //workaround for when in multiplayer, sometimes respawning puts you in 0,0 during the animation before moving your unit
            if ((CuiVars.clickedPlayer.unit() == null || CuiVars.clickedPlayer.unit().x == 0 && CuiVars.clickedPlayer.unit().y == 0) && CuiVars.clickedPlayer.team().data().hasCore()) trackingType = 3;
            if ( CuiVars.clickedPlayer.unit() != null && (CuiVars.clickedPlayer.unit().x != 0 && CuiVars.clickedPlayer.unit().y != 0)) trackingType = 1;
            if (cuiKeyDown(track_cursor) && settings.getBool("cui-playerHoldTrackMouse")) trackingType = 2;
            if (cuiKeyTap(track_cursor) && !settings.getBool("cui-playerHoldTrackMouse")) keepMouseTracking = !keepMouseTracking;
            if (keepMouseTracking && !settings.getBool("cui-playerHoldTrackMouse")) trackingType = 2;
            if (!keepMouseTracking && !settings.getBool("cui-playerHoldTrackMouse") && CuiVars.clickedPlayer.unit() != null) trackingType = 1;
            if (!keepMouseTracking && !settings.getBool("cui-playerHoldTrackMouse") && CuiVars.clickedPlayer.unit() == null) trackingType = 3;

            /* so many if statements, enjoy >;3c */
            switch (trackingType) {
                case 1 -> {
                    tracking = true;
                    Core.camera.position.lerpDelta(CuiVars.clickedPlayer.unit(), cameraFloat);
                }
                case 2 -> {
                    tracking = true;
                    camera.position.lerpDelta(CuiVars.clickedPlayer.mouseX, CuiVars.clickedPlayer.mouseY, cameraFloat);
                }
                case 3 -> {
                    tracking = true;
                    Core.camera.position.lerpDelta(CuiVars.clickedPlayer.bestCore(), cameraFloat);
                }
                case 4 -> {
                    tracking = false;
                    CuiVars.clickedPlayer = null;
                }
            }

        }


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

    public boolean cuiKeyTap(KeyBinds.KeyBind key){
        return CuiVars.rebindDialog.cuiKeyBinds.get(key).key != null && input.keyTap(CuiVars.rebindDialog.cuiKeyBinds.get(key).key);
    }

    public boolean cuiKeyDown(KeyBinds.KeyBind key){
        return CuiVars.rebindDialog.cuiKeyBinds.get(key).key != null && input.keyDown(CuiVars.rebindDialog.cuiKeyBinds.get(key).key);
    }


}
