package casterui.io.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ai.types.LogicAI;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.blocks.storage.CoreBlock;

import static arc.graphics.g2d.Draw.draw;


public class CuiWorldRenderer {
    public int
            playerLineAlpha = Core.settings.getInt("cui-playerTrackAlpha"),
            logicLineAlpha =  Core.settings.getInt("cui-logicLineAlpha") ;

    public void worldRenderer(){
        Events.run(EventType.Trigger.draw, ()-> {
            boolean unitBars = Core.settings.getBool("cui-showUnitBar", true);
            boolean trackPlayerCursor = Core.settings.getBool("cui-TrackPlayerCursor", false);
            boolean trackLogicControl = Core.settings.getBool("cui-TrackLogicControl", false);

            if(Core.settings.getBool("cui-ShowAlerts") || Core.settings.getBool("cui-ShowAlertsCircles")) CoreDestroyAlert();

            if ((!unitBars && !trackLogicControl && !trackPlayerCursor))return;

            Groups.unit.each((unit -> {
                if (unitBars) DrawUnitBars(unit);
                if (trackPlayerCursor && unit.isPlayer())DrawPlayerCursor(unit);
                if (trackLogicControl && unit.controller() instanceof LogicAI)DrawLogicControl(unit);
            }));


        });
    }

    public void DrawUnitBars(Unit unit){

    }

    public void DrawPlayerCursor(Unit unit){
        if(unit.getPlayer() == Vars.player && !Core.settings.getBool("cui-ShowOwnCursor", true)) return;

        float cursorX = unit.getPlayer().mouseX, cursorY = unit.getPlayer().mouseY() , unitX = unit.getX(), unitY = unit.getY();
        int style = Core.settings.getInt("cui-playerCursorStyle");

        draw(Layer.overlayUI+0.01f, ()->{
            if (style % 2 == 0)DrawLine(cursorX, cursorY, unitX, unitY, unit.team.color);
            Draw.alpha((float) (playerLineAlpha * 0.1));

            if (style == 1 || style == 2) Drawf.square(cursorX, cursorY, 2, unit.team().color); // Square (Inspired from Mindustry Ranked Server's spectator mode )
            else if (style == 3 || style == 4) Drawf.circles(cursorX, cursorY, 2, unit.team().color); // Circle
            else if (style == 5 || style == 6) Drawf.target(cursorX, cursorY, 2, unit.team().color); //Target
            else DrawLine(cursorX, cursorY, unitX, unitY, unit.team.color);  //Line (originally from Ferlern/extended-UI')
        });

    }

    public void DrawLine(float cx, float cy, float ux, float uy, Color color) {
        Lines.stroke(1, color);
        Draw.alpha(0.7f);
        Lines.line(ux, uy, cx, cy);
        Draw.reset(); // it is necessary?
    }

    public void DrawLogicControl(Unit unit){
        Unit processor = unit.controller().unit();
        if(processor.type == UnitTypes.block) return;

        float unitX = unit.getX(), unitY = unit.getY(), processorX = processor.getX(), processorY = processor.getY();

        draw(Layer.overlayUI+0.01f, () -> {
            Lines.stroke(1, Color.purple);
            Draw.alpha((float) (logicLineAlpha * 0.1));
            Lines.line(unitX, unitY, processorX, processorY);
            Draw.reset();
        });
    }

    public void CoreDestroyAlert(){
        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if(!(e.tile.block() instanceof CoreBlock)) return;

            if (Core.settings.getBool("eui-ShowAlerts")){
                //Unnamed team toast
                if (e.tile.team().id >= 6){
                    Vars.ui.hudfrag.showToast("["+e.tile.team().color+"]"+e.tile.team().toString()+"alerts.basic"+"("+e.tile.team().toString()+")");
                }
                //Named team toast
                else Vars.ui.hudfrag.showToast("alerts."+e.tile.team().toString());
            }

            if (Core.settings.getBool("eui-ShowAlertsCircles")) {
                float circlesAmount = 1, growSpeed = 1, maxRadius = 2000, radius = 0;
                float startTime = Time.time;

                Draw.color(e.tile.team().color);
                for (int i = 0; i < circlesAmount; i++) {
                    if (radius > maxRadius) return;

                    Drawf.circles(e.tile.x, e.tile.y, radius * (1f + 0.2f *i), e.tile.team().color != null ? e.tile.team().color : Color.red);
                    radius += (Time.time - startTime) / 8 * growSpeed;
                }
            }

        });
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
        float barHeight = barSize;
        float innerBarHeight = barHeight - borderSize*2;

        float fillSize = (innerBarLenght) * progress;

        Draw.z(Layer.darkness+1);
        Draw.alpha(alpha);
        Lines.rect(startX, startY, barLenght, barHeight);

        Draw.color(color, alpha);
        Fill.rect(drawX - (innerBarLenght * (1 - progress))/2, startY + barSize/2, fillSize, innerBarHeight);

        if (labelText != null) {
            Core.bundle.format("{x}%", progress);
        }

        Draw.reset();


    }

}
