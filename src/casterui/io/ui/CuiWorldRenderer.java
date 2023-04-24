package casterui.io.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.util.Time;
import casterui.CuiVars;
import mindustry.Vars;
import mindustry.ai.types.LogicAI;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.HashMap;
import java.util.Map;

import static arc.graphics.g2d.Draw.draw;


public class CuiWorldRenderer {
    public Map<EventType.BlockDestroyEvent, Float> circleQueue = new HashMap<>(), circleRemoveQueue = new HashMap<>();

    public int
            playerLineAlpha = Core.settings.getInt("cui-playerTrackAlpha"),
            trackedPlayerLineAlpha = Core.settings.getInt("cui-playerTrackedAlpha"),
            logicLineAlpha =  Core.settings.getInt("cui-logicLineAlpha") ;

    public void worldRenderer(){
        Events.run(EventType.Trigger.draw, ()-> {
            boolean unitBars = Core.settings.getBool("cui-showUnitBar", true);
            boolean trackPlayerCursor = Core.settings.getBool("cui-TrackPlayerCursor", false);
            boolean trackLogicControl = Core.settings.getBool("cui-TrackLogicControl", false);

            if (unitBars || trackLogicControl || trackPlayerCursor){
                Groups.unit.each((unit -> {
                    if (unitBars) DrawUnitBars(unit);
                    if (trackPlayerCursor && unit.isPlayer()) DrawPlayerCursor(unit);
                    if (trackLogicControl && unit.controller() instanceof LogicAI la) DrawLogicControl(unit, la.controller);
                }));
            }
            if (circleQueue.size() != 0 && Core.settings.getBool("cui-ShowAlertsCircles")){
                float maxSize = Math.max((Vars.state.map.height + Vars.state.map.width) / 1.85f, 3000f), growRate = Core.settings.getInt("cui-alertCircleSpeed") / 2f;
                circleRemoveQueue.forEach((a, b) -> circleQueue.remove(a, b));
                circleRemoveQueue.clear();

                Draw.draw(Layer.overlayUI + 0.01f, () -> circleQueue.forEach((e, startTime) ->{
                    float radius = Math.abs(startTime - Time.time ) * growRate + e.tile.block().size, size = Math.abs(startTime - Time.time ) * growRate + e.tile.block().size;
                    if(size > maxSize) circleRemoveQueue.put(e, startTime);
                    Draw.color(e.tile.team().color);
                    Drawf.circles(e.tile.getX(), e.tile.getY(), radius);
                    Draw.reset();
                }));
            }
        });
    }

    public void DrawUnitBars(Unit unit){

    }

    public void DrawPlayerCursor(Unit unit){
        if(unit.getPlayer() == Vars.player && !Core.settings.getBool("cui-ShowOwnCursor", true)) return;

        float cursorX = unit.getPlayer().mouseX, cursorY = unit.getPlayer().mouseY() , unitX = unit.getX(), unitY = unit.getY();
        int style = Core.settings.getInt("cui-playerCursorStyle");

        float alpha = unit.getPlayer() != CuiVars.clickedPlayer ? (float) (playerLineAlpha * 0.1) : (float) (trackedPlayerLineAlpha * 0.1);
        draw(Layer.overlayUI+0.01f, () ->{
            if (style % 2 == 0){DrawLine(cursorX, cursorY, unitX, unitY, unit.team.color, alpha);}

            if (style == 1 || style == 2) Drawf.square(cursorX, cursorY, 2,  unit.team().color); // Square (Inspired from Mindustry Ranked Server's spectator mode )
            else if (style == 3 || style == 4) Drawf.circles(cursorX, cursorY, 3, unit.team().color); // Circle
            else if (style == 5 || style == 6) Drawf.target(cursorX, cursorY, 3, alpha, unit.team().color); //Target (aka mobile mindustry)
            else DrawLine(cursorX, cursorY, unitX, unitY, unit.team.color, alpha);  //Line (originally from Ferlern/extended-UI')
        });
        Draw.reset();

    }

    public void DrawLine(float cx, float cy, float ux, float uy, Color color, float transparency) {
        Lines.stroke(1);
        Draw.color(color, transparency);
        Lines.line(ux, uy, cx, cy);
        Draw.reset();
    }

    public void DrawLine(float cx, float cy, float ux, float uy, Color color) {
        DrawLine(cx, cy, ux, uy, color, 1);
    }

    public void DrawLogicControl(Unit unit, Building processor){
        float unitX = unit.getX(), unitY = unit.getY(), processorX = processor.getX(), processorY = processor.getY();

        draw(Layer.overlayUI+0.01f, () -> {
            Lines.stroke(1, Color.purple);
            Lines.line(unitX, unitY, processorX, processorY);
            if (unit != CuiVars.heldUnit) Draw.alpha((float) (logicLineAlpha * 0.1));
            Draw.reset();
        });
    }

    public void CoreDestroyAlert(EventType.BlockDestroyEvent e){
        if(!(e.tile.build instanceof CoreBlock.CoreBuild)) return;

        if (Core.settings.getBool("cui-ShowAlertsCircles")) {
                circleQueue.put(e, Time.time);
        }

        if (Core.settings.getBool("cui-ShowAlerts")){
            Vars.ui.hudfrag.showToast("[#" + e.tile.team().color.toString() + "]" + e.tile.team().localized()+ " " + Core.bundle.get("alerts.basic") + "[white] (" +e.tile.x + ", "+ e.tile.y + ")");
        }

        if (Core.settings.getBool("cui-SendChatCoreLost")){
            Call.sendChatMessage("[#" + e.tile.team().color.toString() + "]" + e.tile.team().localized()+ " " + e.tile.block().localizedName + " "+ Core.bundle.get("alerts.destroy") + "[white] (" +e.tile.x + ", "+ e.tile.y + ")");
        }

        CuiVars.lastCoreDestroyEvent = e.tile;
    }

    public void DrawFactoryProgress(){
        if (!Core.settings.getBool("cui-showFactoryProgress", true)) return;

        Events.on(EventType.WorldLoadEvent.class, event -> {

        });
    }

    public void BarBuilder(float drawX, float drawY, float progress, float targetSizeInBlocks, float barSize, String labelText, Color color, float alpha ){
        if (progress == 0) return;

        float borderSize = 1f;
        float blockPixelSize = targetSizeInBlocks*8;
        float startX = drawX - blockPixelSize/2 - barSize;
        float startY = drawY + blockPixelSize/2;
        float endY = startY + barSize;

        float barLenght = blockPixelSize + barSize*2;
        float innerBarLenght = barLenght - borderSize*2;
        float innerBarHeight = barSize - borderSize*2;

        float fillSize = (innerBarLenght) * progress;

        Draw.z(Layer.darkness+1);
        Draw.alpha(alpha);
        Lines.rect(startX, startY, barLenght, barSize);

        Draw.color(color, alpha);
        Fill.rect(drawX - (innerBarLenght * (1 - progress))/2, startY + barSize/2, fillSize, innerBarHeight);

        if (labelText != null) {
            Core.bundle.format("{x}%", progress);
        }

        Draw.reset();


    }

}
