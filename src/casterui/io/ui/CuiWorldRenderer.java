package casterui.io.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import arc.util.pooling.Pools;
import casterui.CuiVars;
import casterui.util.CuiCircleObjectHelper;
import casterui.util.CuiPointerHelper;
import mindustry.Vars;
import mindustry.ai.types.LogicAI;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.Fonts;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;

import java.util.HashMap;

import static arc.graphics.g2d.Draw.draw;
import static mindustry.Vars.*;


public class CuiWorldRenderer {
    public Seq<CuiCircleObjectHelper> circleQueue = new Seq<>();
    public Seq<CuiPointerHelper> cmdPointerHelper = new Seq<>();
    private long lastToast;
    float cmdRange = 0, cmdTrans = 0;
    int cmdStyle = 0, cmdPointer = 0, cmdLimit = 0;

    public void worldRenderer(){
        Events.run(EventType.Trigger.draw, ()-> {
            if (Core.settings.getBool("cui-killswitch")) return;
            boolean unitBars = Core.settings.getInt("cui-showUnitBarStyle") > 0;
            boolean trackPlayerCursor = Core.settings.getBool("cui-TrackPlayerCursor");
            boolean trackLogicControl = Core.settings.getInt("cui-logicLineAlpha") > 0;
            boolean unitCmds = Core.settings.getInt("cui-unitscommands") > 0;

            if (unitBars || trackLogicControl || trackPlayerCursor || unitCmds){
                int style = Core.settings.getInt("cui-showUnitBarStyle");
                float alpha = Core.settings.getInt("cui-showUnitBarAlpha") * 0.1f, stroke = Core.settings.getInt("cui-showUnitBarSize") * 0.25f;
                cmdRange = (Core.settings.getInt("cui-unitCmdRange") * 0.5f) * tilesize;
                cmdTrans = Core.settings.getInt("cui-showUnitBarAlpha") * 0.1f;
                cmdStyle =  Core.settings.getInt("cui-unitCmdStyle");
                cmdPointer =  Core.settings.getInt("cui-unitCmdPointer");
                cmdLimit = Core.settings.getInt("cui-unitscommands");

                Groups.unit.each((unit -> {
                    if (unitBars) drawUnitBars(unit, style, alpha, stroke);
                    if (trackLogicControl && unit.controller() instanceof LogicAI la) drawLogicControl(unit, la.controller);
                    if (unitCmds)drawUnitCmds(unit);
                }));
                if(unitCmds) drawUnitPointer();
                if(trackPlayerCursor) Groups.player.each(ply ->{
                    if(ply.unit() != null){drawPlayerCursor(ply);}
                });
            }
            if (circleQueue.size != 0 && Core.settings.getBool("cui-ShowAlertsCircles")){
                /*Why would anyone need this amount control for the speed? ehh why not!*/
                float maxSize = Math.max((Vars.state.map.height + Vars.state.map.width) / 2.1f, 3000f), growRate = Math.round(Core.settings.getInt("cui-alertCircleSpeed") * 0.5f);

                Draw.draw(Layer.overlayUI + 0.01f, () -> {
                    for (CuiCircleObjectHelper cir : circleQueue) {
                        float radius = Math.abs(cir.startTime - Time.time) * growRate + cir.size, size = Math.abs(cir.startTime - Time.time) * growRate + cir.size;
                        if (size > maxSize) circleQueue.remove(cir);
                        if (Core.settings.getBool("cui-alertReverseGrow")) {
                            float pr = radius;
                            radius = maxSize - radius;
                            Log.err("pr:" + pr + " r:" + radius);
                        }
                        Draw.color(cir.team.color);

                        int style = Core.settings.getInt("cui-alertStyle");
                        if (style == 2) {
                            Drawf.dashCircle(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);
                        } else if (style == 3) {
                            Drawf.select(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);
                        } else if (style == 4) {
                            Drawf.square(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);
                        } else if (style == 5) {
                            Drawf.target(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);
                        } else Drawf.circles(cir.pos.getX(), cir.pos.getY(), radius, cir.team.color);
                        Draw.reset();
                    }
                });
            }

            if(CuiVars.drawRally && Core.settings.getInt("cui-logicLineAlpha" ) != 1){
                Building block = CuiVars.fragment.mouseBuilding;
                Draw.draw(Layer.overlayUI+0.01f, () -> drawLine(block.getX(), block.getY(), block.getCommandPosition().x ,block.getCommandPosition().getY(), block.team.color, (Core.settings.getInt("cui-rallyPointAlpha") - 1) * 0.1f));
            }
            drawFactoryProgress();
        });
        Events.run(EventType.Trigger.update, () -> {
            if (Core.settings.getBool("cui-killswitch")) return;
            boolean trackLogicControl = Core.settings.getInt("cui-logicLineAlpha") > 0;
            Groups.unit.each(unit -> {
                if (trackLogicControl && unit.controller() instanceof LogicAI la) drawLogicControl(unit, la.controller);
            });
        });
    }

    public void drawUnitBars(Unit unit, int style, float alpha, float stroke){
        if(unit.dead()) return;
        if(unit.health == unit.maxHealth) return;
        float x = unit.x(), y= unit.y(), offset = unit.hitSize  * 0.9f, width = unit.hitSize() * -0.9f, hp = Mathf.clamp(unit.health / unit.maxHealth), shield = Mathf.clamp(unit.shield / unit.maxHealth);
        Color colour = new Color().set(unit.team.color).a(alpha), bg = new Color().set(Pal.gray).a(alpha);
        Color shclr = new Color().set(unit.team.color).lerp(Color.black, 0.25f).a(alpha);

        Draw.alpha(alpha);
        Draw.draw(Layer.flyingUnit +2, () ->{
            if(Core.settings.getInt("cui-showUnitTextStyle") > 0){
                int tstyle = Core.settings.getInt("cui-showUnitTextStyle");
                String txt = "[red]";
                if(tstyle == 1){
                    txt  += Mathf.round(hp * 100) + "%[]";
                    if(shield > 0) txt += "\n" + CuiVars.decFor.format(unit.shield);
                }else if(style == 2){
                    txt  += CuiVars.decFor.format(unit.health) + "[]";
                    if(shield > 0) txt += "\n" + CuiVars.decFor.format(unit.shield);
                } else {
                    txt  += (CuiVars.decFor.format(unit.health) + "[][white]/[][pink]"+ Math.round(unit.maxHealth) +"[]");
                    if(shield > 0) txt += "\n" + CuiVars.decFor.format(unit.shield);
                }

                drawLabel(unit.x, unit.y, txt, colour);

            }

            float yShield = y - offset + stroke, yShieldAlt = yShield + (stroke /2.5f);
            float yOff = y - offset;
            switch (style) {
                case 9 -> {//boarder - right
                    if(shield > 0)drawBoardedLine(colour, x + width, yShield, x - (width), yShield,x + offset + ((width * 2) * shield), yShield, x + offset, yShield, stroke, bg);
                    drawBoardedLine(colour, x + width, yOff, x - (width), yOff,x + offset + ((width * 2) * hp), yOff, x + offset, yOff, stroke, bg);
                }
                case 8 -> {//boarder - left
                    if(shield > 0)drawBoardedLine(colour, x + width, yShield, x - (width), yShield,x - offset, yShield, x - offset - ((width * 2) * shield), yShield, stroke, bg);
                    drawBoardedLine(colour, x + width, yOff, x - (width), yOff,x - offset, yOff, x - offset - ((width * 2) * hp), yOff, stroke, bg);
                }
                case 7 -> {//boarder - center
                    if(shield > 0)drawBoardedLine(shclr, x + width, yShieldAlt, x - (width), yShieldAlt, x + (width * shield), yShieldAlt, x - (width * shield), yShieldAlt, stroke, bg);
                    drawBoardedLine(colour, x + width, yOff, x - (width), yOff, x - (width * hp), yOff, x + (width * hp), yOff, stroke, bg);
                }
                case 6 -> {//right
                    drawLine(x + offset, yOff, x + offset + ((width * 2) * hp), yOff, colour, alpha, stroke);
                    if (shield == 0) break;
                    drawLine(x + offset, yShield, x + offset + ((width * 2) * shield), yShield, shclr, alpha, stroke);
                }
                case 5 -> { //left
                    drawLine(x - offset, yOff, x - offset - ((width * 2) * hp), yOff, colour, alpha, stroke);
                    if (shield == 0) break;
                    drawLine(x - offset, yShield, x - offset - ((width * 2) * shield), yShield, shclr, alpha, stroke);
                }
                case 4 -> {//center
                    drawLine(x + width * hp, yOff, x - (width * hp), yOff, colour, alpha, stroke);
                    if (shield == 0) break;
                    drawLine(x + width * shield, yShield, x - (width * shield), yShield, shclr, alpha, stroke);
                }
                case 3 -> {//bg left
                    drawLine(x - width, yOff, x + (width), yOff, bg, alpha, stroke);
                    drawLine(x - offset - ((width * 2) * hp), yOff, x - offset, yOff, colour, alpha, stroke);
                    if (shield == 0) break;
                    drawLine(x - width, yShield, x + (width), yShield, bg, alpha, stroke);
                    drawLine(x - offset - ((width * 2) * shield), yShield, x - offset, yShield, shclr, alpha, stroke);
                }
                case 2 -> {//bg right
                    drawLine(x + width, yOff, x - (width), yOff, bg, alpha, stroke);
                    drawLine(x + offset + ((width * 2) * hp), yOff, x + offset, yOff, colour, alpha, stroke);
                    if (shield == 0) break;
                    drawLine(x + width, yShield, x - (width), yShield, bg, alpha, stroke);
                    drawLine(x + offset + ((width * 2) * shield), yShield, x + offset, yShield, shclr, alpha, stroke);
                }
                case 1 -> { //bg center
                    drawLine(x + width, yOff, x - (width), yOff, bg, alpha, stroke);
                    drawLine(x + width * hp, yOff, x - (width * hp), yOff, colour, alpha, stroke);
                    if (shield == 0) break;
                    drawLine(x + width, yShield, x - (width), yShield, bg, alpha, stroke);
                    drawLine(x + width * shield, yShield, x - (width * shield), yShield, shclr, alpha, stroke);
                }
                default -> {
                }
            }
            //wip
        });
        Draw.reset();
    }

    public  void  drawUnitPointer(){
        for (CuiPointerHelper p : cmdPointerHelper) drawPointer(p.color, p.x(), p.y(), p.unit, cmdPointer);
        cmdPointerHelper.clear();
    }

    public void drawUnitCmds(Unit unit){
        Tmp.v1.set(player.mouseX, player.mouseY());
        boolean draw  = false;

        if(!(unit.isCommandable() && unit.command().hasCommand())) return;
        if(cmdLimit == 1 && unit.within(Tmp.v1, cmdRange)) draw = true;
        if(cmdLimit == 2 && unit.within(Core.camera.position, Math.max(Core.camera.height, Core.camera.width))) draw = true;
        if(cmdLimit == 3) draw = true;

        if(draw){
            Tmp.v2.set(unit.command().targetPos);

            cmdPointerHelper.addUnique(new CuiPointerHelper(Tmp.v2, unit.team.color, unit));

            Draw.draw(Layer.overlayUI+0.01f, () ->{
                Lines.stroke(1, unit.team.color);
                Draw.color(unit.team.color, cmdTrans);

                if(cmdStyle == 1) drawLine(unit.x, unit.y, Tmp.v2.x, Tmp.v2.y, unit.team.color, cmdTrans);
                else if(cmdStyle == 2)Lines.dashLine(unit.x, unit.y, Tmp.v2.x, Tmp.v2.y, Math.round(unit.dst(Tmp.v2.x, Tmp.v2.y) / 2));
                else if(cmdStyle == 3)Drawf.line(unit.team.color, unit.x, unit.y, Tmp.v2.x, Tmp.v2.y);
                Draw.reset();
            });
        }
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

        drawPointer(ply.team().color, finalCursorX, finalCursorY, ply.unit(), style);
        Draw.reset();
        if(Core.settings.getInt("cui-playerDrawNames") == 2 || Core.settings.getInt("cui-playerDrawNames") == 1 && isTracked ){
            drawLabel(finalCursorX, finalCursorY, ply.name, ply.team().color);
            Draw.reset();
        }

    }

    public void drawBindingSelected(float x, float y, Color color){
        drawBindingSelected(x, y, color, -1, -1);
    }

    public void drawBindingSelected(float x, float y, Color color, float sin, float mag){
        float fx = x, fy = y, absin = 1;
        //so it always bind to the block despite the size and where the x-y is
        if(world.tileWorld(x, y).build != null){
            fx = world.tileWorld(x, y).build.x;
            fy = world.tileWorld(x, y).build.y;
        }
        if(sin > -1 || mag > -1) absin = Mathf.absin(sin, mag);
        Drawf.selected(world.tileWorld(fx, fy), Tmp.c1.set(color).a(absin));
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
        drawLine(cx, cy, ux, uy, color, transparency, 1);
    }

    public void drawLine(float cx, float cy, float ux, float uy, Color color, float transparency, float stroke) {
        Lines.stroke(stroke, color);
        Draw.color(color, transparency);
        Lines.line(ux, uy, cx, cy);
        Draw.reset();
    }

    public void drawDirectionalTriangle(float cx, float cy, float rot, float width, float height, Color color, float transparency) {
        Draw.color(color, transparency);
        Drawf.tri(cx, cy, width, height, rot);
        Draw.reset();
    }

    public void drawPointer(Color color, float x, float y, Position unit, int style){
        if(style == 0 )return;
        Draw.draw(Layer.overlayUI+0.02f, () -> {
            Lines.stroke(1, color);
            Draw.color(color, cmdTrans);
            switch (style) {
                case 2 -> Drawf.circles(x, y, 3, color); // Circle
                case 3 -> Drawf.target(x, y, 3, cmdTrans, color); //Target (aka mobile mindustry)
                case 4 -> Fill.circle(x, y, 3); //Foo's style
                case 5 -> drawDirectionalTriangle(x, y, unit.angleTo(x, y), 5, 5, color, cmdTrans);
                case 6 -> drawDirectionalTriangle(x, y, Tmp.v1.set(x, y).angleTo(unit), 5, 5, color, cmdTrans);
                case 7 -> drawBindingSelected(x, y, color);
                case 8 -> drawBindingSelected(x, y, color, 4 , 1);
                default -> Drawf.square(x, y, 2, color); // Square (Inspired from Mindustry Ranked Server's spectator mode )
            }
        });
        Draw.reset();
    }

    public static void drawBoardedLine(Color color, float x, float y, float x2, float y2, float x3, float y3, float stoke, Color bg){
        drawBoardedLine(color, x, y, x2, y2, x3, y3, x, y, stoke, bg);
    }

    public static void drawBoardedLine(Color color, float x, float y, float x2, float y2, float x3, float y3, float x4, float y4, float stoke, Color bg){
        Lines.stroke(stoke * 2.5f);
        Draw.color(bg, color.a);
        Lines.line(x, y, x2, y2);
        Lines.stroke(stoke, color);
        Lines.line(x4, y4, x3, y3);
        Draw.reset();
    }

    public void drawLine(float cx, float cy, float ux, float uy, Color color) {
        drawLine(cx, cy, ux, uy, color, 1);
    }

    public void drawLogicControl(Unit unit, Building processor){
        if(processor == null || unit == null) return;
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
                String alert = "[#" + e.tile.team().color.toString() + "]" + e.tile.team().localized()+ " " + Core.bundle.get("alerts.basic") + "\n[white] (" +e.tile.x + ", "+ e.tile.y + ")";
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
        for (Building b : Groups.build) {
            if (!(b instanceof UnitFactory.UnitFactoryBuild fac)) continue;
            if (fac.currentPlan == -1) continue;

            if (style == 2 || style == 3 || style == 4 || style == 5) {
                float x = fac.x(), y = fac.y(), width = (fac.block.size * 4) * -0.9f, hp = (fac.fraction()),
                        offset = ((fac.block.size * 4) * 0.9f) * (style == 2 || style == 5 ? 1 : -1);
                Draw.alpha(0.5f);
                draw(Layer.flyingUnit + 0.01f, () -> {
                    Drawf.line(Pal.gray, x + width, y - offset, x - (width), y - offset);
                    Drawf.line(fac.team.color, x + width * hp, y - offset, x - (width * hp), y - offset);
                });
            }
            if (style == 2 || style == 3 || style == 6)
                drawLabel(fac.x(), fac.y, Math.round(fac.fraction() * 100) + "%", Color.white, 0f, 1.7f);
            Draw.reset();
        }
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
