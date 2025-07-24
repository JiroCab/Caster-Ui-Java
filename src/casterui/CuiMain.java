package casterui;

import arc.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.mod.*;

public class CuiMain extends Mod {


    public CuiMain() {
        try{
            Events.on(EventType.ClientLoadEvent.class, you -> CuiVars.init());
            Events.on(EventType.WorldLoadEvent.class, you -> CuiVars.postInt());
            Events.run(EventType.Trigger.update, CuiVars::update);
            Events.on(EventType.BlockDestroyEvent.class, block -> CuiVars.renderer.coreDestroyAlert(block));
        } catch (Exception e){
            Log.err("CUI UI: can't load " + "\n: " + e +  e.getCause());
        }

    }

    @Override
    public void loadContent(){

    }


}
