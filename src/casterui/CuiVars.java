package casterui;

import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import casterui.modules.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.io.*;

import java.util.*;

public class CuiVars{
        //Note `*` means its the default

        /* == Global Settings == */
        // > player trackers
        public int
            trackerPlayerCursorStyle = 3,
            trackerPlayerLineStyle = 1,
            trackerPlayerTransparency = 1,
            trackerFocusedCursorStyle = 3,
            trackerFocusedLineStyle = 1,
            trackerFocusedTransparency =1
        ;

        public boolean
            trackerBackground = true,
            trackerTyping = true,
            trackerCoreless = true,
            trackerHoldMouse = true
        ;

        // trackerIgnoredTeams
        // trackerIgnoredPlayers

        // > core alerts
        public int
            alertToastStyle = 0,
            alertWorldGrowSpeed =12,
            alertWorldStyle = 1,
            alertChatCondition = 0 //off, host only, host + admin, always
        ;

        // > Unit healthBars
        public int
            unitBarsBarStyle =7,
            unitBarsBarSize = 4,
            unitBarsBarTransparency = 10,
            unitBarsTextStyle = 1
        ;

        // > Unit Command Preview
        public int
            cmdRangeStyle =1,
            cmdRange = 6,
            cmdLineTransparency = 10,
            cmdLineStyle = 1,
            cmdTypeSize = 25,
            cmdTypeTransparency = 80,
            cmdPointerStyle = 1
        ;

        // > Block healthBars
        public int
            blockBarsBarStyle = 2,
            blockBarsBarSize = 10,
            blockBarsBarTransparency = 80,
            blockBarsTextStyle = -1,
            blockBarsFactoryBarStyle = -1,
            blockBarsFactoryRallyStyle = -1,
            blockBarsFactoryRallyTransparency = 80,
            blockBarsLogicLineStyle = 80,
            blockBarsLogicRallyStyle = -1,
            blockBarsLogicRallyTransparency = 40,
            blockBarsLogicRange= 6
        ;

        public float debug = 0;



    public CuiVars copy(){
        return JsonIO.copy(this);
    }

    public static CuiHoverBlockInfo owo = new CuiHoverBlockInfo();

    public Seq<CuiBaseModule> modules =  Seq.with(new CuiHoverBlockInfo());

    public class Modules implements JsonSerializable{
        @Override
        public void write(Json json){
            for(CuiBaseModule module : modules){
                json.writeValue(module.getName(), JsonIO.write(module));
            }
        }

        @Override
        public void read(Json json, JsonValue jsonData){
            for(JsonValue m : jsonData){

                if(Objects.equals(m.name, owo.getName())){
                    @Nullable CuiBaseModule module = CuiModuleHandler.readModule(m.name);
                    if(module != null){
                        modules.add(json.readValue(module.getClass(), m));
                    }
                }
            }
            Log.err(modules.toString());
        }
    }
}
