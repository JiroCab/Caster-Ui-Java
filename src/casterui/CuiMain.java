package casterui;

import arc.Events;
import mindustry.game.EventType;
import mindustry.mod.Mod;

public class CuiMain extends Mod {


    public CuiMain() {
        Events.on(EventType.ClientLoadEvent.class, you -> CuiVars.init());
        Events.on(EventType.WorldLoadEvent.class, you -> CuiVars.postInt());
        Events.run(EventType.Trigger.update, CuiVars::update);
    }

    @Override
    public void loadContent(){

    }


}
