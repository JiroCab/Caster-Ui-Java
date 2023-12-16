package casterui.io.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.struct.Seq;
import arc.util.Time;
import casterui.CuiVars;
import casterui.util.CuiCircleObjectHelper;
import mindustry.Vars;
import mindustry.ai.types.LogicAI;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.storage.CoreBlock;

import static arc.graphics.g2d.Draw.draw;


public class CuiWorldRenderer {
    public Seq<CuiCircleObjectHelper> circleQueue = new Seq<>();

    public void worldRenderer(){
        Events.run(EventType.Trigger.draw, ()-> {
            boolean unitBars = Core.settings.getBool("cui-showUnitBar");
            boolean trackPlayerCursor = Core.settings.getBool("cui-TrackPlayerCursor");
            boolean trackLogicControl = Core.settings.getBool("cui-TrackLogicControl");

            if (unitBars || trackLogicControl || trackPlayerCursor){
                Groups.unit.each((unit -> {
                    if (unitBars) DrawUnitBars(unit);
                    if (trackLogicControl && unit.controller() instanceof LogicAI la) DrawLogicControl(unit, la.controller);
                }));
                if(trackPlayerCursor) Groups.player.each(ply ->{
                    if(ply.unit() != null){DrawPlayerCursor(ply);}
                });
            }
            if (circleQueue.size != 0 && Core.settings.getBool("cui-ShowAlertsCircles")){
                /*Why would anyone need this amount control for the speed? ehh why not!*/
                float maxSize = Math.max((Vars.state.map.height + Vars.state.map.width) / 1.85f, 3000f), growRate = Math.round(Core.settings.getInt("cui-alertCircleSpeed") * 0.5f);

                Draw.draw(Layer.overlayUI + 0.01f, () -> circleQueue.forEach((cir) -> {
                    float radius = Math.abs(cir.startTime - Time.time) * growRate + cir.size, size = Math.abs(cir.startTime - Time.time) * growRate + cir.size;
                    if (size > maxSize) circleQueue.remove(cir);
                    Draw.color(cir.team.color);

                    int style = Core.settings.getInt("cui-alertStyle");
                    if (style == 2){Drawf.dashCircle(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);}
                    else if(style == 3){Drawf.select(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);}
                    else if(style == 4){Drawf.square(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);}
                    else if(style == 5){Drawf.target(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);}
                    else Drawf.circles(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);
                    Draw.reset();
                }));
            }

            if(CuiVars.drawRally && Core.settings.getInt("cui-logicLineAlpha" ) != 1){
                Building block = CuiVars.fragment.mouseBuilding;
                Draw.draw(Layer.overlayUI+0.01f, () -> DrawLine(block.getX(), block.getY(), block.getCommandPosition().x ,block.getCommandPosition().getY(), block.team.color, (Core.settings.getInt("cui-rallyPointAlpha") - 1) * 0.1f));
            }
        });
        Events.run(EventType.Trigger.update, () -> {
            boolean trackLogicControl = Core.settings.getBool("cui-TrackLogicControl", false);
            Groups.unit.each(unit -> {
                if (trackLogicControl && unit.controller() instanceof LogicAI la) DrawLogicControl(unit, la.controller);
            });
        });
    }

    public void DrawUnitBars(Unit unit){
        if(unit.dead()) return;
        float x = unit.x(), y= unit.y(), offset = unit.hitSize  * 0.9f, width = unit.hitSize() * -0.9f, hp = (unit.health / unit.maxHealth);
        Draw.alpha(0.5f);
        Draw.draw(Layer.flyingUnit +0.01f, () ->{

            Drawf.line(Pal.gray, x + width, y - offset, x - (width), y - offset);
            Drawf.line(unit.team.color, x + width * hp, y - offset, x - (width * hp), y - offset);
            //wip
        });
        Draw.reset();
    }

    public void DrawPlayerCursor(Player ply){
        if(ply == Vars.player && !Core.settings.getBool("cui-ShowOwnCursor")) return;
        if(ply.unit() == null)return;

        boolean isTracked = ply == CuiVars.clickedPlayer;
        if(isTracked){
            if(Core.settings.getInt("cui-playerTrackedAlpha") == 0) return;
        } else if(Core.settings.getInt("cui-playerTrackAlpha")== 0) return;

        Unit unit = ply.unit();

        float cursorX = ply.mouseX, cursorY = ply.mouseY() , unitX = unit.getX(), unitY = unit.getY();
        //respawning put you in 0,0 before moving your unit, this a workaround for it to look less jarring
        if(ply.mouseX == 0f &&  ply.mouseY == 0f &&ply.team().data().hasCore() ){
            cursorX = ply.bestCore().x();
            cursorY = ply.bestCore().y();
        }
        int style = Core.settings.getInt("cui-playerCursorStyle");


        float alpha = isTracked ? (float) (Core.settings.getInt("cui-playerTrackAlpha") * 0.1) : (float) (Core.settings.getInt("cui-playerTrackedAlpha") * 0.1);

        float finalCursorX = cursorX, finalCursorY = cursorY;
        Draw.draw(Layer.overlayUI+0.01f, () ->{
            if (style % 2 == 0){DrawLine(finalCursorX, finalCursorY, unitX, unitY, ply.team().color, alpha);}

            if (style == 1 || style == 2) Drawf.square(finalCursorX, finalCursorY, 2,  ply.team().color); // Square (Inspired from Mindustry Ranked Server's spectator mode )
            else if (style == 3 || style == 4) Drawf.circles(finalCursorX, finalCursorY, 3, ply.team().color); // Circle
            else if (style == 5 || style == 6) Drawf.target(finalCursorX, finalCursorY, 3, alpha, ply.team().color); //Target (aka mobile mindustry)
            else DrawLine(finalCursorX, finalCursorY, unitX, unitY, unit.team.color, alpha);  //Line (originally from Ferlern/extended-UI')
        });
        Draw.reset();

    }

    public void DrawLine(float cx, float cy, float ux, float uy, Color color, float transparency) {
        Lines.stroke(1, color);
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
            DrawLine(unitX, unitY, processorX, processorY, Color.purple, CuiVars.heldUnit != null && CuiVars.heldUnit.type == unit.type ? 1f : Core.settings.getInt("cui-logicLineAlpha") * 0.1f  );

            /*
            Lines.stroke(1, Color.purple);
            Draw.color(Color.purple, CuiVars.heldUnit != null && CuiVars.heldUnit.type == unit.type ? 1f : Core.settings.getInt("cui-logicLineAlpha") * 0.1f );
            Lines.line(unitX, unitY, processorX, processorY);
            Draw.reset();*/
        });
    }

    public void CoreDestroyAlert(EventType.BlockDestroyEvent e){
        if(!(e.tile.build instanceof CoreBlock.CoreBuild)) return;

        if (Core.settings.getBool("cui-ShowAlertsCircles")) {
                circleQueue.add(new CuiCircleObjectHelper(e.tile, e.tile.team(), Time.time, e.tile.block().size));
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
