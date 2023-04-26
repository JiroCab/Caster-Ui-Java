package casterui.io;

import arc.Core;
import arc.struct.Seq;
import casterui.CuiVars;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CuiInputs {
    public boolean tracking = false, keepPlayerTracking = false;
    public int trackingType = 4;
    Seq<Player> ply = new Seq<>();
    public int playerNumber = 0;

    public void update(){
        if (state.isMenu()) return;

        if(input.keyTap(CuiBinding.toggle_cui_menu) && !settings.getBool("cui-hideWithMenus")) CuiVars.unitTableCollapse = !CuiVars.unitTableCollapse;
        else if(settings.getBool("cui-hideWithMenus")) CuiVars.unitTableCollapse = ui.hudfrag.shown;
        if(input.keyTap(CuiBinding.host_teams))CuiVars.teamManger.shouldShow();

        if (scene.hasField()) return;
        tracking = false;

        float cameraFloat = 0.085F;
        if (!Core.settings.getBool("smoothcamera")){ cameraFloat = 1;}

        if(input.keyTap(CuiBinding.spectate_next_player)) cyclePlayers(true);
        if(input.keyTap(CuiBinding.spectate_previous_player)) cyclePlayers(false);

        if (CuiVars.lastCoreDestroyEvent != null && input.keyDown(CuiBinding.last_destroyed_core) && !tracking){
            if(control.input instanceof DesktopInput input) input.panning = true;
            if(CuiVars.clickedPlayer != null) CuiVars.clickedPlayer = null;

            Core.camera.position.set(CuiVars.lastCoreDestroyEvent);
            tracking = true;
        }

        if (CuiVars.clickedPlayer != null && CuiVars.clickedPlayer.unit() != null && state.isPlaying() && !tracking){
            if((Math.abs(Core.input.axis(Binding.move_x)) > 0 || Math.abs(Core.input.axis(Binding.move_y)) > 0 || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.pan)) && (!scene.hasField())){
                CuiVars.clickedPlayer = null;
                return;
            }

            if(control.input instanceof DesktopInput input){
                input.panning = true;
            }


            if ( CuiVars.clickedPlayer != null && !tracking){
                trackingType = 4;
                //workaround for when in multiplayer, sometimes respawning puts you in 0,0 during the animation before moving your unit
                if ((CuiVars.clickedPlayer.unit() == null || CuiVars.clickedPlayer.unit().x != 0 && CuiVars.clickedPlayer.unit().y != 0) && CuiVars.clickedPlayer.team().data().hasCore()) trackingType = 3;
                if ( CuiVars.clickedPlayer.unit() != null && (CuiVars.clickedPlayer.unit().x != 0 && CuiVars.clickedPlayer.unit().y != 0)) trackingType = 1;
                if (input.keyDown(CuiBinding.track_cursor) && settings.getBool("cui-playerHoldTrackMouse")) trackingType = 2;
                if (input.keyTap(CuiBinding.track_cursor) && !settings.getBool("cui-playerHoldTrackMouse")) keepPlayerTracking = !keepPlayerTracking;
                if (keepPlayerTracking && !settings.getBool("cui-playerHoldTrackMouse")) trackingType = 2;

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
    }

    void cyclePlayers(boolean increment){
        ply.clear();
        Groups.player.forEach(ply::add);
        ply.remove(player);

        if (ply.size < 1) return;
        int number = playerNumber;

        if(increment) number++;
        else number--;

        if(number >= ply.size ) number = 0;
        if(number <= -1) number = ply.size - 1;

        playerNumber = number;

        //Log.err(playerNumber + " | " + number +"/" + ply.size + " | " +ply.get(playerNumber).name());
        CuiVars.clickedPlayer = ply.get(playerNumber);
    }

}
