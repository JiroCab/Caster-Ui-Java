package casterui.io.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import arc.util.pooling.Pools;
import casterui.CuiVars;
import casterui.util.CuiCircleObjectHelper;
import mindustry.Vars;
import mindustry.ai.types.LogicAI;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.Fonts;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;

import static arc.graphics.g2d.Draw.draw;
import static mindustry.Vars.*;


public class CuiWorldRenderer {
    public Seq<CuiCircleObjectHelper> circleQueue = new Seq<>();
    private long lastToast;

    public void worldRenderer(){
        Events.run(EventType.Trigger.draw, ()-> {
            boolean unitBars = Core.settings.getBool("cui-showUnitBar");
            boolean trackPlayerCursor = Core.settings.getBool("cui-TrackPlayerCursor");
            boolean trackLogicControl = Core.settings.getInt("cui-logicLineAlpha") > 0;

            if (unitBars || trackLogicControl || trackPlayerCursor){
                Groups.unit.each((unit -> {
                    if (unitBars) drawUnitBars(unit);
                    if (trackLogicControl && unit.controller() instanceof LogicAI la) drawLogicControl(unit, la.controller);
                }));
                if(trackPlayerCursor) Groups.player.each(ply ->{
                    if(ply.unit() != null){drawPlayerCursor(ply);}
                });
            }
            if (circleQueue.size != 0 && Core.settings.getBool("cui-ShowAlertsCircles")){
                /*Why would anyone need this amount control for the speed? ehh why not!*/
                float maxSize = Math.max((Vars.state.map.height + Vars.state.map.width) / 2.1f, 3000f), growRate = Math.round(Core.settings.getInt("cui-alertCircleSpeed") * 0.5f);

                Draw.draw(Layer.overlayUI + 0.01f, () -> circleQueue.forEach((cir) -> {
                    float radius = Math.abs(cir.startTime - Time.time) * growRate + cir.size, size = Math.abs(cir.startTime - Time.time) * growRate + cir.size;
                    if (size > maxSize) circleQueue.remove(cir);
                    if( Core.settings.getBool("cui-alertReverseGrow")){
                        float pr = radius;
                        radius = maxSize - radius;
                        Log.err("pr:" + pr + " r:" + radius);
                    }
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
                Draw.draw(Layer.overlayUI+0.01f, () -> drawLine(block.getX(), block.getY(), block.getCommandPosition().x ,block.getCommandPosition().getY(), block.team.color, (Core.settings.getInt("cui-rallyPointAlpha") - 1) * 0.1f));
            }
            drawFactoryProgress();
        });
        Events.run(EventType.Trigger.update, () -> {
            boolean trackLogicControl = Core.settings.getInt("cui-logicLineAlpha") > 0;
            Groups.unit.each(unit -> {
                if (trackLogicControl && unit.controller() instanceof LogicAI la) drawLogicControl(unit, la.controller);
            });
        });
    }

    public void drawUnitBars(Unit unit){
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

    public void drawPlayerCursor(Player ply){
        if(ply == Vars.player && !Core.settings.getBool("cui-ShowOwnCursor")) return;
        if(ply.unit() == null)return;

        boolean isTracked = ply == CuiVars.clickedPlayer;
        if(isTracked && Core.settings.getInt("cui-playerTrackedAlpha") == 0) return;
        else if(Core.settings.getInt("cui-playerTrackAlpha")== 0) return;

        Unit unit = ply.unit();

        float cursorX = ply.mouseX, cursorY = ply.mouseY() , unitX = unit.getX(), unitY = unit.getY();
        //respawning put you in 0,0 before moving your unit, this a workaround for it to look less jarring
        if(ply.mouseX == 0f &&  ply.mouseY == 0f &&ply.team().data().hasCore() ){
            cursorX = ply.bestCore().x();
            cursorY = ply.bestCore().y();
        }
        int style = Core.settings.getInt("cui-playerCursorStyle"), lines = Core.settings.getInt("cui-playerLineStyle");


        float alpha = !isTracked ? (float) (Core.settings.getInt("cui-playerTrackAlpha") * 0.1) : (float) (Core.settings.getInt("cui-playerTrackedAlpha") * 0.1);

        float finalCursorX = cursorX, finalCursorY = cursorY;
        Draw.draw(Layer.overlayUI+0.01f, () ->{
            Lines.stroke(1, ply.team().color);
            Draw.color(ply.team().color, alpha);

            if(lines == 1) drawLine(finalCursorX, finalCursorY, unitX, unitY, ply.team().color, alpha);
            else if(lines == 2)Lines.dashLine(finalCursorX, finalCursorY, unitX, unitY, Math.round(unit.dst(finalCursorX, finalCursorY) / 2));
            else if(lines == 3)Drawf.line(ply.team().color, finalCursorX, finalCursorY, unitX, unitY);
            Draw.reset();
        });

        Draw.draw(Layer.overlayUI+0.02f, () ->{
            Draw.color(ply.team().color, alpha);

            if (style == 2) Drawf.circles(finalCursorX, finalCursorY, 3, ply.team().color); // Circle
            else if (style == 3) Drawf.target(finalCursorX, finalCursorY, 3, alpha, ply.team().color); //Target (aka mobile mindustry)
            else if (style == 4) Fill.circle(finalCursorX, finalCursorY, 3); //Foo's style
            else Drawf.square(finalCursorX, finalCursorY, 2,  ply.team().color); // Square (Inspired from Mindustry Ranked Server's spectator mode )
            Draw.reset();
        });
        Draw.reset();
        if(Core.settings.getInt("cui-playerDrawNames") == 2 || Core.settings.getInt("cui-playerDrawNames") == 1 && isTracked ){
            drawLabel(finalCursorX, finalCursorY, ply.name, ply.team().color);
            Draw.reset();
        }

    }


    public void drawLabel(float x, float y, String text, Color color, float yOffset, float margin){
        Font font = Fonts.outline;
        GlyphLayout l = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.getData().setScale(1 / 5f / Scl.scl(1f));
        font.setUseIntegerPositions(false);

        l.setText(font, text, color, 90f, Align.left, true);


        Draw.color(0f, 0f, 0f, 0.2f);
        Fill.rect(x, y + yOffset - l.height/2f, l.width + margin, l.height + margin);
        Draw.color();
        font.setColor(color);
        font.draw(text, x - l.width/2f, y + yOffset, 90f, Align.left, true);
        font.setUseIntegerPositions(ints);

        font.getData().setScale(1f);

        Pools.free(l);
    }
    public void drawLabel(float x, float y, String text, Color color){drawLabel(x, y, text, color, 7f, 1.7f);}

    public void drawLine(float cx, float cy, float ux, float uy, Color color, float transparency) {
        Lines.stroke(1, color);
        Draw.color(color, transparency);
        Lines.line(ux, uy, cx, cy);
        Draw.reset();
    }

    public void drawLine(float cx, float cy, float ux, float uy, Color color) {
        drawLine(cx, cy, ux, uy, color, 1);
    }

    public void drawLogicControl(Unit unit, Building processor){
        float unitX = unit.getX(), unitY = unit.getY(), processorX = processor.getX(), processorY = processor.getY();

        draw(Layer.overlayUI+0.01f, () -> {
            drawLine(unitX, unitY, processorX, processorY, Color.purple, CuiVars.heldUnit != null && CuiVars.heldUnit.type == unit.type ? 1f : Core.settings.getInt("cui-logicLineAlpha") * 0.1f  );

            /*
            Lines.stroke(1, Color.purple);
            Draw.color(Color.purple, CuiVars.heldUnit != null && CuiVars.heldUnit.type == unit.type ? 1f : Core.settings.getInt("cui-logicLineAlpha") * 0.1f );
            Lines.line(unitX, unitY, processorX, processorY);
            Draw.reset();*/
        });
    }

    public void coreDestroyAlert(EventType.BlockDestroyEvent e){
        if(!(e.tile.build instanceof CoreBlock.CoreBuild)) return;

        if (Core.settings.getBool("cui-ShowAlertsCircles")) {
                circleQueue.add(new CuiCircleObjectHelper(e.tile, e.tile.team(), Time.time, e.tile.block().size));
        }

        if (Core.settings.getBool("cui-ShowAlerts")){
            if(Vars.state.isPlaying()){
                String alert = "[#" + e.tile.team().color.toString() + "]" + e.tile.team().localized()+ " " + Core.bundle.get("alerts.basic") + "[white] (" +e.tile.x + ", "+ e.tile.y + ")";
                if(Core.settings.getBool("cui-AlertsHideWithUi") || Core.settings.getBool("cui-AlertsUseBottom")) showToastIndependent(alert, Core.settings.getBool("cui-AlertsUseBottom"));
                else Vars.ui.hudfrag.showToast(alert);
            }
        }

        if (Core.settings.getBool("cui-SendChatCoreLost")){
            Call.sendChatMessage("[#" + e.tile.team().color.toString() + "]" + e.tile.team().localized()+ " " + e.tile.block().localizedName + " "+ Core.bundle.get("alerts.destroy") + "[white] (" +e.tile.x + ", "+ e.tile.y + ")");
        }

        CuiVars.lastCoreDestroyEvent = e.tile;
    }

    public void drawFactoryProgress(){
        int style = Core.settings.getInt("cui-showFactoryProgressStyle");
        if(style == 1) return;
        Groups.build.forEach(b ->{
            if(!(b instanceof UnitFactory.UnitFactoryBuild fac)) return;
            if(fac.currentPlan == -1) return;

            if(style == 2 || style == 3 || style == 4 || style == 5){
                float x = fac.x(), y= fac.y(), width = (fac.block.size * 4) * -0.9f, hp = (fac.fraction()),
                              offset = ((fac.block.size * 4)  * 0.9f) * (style == 2 || style == 5 ? 1 : -1);
                Draw.alpha(0.5f);
                Draw.draw(Layer.flyingUnit +0.01f, () ->{
                    Drawf.line(Pal.gray, x + width, y - offset, x - (width), y - offset);
                    Drawf.line(fac.team.color, x + width * hp, y - offset, x - (width * hp), y - offset);
                });
            }
            if(style == 2 || style == 3 || style == 6)drawLabel(fac.x(), fac.y, Math.round(fac.fraction() * 100) + "%", Color.white, 0f, 1.7f);
            Draw.reset();
        });
    }

    public void barBuilder(float drawX, float drawY, float progress, float targetSizeInBlocks, float barSize, String labelText, Color color, float alpha ){
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

    public void showToastIndependent(String text, boolean place){
        showBottomToast(Icon.warning, -1, text, place);
    }

    public void showBottomToast(Drawable icon, float size, String text, boolean bottom){
        float p = bottom ? -1 : 1;
        scheduleToast(() -> {
            Sounds.message.play();
            Table table = new Table(Tex.button);
            table.update(() -> {
                if(state.isMenu() || !ui.hudfrag.shown && Core.settings.getBool("cui-AlertsHideWithUi") || !Core.settings.getBool("cui-AlertsHideWithUi") && !CuiVars.unitTableCollapse){
                    table.remove();
                }
            });
            table.margin(12);
            var cell = table.image(icon).pad(3);
            if(size > 0) cell.size(size);
            table.add(text).wrap().width(280f).get().setAlignment(Align.center, Align.center);
            table.pack();

            //create container table which will align and move
            Table container = Core.scene.table();
            if(bottom)container.bottom();
            else container.top();
            container.add(table);
            container.setTranslation(0, p * table.getPrefHeight());
            container.actions(Actions.translateBy(0, p * -table.getPrefHeight(), 1f, Interp.fade), Actions.delay(2.5f),
                    //nesting actions() calls is necessary so the right prefHeight() is used
                    Actions.run(() -> container.actions(Actions.translateBy(0, p * table.getPrefHeight(), 1f, Interp.fade), Actions.remove())));
        });
    }

    private void scheduleToast(Runnable run){
        long duration = (int)(3.5 * 1000);
        long since = Time.timeSinceMillis(lastToast);
        if(since > duration){
            lastToast = Time.millis();
            run.run();
        }else{
            Time.runTask((duration - since) / 1000f * 60f, run);
            lastToast += duration;
        }
    }
}
