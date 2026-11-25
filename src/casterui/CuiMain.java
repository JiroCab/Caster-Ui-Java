package casterui;

import arc.*;
import arc.util.*;
import arc.util.serialization.*;
import casterui.dialogs.*;
import casterui.util.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mindustry.mod.*;

public class CuiMain extends Mod{
    public  static CuiVars data;

    public CuiMain(){
        Events.on(ClientLoadEvent.class, e -> {
            CuiClassMap.load(this.getClass().getPackage().getName());
            data = new CuiVars();

            data.alertToastStyle = 69;

            CuiSettingsDialog.buildCategory();
        });

        Events.run(Trigger.update, () -> {
            for(CuiBaseModule m : data.modules) m.update();
        });

        Events.on(WorldLoadEndEvent.class, e -> {
            for(CuiBaseModule m : data.modules){
                m.reset();
            }
        });
    }

    @Override
    public void loadContent(){
        super.loadContent();
    }

}
