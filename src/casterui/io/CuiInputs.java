package casterui.io;

import arc.Core;
import casterui.CuiVars;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CuiInputs {

    public void update(){
        if (CuiVars.fragment.clickedPlayer != null && state.isPlaying()){

            if((Math.abs(Core.input.axis(Binding.move_x)) > 0 || Math.abs(Core.input.axis(Binding.move_y)) > 0 || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.mouse_move) || input.keyDown(Binding.pan)) && (!scene.hasField())){
                CuiVars.fragment.clickedPlayer = null;
                return;
            }

            if(control.input instanceof DesktopInput input){
                input.panning = true;
            }

            float cameraFloat = 0.085F;
            if (!Core.settings.getBool("smoothcamera")){ cameraFloat = 1;}


            //workaround for when in multiplayer, sometimes respawning puts you in 0,0 during the animation before moving your unit
            if (CuiVars.fragment.clickedPlayer.unit() != null && (CuiVars.fragment.clickedPlayer.unit().x != 0 && CuiVars.fragment.clickedPlayer.unit().y != 0)) {Core.camera.position.lerpDelta(CuiVars.fragment.clickedPlayer.unit(), cameraFloat);
            } else {
                Core.camera.position.lerpDelta(CuiVars.fragment.clickedPlayer.bestCore(), cameraFloat);
            }
        }
    }

}
