package casterui.io.ui;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import casterui.CuiVars;
import casterui.io.ui.dialog.CuiSettingsDialog;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.logic.LAccess;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.units.UnitFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static arc.Core.settings;

public class CuiFragment {
    public Table
            playersTable = new Table(),
            unitTable = new Table(),
            controlTable = new Table(),
            unitPlayerTable =new Table(),
            blockTable = new Table(),
            blockItemTable = new Table(),
            blockLiquidTable = new Table();
    public int
            playerIconSize = Core.settings.getInt("cui-playerIconSize"),
            unitsIconSize = Core.settings.getInt("cui-unitsIconSize");
    float buttonSize;
    public Boolean  unitTableCompactPlayers = false, showBlockTable = false, showTableUnitsPlayer = false;
    public Building mouseBuilding = null;
    public int iconSizes = 25;

    public void BuildTables(Group parent){

        if(!Core.settings.getBool("cui-ShowUnitTable") && !Core.settings.getBool("cui-ShowPlayerList") ) return;

        parent.fill(parentCont -> {
            parentCont.name = "cui-unit-player-table";
            parentCont.bottom().left();
            unitPlayerTable.background(Tex.buttonTrans);
            parentCont.clear();
            unitPlayerTable.add(controlTable).row();
            if(Core.settings.getBool("cui-ShowPlayerList")) unitPlayerTable.add(playersTable).left();
            if(Core.settings.getBool("cui-ShowPlayerList") && Core.settings.getBool("cui-ShowUnitTable")) unitPlayerTable.row();
            if(Core.settings.getBool("cui-ShowUnitTable")) unitPlayerTable.add(unitTable).left();
            parentCont.add(unitPlayerTable).visible(() -> CuiVars.unitTableCollapse && showTableUnitsPlayer);
        });

        if(Core.settings.getBool("cui-ShowBlockInfo")){
            parent.fill(parentCont -> {
                parentCont.name = "cui-block-table";
                parentCont.left();
                blockTable.background(Styles.black3);
                parentCont.add(blockTable).visible(() -> CuiVars.unitTableCollapse).visible(() -> CuiVars.unitTableCollapse && showBlockTable);
            });
        }

    }

    public void StutteredUpdateTables() {
        if (CuiVars.hoveredEntity != null && !unitPlayerTable.hasMouse()) CuiVars.hoveredEntity = null;
        buttonSize = (float) Core.settings.getInt("cui-buttonSize");
        showTableUnitsPlayer = (Core.settings.getBool("cui-ShowPlayerList") || Core.settings.getBool("cui-ShowUnitTable"));

        // region Control table
        controlTable.clear();
        if (Core.settings.getBool("cui-ShowUnitTable"))
            controlTable.button(Icon.admin, Styles.defaulti, () -> CuiVars.showCoreUnits = !CuiVars.showCoreUnits).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.core-units.tooltip");
        if (Core.settings.getBool("cui-ShowPlayerList"))
            controlTable.button(Icon.host, Styles.defaulti, () -> unitTableCompactPlayers = !unitTableCompactPlayers).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.compact-player-list.tooltip");

        //endregion
        //region Units Table
        unitTable.clear();
        if (Core.settings.getBool("cui-ShowUnitTable")) {
            AtomicInteger icons = new AtomicInteger();
            //prevent  doubling of rows
            AtomicBoolean newRow = new AtomicBoolean(false);

            for (int id = 0 ; id < Team.all.length ; id++){
                Team team = Team.get(id);
                if(team.data().units.size >= 1) {
                    if(settings.getBool("cui-separateTeamsUnit") && !newRow.get()){
                        if(!newRow.get())icons.set(0);
                        unitTable.row();
                    }

                    Map<Short, Integer> teamUnits = new HashMap<>();

                    for (Unit u : team.data().units.sort(unit -> unit.type.id)) {
                        if(CuiSettingsDialog.hiddenUnits.contains(u.type) && CuiVars.showCoreUnits ) break;
                        if(!CuiVars.showCoreUnits && CuiSettingsDialog.coreUnitsTypes.contains(u.type))break;
                        if(!CuiVars.showCoreUnits && u.spawnedByCore && settings.getBool("cui-unitFlagCoreUnitHides"))break;

                        teamUnits.merge(u.type.id, 1, Integer::sum);
                    }
                    teamUnits.forEach((u, i ) ->{
                        UnitType unit = Vars.content.unit(u);

                        unitTable.image(new TextureRegion(unit.fullIcon)).tooltip(unit.localizedName).size(unitsIconSize).scaling(Scaling.bounded).get();
                        unitTable.add(new Label(() -> "[#" + team.color.toString() + "]" + i + "[white]")).get();

                        if (icons.get() >= settings.getInt("cui-unitsPlayerTableSize")) {
                            unitTable.row();
                            icons.set(0);
                            newRow.set(true);
                        } else {
                            icons.getAndIncrement();
                            newRow.set(false);
                        }
                    });
                }
            }
        }
        //endregion

        // region Players Table
        if(Core.settings.getBool("cui-ShowPlayerList")) {
            playersTable.clearChildren();
            final int[] plys = {0};

            Groups.player.each(player -> {
                if(player == Vars.player) return;
                if(settings.getBool("cui-hideNoUnitPlayers") && (player.unit() == null || !player.team().data().hasCore()))return;

                Label teamIcon = new Label(() -> player.team().emoji.equals("") ? "[#" + player.team().color + "]" +player.team().id + "[]" : player.team().emoji);
                if (!unitTableCompactPlayers) playersTable.add(teamIcon).with( w -> w.tapped( () -> setTrackPlayer(player)));
                TextureRegion playerIcon = player.unit().icon() == null ? Icon.eye.getRegion() : player.unit().icon();

                playersTable.add(new Image(playerIcon).setScaling(Scaling.bounded)).size(playerIconSize).left().with( w -> w.tapped( () -> setTrackPlayer(player)));

                if (!unitTableCompactPlayers) {
                    Label playerName = new Label(() -> player.name);
                    playerName.tapped( () -> setTrackPlayer(player));
                    playersTable.add(playerName);
                }
                playersTable.tapped(() -> setTrackPlayer(player));
                plys[0]++;
                if(plys[0] >= Core.settings.getInt("cui-unitsPlayerTableSize") || !unitTableCompactPlayers){ playersTable.row(); }
            });
        }
        //endregion
    }

    public void slowUpdateTables(){
        //region Block info slow
        /*Moved here to reduce flickering*/
        blockItemTable.clear();
        blockLiquidTable.clear();
        if (Core.settings.getBool("cui-ShowBlockInfo") && mouseBuilding != null) {
            if (mouseBuilding.items != null && mouseBuilding.items.total() > 0) {
                AtomicInteger itemTypes = new AtomicInteger();
                mouseBuilding.items.each((item, amount) -> {
                    blockItemTable.image(item.uiIcon).size(iconSizes).left();
                    blockItemTable.label(() -> amount + " ");
                    if (itemTypes.get() > 4) {
                        itemTypes.set(0);
                        blockItemTable.row();
                    } else itemTypes.getAndIncrement();
                });
            }
            if (mouseBuilding.liquids != null) {
                AtomicInteger liquidTypes = new AtomicInteger();
                mouseBuilding.liquids.each((liquid, amount) -> {
                    float newliquid = Math.round(amount * 100f) / 100f;
                    blockLiquidTable.image(liquid.uiIcon).size(iconSizes).left();
                    blockLiquidTable.label(() -> newliquid + " ");
                    if (liquidTypes.get() > 4) {
                        liquidTypes.set(0);
                        blockLiquidTable.row();
                    } else liquidTypes.getAndIncrement();
                });
            }
        }
        //endregion
    }

    public void UpdateTables(){
        CuiVars.fastUpdate = !unitPlayerTable.hasMouse();

        //region Block info main
        if (Core.settings.getBool("cui-ShowBlockInfo")) {
            blockTable.clear();
            Vec2 mouse = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
            Tile mouseTile = Vars.world.tileWorld(mouse.x, mouse.y);
            showBlockTable = false;
            CuiVars.drawRally = false;

            mouseBuilding = mouseTile != null ? mouseTile.build : null;
            if (mouseBuilding != null) { //the less cool deltanedas/waisa
                showBlockTable = true;
                String armor = mouseBuilding.block.armor >= 1 ? " [white]("+Math.round(mouseBuilding.block.armor) +")" : "";
                if(mouseBuilding.health > 0 && Core.settings.getBool("cui-ShowBlockHealth")){
                    blockTable.label(()-> Core.bundle.get("cui-block-info.health") + ": [red]"+ Math.round(mouseBuilding.health) +"[white]/[pink]" + Math.round(mouseBuilding.maxHealth) +armor).row();
                }
                if(mouseBuilding.power != null){
                    PowerGraph graphs = mouseBuilding.power.graph;
                    int power = Math.round( graphs.getPowerBalance() * 60f);
                    String sign = power > 0 ? "[stat]+" : "[red]";

                    blockTable.label(() -> Core.bundle.get("cui-block-info.power") + ": "+  sign + power).row();
                    if(mouseBuilding.block instanceof Battery || mouseBuilding.block instanceof PowerNode || mouseBuilding.block instanceof BeamNode) blockTable.label(() -> "[stat]"+ Math.round(mouseBuilding.sense(LAccess.powerNetStored))+ "[white]/[accent]" + Math.round(mouseBuilding.sense(LAccess.powerNetCapacity)));
                }
                if (mouseBuilding.items != null && mouseBuilding.items.total() > 0) blockTable.add(blockItemTable).row();
                if (mouseBuilding.liquids != null) blockTable.add(blockLiquidTable).row();

                if(mouseBuilding.config() instanceof String)blockTable.label(() -> mouseBuilding.config().toString()).row();
                /* why bother being fancy and reading directly from the block if have logic for that */
                if(mouseBuilding.block instanceof UnitFactory && mouseBuilding.senseObject(LAccess.config)!= null){
                    blockTable.table(a-> {
                        a.label(() -> Vars.content.unit(mouseBuilding.senseObject(LAccess.config).toString()).localizedName).pad(1f);
                        a.image(() -> Vars.content.unit(mouseBuilding.senseObject(LAccess.config).toString()).fullIcon).size(iconSizes).pad(1f).row();
                    }).row();

                    if(mouseBuilding.getCommandPosition() != null){
                        blockTable.label(() -> Core.bundle.get("cui-block-info.rally") + ": " + Math.round(mouseBuilding.getCommandPosition().x) + ", "+ Math.round(mouseBuilding.getCommandPosition().y)).row();
                        CuiVars.drawRally = true;
                    }
                }
                if(mouseBuilding.block instanceof Turret)blockTable.label(() -> "[accent]"+  mouseBuilding.sense(LAccess.ammo) + "[white]/[orange]"+ ((Turret) mouseBuilding.block).maxAmmo).row();
                //TODO: heat, block constructors, Payload

            }
        }
        //endregion
    }

    public static void setTrackPlayer(Player player){
        if(CuiVars.clickedPlayer  == null || CuiVars.clickedPlayer  != player) CuiVars.clickedPlayer  = player;
        else CuiVars.clickedPlayer  = null;
    }

    public void clearTables(){
        if (CuiVars.clickedPlayer != null) CuiVars.clickedPlayer = null;
        if (CuiVars.heldUnit != null) CuiVars.heldUnit = null;
        if (CuiVars.hoveredEntity != null) CuiVars.hoveredEntity = null;

        playerIconSize = Core.settings.getInt("cui-playerIconSize");
        unitsIconSize = Core.settings.getInt("cui-unitsIconSize");
        unitPlayerTable.clear();
    }

}
