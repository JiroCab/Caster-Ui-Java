package casterui.io;

import arc.Core;
import casterui.CuiVars;
import mindustry.Vars;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CuiInputs {

    public void update(){
        float cameraFloat = 0.085F;
        if (!Core.settings.getBool("smoothcamera")){ cameraFloat = 1;}

        if (CuiVars.clickedPlayer != null && CuiVars.clickedPlayer.unit() != null && state.isPlaying()){

            if((Math.abs(Core.input.axis(Binding.move_x)) > 0 || Math.abs(Core.input.axis(Binding.move_y)) > 0 || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.pan)) && (!scene.hasField())){
                CuiVars.clickedPlayer = null;
                return;
            }

            if(control.input instanceof DesktopInput input){
                input.panning = true;
            }

            //workaround for when in multiplayer, sometimes respawning puts you in 0,0 during the animation before moving your unit
            if ( CuiVars.clickedPlayer.unit() != null && (CuiVars.clickedPlayer.unit().x != 0 && CuiVars.clickedPlayer.unit().y != 0)){
                if (input.keyDown(CuiBinding.track_cursor)) camera.position.lerpDelta(CuiVars.clickedPlayer.mouseX, CuiVars.clickedPlayer.mouseY, cameraFloat);
                else Core.camera.position.lerpDelta(CuiVars.clickedPlayer.unit(), cameraFloat);
            } else if (CuiVars.clickedPlayer.team().data().hasCore()) {
                Core.camera.position.lerpDelta(CuiVars.clickedPlayer.bestCore(), cameraFloat);
            } else {
                CuiVars.clickedPlayer = null;
            }
        }
        if (CuiVars.lastCoreDestroyEvent != null && input.keyRelease(CuiBinding.last_destroyed_core)){
            if(control.input instanceof DesktopInput input) input.panning = true;
            if(CuiVars.clickedPlayer != null) CuiVars.clickedPlayer = null;

            Core.camera.position.set(CuiVars.lastCoreDestroyEvent);
        }

        if(input.keyTap(CuiBinding.toggle_cui_menu) && !settings.getBool("cui-hideWithMenus")) CuiVars.unitTableCollapse = !CuiVars.unitTableCollapse;
        else if(settings.getBool("cui-hideWithMenus")) CuiVars.unitTableCollapse = ui.hudfrag.shown;
    }



}
