package casterui.io;

import arc.Core;
import casterui.CuiVars;
import casterui.io.ui.CuiBinding;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CuiInputs {

    public void update(){
        if (CuiVars.clickedPlayer != null && state.isPlaying()){

            if((Math.abs(Core.input.axis(Binding.move_x)) > 0 || Math.abs(Core.input.axis(Binding.move_y)) > 0 || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.pan)) && (!scene.hasField())){
                CuiVars.clickedPlayer = null;
                return;
            }

            if(control.input instanceof DesktopInput input){
                input.panning = true;
            }

            float cameraFloat = 0.085F;
            if (!Core.settings.getBool("smoothcamera")){ cameraFloat = 1;}

            //workaround for when in multiplayer, sometimes respawning puts you in 0,0 during the animation before moving your unit
            if ( CuiVars.clickedPlayer.unit() != null && (CuiVars.clickedPlayer.unit().x != 0 && CuiVars.clickedPlayer.unit().y != 0)){
                if (input.keyDown(CuiBinding.trackCursor)) camera.position.lerpDelta(CuiVars.clickedPlayer.mouseX, CuiVars.clickedPlayer.mouseY, cameraFloat);
                else Core.camera.position.lerpDelta(CuiVars.clickedPlayer.unit(), cameraFloat);
            } else {
                Core.camera.position.lerpDelta(CuiVars.clickedPlayer.bestCore(), cameraFloat);
            }
        }
    }



}
