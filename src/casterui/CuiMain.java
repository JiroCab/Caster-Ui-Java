package casterui;

import arc.Events;
import arc.util.Log;
import casterui.io.ui.dialog.CuiSettingsDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;

import static arc.Core.settings;

public class CuiMain extends Mod {


    public CuiMain() {
        try{
            Events.on(EventType.ClientLoadEvent.class, you -> CuiVars.init());
            Events.on(EventType.WorldLoadEvent.class, you -> CuiVars.postInt());
            Events.run(EventType.Trigger.update, CuiVars::update);
            Events.on(EventType.BlockDestroyEvent.class, block -> CuiVars.renderer.CoreDestroyAlert(block));
        } catch (Exception e){
            Log.err("CUI UI: can't load " + "\n: " + e +  e.getCause());
        }

    }

    @Override
    public void loadContent(){

    }


}
