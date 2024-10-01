package casterui.io.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Scaling;
import casterui.CuiVars;
import casterui.io.ui.dialog.CuiSettingsDialog;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.*;
import mindustry.logic.LAccess;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.units.UnitFactory;

import java.text.DecimalFormat;
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
            blockLiquidTable = new Table(),
            teamItemsTable = new Table();
    public int
            playerIconSize = Core.settings.getInt("cui-playerIconSize"),
            unitsIconSize = Core.settings.getInt("cui-unitsIconSize");
    float buttonSize;
    public Boolean  showBlockTable = false, showTableUnitsPlayer = false;
    public Building mouseBuilding = null;
    public int iconSizes = 25;
    Seq<Drawable> tableStyles = Seq.with(Tex.buttonTrans, Tex.clear, Styles.black3, Tex.inventory, Tex.button, Tex.pane, Styles.black5, Styles.black6, Styles.black8, Styles.black9);
    Seq<Integer> alignSides = Seq.with(Align.bottom, Align.bottomLeft, Align.bottomRight, Align.top, Align.topLeft, Align.topRight, Align.center, Align.left, Align.right);


    public void BuildTables(Group parent){

        if(settings.getBool("cui-ShowUnitTable") || settings.getBool("cui-ShowPlayerList") ) {
            parent.fill(parentCont -> {
                parentCont.name = "cui-unit-player-table";
                parentCont.align(alignSides.get(settings.getInt("cui-PlayerUnitsTableSide")));
                unitPlayerTable.background(tableStyles.get(settings.getInt("cui-playerunitstablestyle")));
                parentCont.clear();
                unitPlayerTable.add(controlTable).row();
                if (settings.getBool("cui-ShowPlayerList")) unitPlayerTable.add(playersTable).left();
                if (settings.getBool("cui-ShowPlayerList") && settings.getBool("cui-ShowUnitTable")) unitPlayerTable.row();
                if (settings.getBool("cui-ShowUnitTable")) unitPlayerTable.add(unitTable).left();
                parentCont.add(unitPlayerTable).visible(() -> CuiVars.unitTableCollapse && showTableUnitsPlayer && CuiVars.globalShow);
            });
        }

        if(settings.getBool("cui-ShowBlockInfo")){
            parent.fill(parentCont -> {
                parentCont.name = "cui-block-table";
                parentCont.align(alignSides.get(settings.getInt("cui-blockinfoSide")));
                blockTable.background(tableStyles.get(settings.getInt("cui-blockinfostyle")));
                parentCont.add(blockTable).visible(() -> CuiVars.unitTableCollapse && showBlockTable  && CuiVars.globalShow);
            });
        }

        if(settings.getBool("cui-ShowTeamItems")){
            parent.fill(parentCont -> {
                parentCont.name = "cui-team-Items";
                parentCont.align(alignSides.get(settings.getInt("cui-TeamItemsSide")));
                blockTable.background(tableStyles.get(settings.getInt("cui-blockinfostyle")));
                parentCont.add(teamItemsTable).visible(() -> CuiVars.unitTableCollapse && CuiVars.globalShow);
            });
        }

    }

    public void StutteredUpdateTables() {
        if (CuiVars.hoveredEntity != null && !unitPlayerTable.hasMouse()) CuiVars.hoveredEntity = null;
        buttonSize = (float) Core.settings.getInt("cui-buttonSize");
        showTableUnitsPlayer = (settings.getBool("cui-ShowPlayerList") || settings.getBool("cui-ShowUnitTable")) && Groups.unit.size() > 0;

        // region Control table
        controlTable.clear();
        if(settings.getBool("cui-playerunitstablecontols")){
            if (settings.getBool("cui-ShowUnitTable")) controlTable.button(Icon.admin, Styles.defaulti, () -> Core.settings.put("cui-unitsTableCoreUnits", !Core.settings.getBool("cui-unitsTableCoreUnits"))).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.core-units.tooltip");
            if (settings.getBool("cui-ShowPlayerList")) controlTable.button(Icon.host, Styles.defaulti, () -> Core.settings.put("cui-playerTableSummarizePlayers", !Core.settings.getBool("cui-playerTableSummarizePlayers"))).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.compact-player-list.tooltip");
        }


        //endregion
        //region Units Table
        unitTable.clear();
        if (settings.getBool("cui-ShowUnitTable")) {
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
                        if(CuiSettingsDialog.hiddenUnits.contains(u.type) && Core.settings.getBool("cui-unitsTableCoreUnits") ) break;
                        if(!Core.settings.getBool("cui-unitsTableCoreUnits") && CuiSettingsDialog.coreUnitsTypes.contains(u.type))break;
                        if(!Core.settings.getBool("cui-unitsTableCoreUnits") && u.spawnedByCore && settings.getBool("cui-unitFlagCoreUnitHides"))break;

                        teamUnits.merge(u.type.id, 1, Integer::sum);
                    }
                    int style = settings.getInt("cui-unitsPlayerTableStyle"), tabSize = settings.getInt("cui-unitsPlayerTableSize");
                    if(Core.settings.getBool("cui-teamtotalunitcount")){
                        int total = 0;
                        for (Map.Entry<Short, Integer> entry : teamUnits.entrySet()) total += entry.getValue();

                        if(total > 0){
                            makeIcon(style, total, team, Icon.units.getRegion(), "@wavemode.counts", true);
                            icons.getAndIncrement();
                            newRow.set(false);
                        }

                    }
                    for (Map.Entry<Short, Integer> entry : teamUnits.entrySet()) {
                        Short u = entry.getKey();
                        Integer i = entry.getValue();
                        UnitType unit = Vars.content.unit(u);

                        makeIcon(style, i, unit, team);

                        if (icons.get() >= tabSize) {
                            unitTable.row();
                            icons.set(0);
                            newRow.set(true);
                        } else {
                            icons.getAndIncrement();
                            newRow.set(false);
                        }
                    }
                }
            }
        }
        //endregion

        // region Players Table
        if(settings.getBool("cui-ShowPlayerList")) {
            playersTable.clearChildren();
            final int[] plys = {0};

            Groups.player.each(player -> {
                if(player == Vars.player) return;
                if(settings.getBool("cui-hideNoUnitPlayers") && (player.unit() == null || !player.team().data().hasCore()))return;

                Label teamIcon = new Label(() -> player.team().emoji.equals("") ? "[#" + player.team().color + "]" +player.team().id + "[]" : player.team().emoji);
                if (!Core.settings.getBool("cui-playerTableSummarizePlayers")) playersTable.add(teamIcon).with( w -> w.tapped( () -> setTrackPlayer(player)));
                TextureRegion playerIcon = player.unit().icon() == null ? Icon.eye.getRegion() : player.unit().icon();

                playersTable.add(new Image(playerIcon).setScaling(Scaling.bounded)).size(playerIconSize).left().with( w -> w.tapped( () -> setTrackPlayer(player)));

                if (!Core.settings.getBool("cui-playerTableSummarizePlayers")) {
                    Label playerName = new Label(() -> player.name);
                    playerName.tapped( () -> setTrackPlayer(player));
                    playersTable.add(playerName);
                }
                playersTable.tapped(() -> setTrackPlayer(player));
                plys[0]++;
                if(plys[0] >= Core.settings.getInt("cui-unitsPlayerTableSize") || !Core.settings.getBool("cui-playerTableSummarizePlayers")){ playersTable.row(); }
            });
        }
        //endregion

        if(settings.getInt("cui-TeamItemsUpdateRate")  == 2) buildTeamItemTable();
    }

    public void slowUpdateTables(){
        //region Block info slow
        /*Moved here to reduce flickering*/
        blockItemTable.clear();
        blockLiquidTable.clear();
        if (settings.getBool("cui-ShowBlockInfo") && mouseBuilding != null) {
            if (mouseBuilding.items != null && mouseBuilding.items.total() > 0) {
                AtomicInteger itemTypes = new AtomicInteger();
                mouseBuilding.items.each((item, amount) -> {
                    blockItemTable.image(item.uiIcon).size(iconSizes).left();
                    blockItemTable.label(() -> (!settings.getBool("cui-BlockInfoShortenItems") ? amount : UI.formatAmount(amount) )+ " ");
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

        if(settings.getInt("cui-TeamItemsUpdateRate")  == 3) buildTeamItemTable();
    }

    public void UpdateTables(){
        CuiVars.fastUpdate = !unitPlayerTable.hasMouse();

        //region Block info main
        if (settings.getBool("cui-ShowBlockInfo")) {
            blockTable.clear();
            Vec2 mouse = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
            DecimalFormat decFor = new DecimalFormat("#.##");
            Tile mouseTile = Vars.world.tileWorld(mouse.x, mouse.y);
            showBlockTable = false;
            CuiVars.drawRally = false;

            mouseBuilding = mouseTile != null ? mouseTile.build : null;
            if (mouseBuilding != null) { //the less cool deltanedas/waisa
                showBlockTable = true;
                String armor = mouseBuilding.block.armor >= 1 ? " [white]("+Math.round(mouseBuilding.block.armor) +")" : "";
                if(mouseBuilding.health > 0 && settings.getBool("cui-ShowBlockHealth")){
                    blockTable.label(()-> Core.bundle.get("cui-block-info.health") + ": [red]"+ Math.round(mouseBuilding.health) +"[white]/[pink]" + Math.round(mouseBuilding.maxHealth) +armor).row();
                }
                if(mouseBuilding.power != null){
                    PowerGraph graphs = mouseBuilding.power.graph;
                    int power = Math.round( graphs.getPowerBalance() * 60f);

                    String sign = power > 0 ? "[stat]+" : "[red]";

                    blockTable.label(() -> Core.bundle.get("cui-block-info.power") + ": "+  sign + decFor.format(power)).row();
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
                if(mouseBuilding.block instanceof Turret)blockTable.label(() -> "[accent]"+  decFor.format(mouseBuilding.sense(LAccess.ammo)) + "[white]/[orange]"+ ((Turret) mouseBuilding.block).maxAmmo).row();
                //TODO: heat, block constructors, Payload

            }
        }
        //endregion

        if(settings.getInt("cui-TeamItemsUpdateRate")  == 1) buildTeamItemTable();
    }

    public void buildTeamItemTable() {
        teamItemsTable.clear();
        if (settings.getBool("cui-ShowTeamItems")) {
            for (Teams.TeamData team : Vars.state.teams.active) {
                if (team.core() == null) continue;
                Table sub = new Table() {
                    @Override
                    public void draw() {
                        Draw.color(team.team.color, team.team.color.a * parentAlpha * (settings.getInt("cui-TeamItemsAlpha") * 0.1f));
                        Fill.rect(x + (width / 2), y + (height / 2), width, height);
                        Draw.reset();
                        super.draw();
                    }
                };
                AtomicInteger itemTypes = new AtomicInteger();
                team.core().items.each((item, amount) -> {
                    sub.image(item.uiIcon).size(iconSizes).left();
                    sub.label(() -> (!settings.getBool("cui-TeamItemsShortenItems") ? amount : UI.formatAmount(amount)) + " ");
                    if (itemTypes.get() > 4) {
                        itemTypes.set(0);
                        sub.row();
                    } else itemTypes.getAndIncrement();
                });
                teamItemsTable.add(sub).growX().row();
            }
        }
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


    public void makeIcon(int style, int i, UnitType unit, Team team){
        makeIcon(style, i, team, unit.uiIcon, unit.name, false);
    }

    public void makeIcon(int style, int i, Team team, TextureRegion icon, String name, boolean color){
        var img = new Image(new TextureRegion(icon));
        float fsize = color ? unitsIconSize * 0.85f:  unitsIconSize;
        if(color)img.setColor(team.color);
        if(style == 1){
            //TODO: anything above 1k is hard to see
            Table countTable = new Table(t -> t.center().add(new Label(() -> "[#" + team.color.toString() + "]" + i + "[white]")).style(Styles.outlineLabel).scaling(Scaling.bounded).color(new Color(1, 1, 1, 0.85f)));
            countTable.setColor(new Color(1, 1, 1, 0.85f));
            unitTable.stack(
                    new Table(t -> t.add(img).scaling(Scaling.bounded).size(iconSizes)),
                    countTable
            ).tooltip(name).size(fsize).scaling(Scaling.bounded).get();
        }else {
            unitTable.add(img).tooltip(name).size(fsize).scaling(Scaling.bounded).get();
            unitTable.add(new Label(() -> "[#" + team.color.toString() + "]" + i + "[white]")).style(Styles.outlineLabel).get();
        }
    }

}
