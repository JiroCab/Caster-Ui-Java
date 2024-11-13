package casterui.io.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.style.Drawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import casterui.CuiVars;
import casterui.io.ui.dialog.CuiSettingsDialog;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.*;
import mindustry.logic.LAccess;
import mindustry.type.Category;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.units.UnitFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static arc.Core.settings;
import static casterui.CuiVars.*;

public class CuiFragment {
    public Table
            playersTable = new Table(),
            unitTable = new Table(),
            controlTable = new Table(),
            unitPlayerTable =new Table(),
            blockTable = new Table(),
            blockItemTable = new Table(),
            blockLiquidTable = new Table(),
            teamItemsTable = new Table(),
            dominationTable = new Table()
    ;
    public int
            playerIconSize = settings.getInt("cui-playerIconSize"),
            unitsIconSize = settings.getInt("cui-unitsIconSize");
    float buttonSize;
    public Boolean  showBlockTable = false, showTableUnitsPlayer = false;
    public Building mouseBuilding = null;
    public int iconSizes = 25;
    Seq<Drawable> tableStyles = Seq.with(Tex.buttonTrans, Tex.clear, Styles.black3, Tex.inventory, Tex.button, Tex.pane, Styles.black5, Styles.black6, Styles.black8, Styles.black9);
    Seq<Integer> alignSides = Seq.with(Align.bottom, Align.bottomLeft, Align.bottomRight, Align.top, Align.topLeft, Align.topRight, Align.center, Align.left, Align.right);
    public int[][] blockCats = new int[Team.all.length][Category.all.length + 4];
    public int worldBlocks;
    public String[] dominationIconList = {Iconc.host + "", Iconc.turret + "", Iconc.production + "", Iconc.distribution + "", Iconc.liquid + "", Iconc.power + "", Iconc.defense + "", Iconc.crafting + "", Iconc.units + "", Iconc.effect + "", Iconc.logic + "", Iconc.home + "",Iconc.map + "", Iconc.list + ""};


    public void BuildTables(Group parent){

        if(showCountersUnits || showCountersPlayers ) {
            parent.fill(parentCont -> {
                parentCont.name = "cui-unit-player-table";
                marginHandler(parentCont, settings.getInt("cui-playerunitstables-x"), settings.getInt("cui-playerunitstables-y"), settings.getBool("cui-playerunitstables-x-abs"), settings.getBool("cui-playerunitstables-y-abs"));
                parentCont.align(alignSides.get(settings.getInt("cui-PlayerUnitsTableSide")));
                unitPlayerTable.background(tableStyles.get(settings.getInt("cui-playerunitstablestyle")));
                parentCont.clear();
                unitPlayerTable.add(controlTable).row();
                if (showCountersPlayers) unitPlayerTable.add(playersTable).left();
                if (showCountersPlayers && showCountersUnits) unitPlayerTable.row();
                if (showCountersUnits) unitPlayerTable.add(unitTable).left();
                parentCont.add(unitPlayerTable).visible(() -> globalHidden && showTableUnitsPlayer && globalShow);
            });
        }

        if(showBlockInfo){
            parent.fill(parentCont -> {
                parentCont.name = "cui-block-table";
                parentCont.align(alignSides.get(settings.getInt("cui-blockinfoSide")));
                marginHandler(parentCont, settings.getInt("cui-blockinfo-x"), settings.getInt("cui-blockinfo-y"), settings.getBool("cui-blockinfo-x-abs"), settings.getBool("cui-blockinfo-y-abs"));
                blockTable.background(tableStyles.get(settings.getInt("cui-blockinfostyle")));
                parentCont.add(blockTable).visible(() -> globalHidden && showBlockTable  && globalShow);
            });
        }

        if(showTeamItems){
            parent.fill(parentCont -> {
                parentCont.name = "cui-team-Items";
                parentCont.align(alignSides.get(settings.getInt("cui-TeamItemsSide")));
                marginHandler(parentCont, settings.getInt("cui-TeamItems-x"), settings.getInt("cui-TeamItems-y"), settings.getBool("cui-TeamItems-x-abs"), settings.getBool("cui-TeamItems-y-abs"));
                blockTable.background(tableStyles.get(settings.getInt("cui-blockinfostyle")));
                parentCont.add(teamItemsTable).visible(() -> globalHidden && globalShow);
            });
        }

        if(showDomination){
            parent.fill(parentCont -> {
                parentCont.name = "cui-domination-table";
                parentCont.align(alignSides.get(settings.getInt("cui-domination-side")));
                marginHandler(parentCont, settings.getInt("cui-domination-x"), settings.getInt("cui-domination-y"), settings.getBool("cui-domination-x-abs"), settings.getBool("cui-domination-y-abs"));
                dominationTable.background(tableStyles.get(settings.getInt("cui-blockinfostyle")));
                parentCont.add(dominationTable).visible(() -> globalShow && globalHidden);
            });
        }

    }

    public void StutteredUpdateTables() {
        if (hoveredEntity != null && !unitPlayerTable.hasMouse()) hoveredEntity = null;
        buttonSize = (float) settings.getInt("cui-buttonSize");
        showTableUnitsPlayer = (showCountersPlayers || showCountersUnits) && !Groups.unit.isEmpty();

        // region Control table
        controlTable.clear();
        if(showCountersButton){
            if (showCountersUnits) controlTable.button(Icon.admin, Styles.defaulti, () -> settings.put("cui-unitsTableCoreUnits", !settings.getBool("cui-unitsTableCoreUnits"))).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.core-units.tooltip");
            if (showCountersPlayers) controlTable.button(Icon.host, Styles.defaulti, () -> settings.put("cui-playerTableSummarizePlayers", !settings.getBool("cui-playerTableSummarizePlayers"))).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.compact-player-list.tooltip");
        }


        //endregion
        //region Units Table
        unitTable.clear();
        if (showCountersUnits) {
            int[] icons = {0};
            //prevent  doubling of rows
            boolean[] newRow = {false};

            for (int id = 0 ; id < Team.all.length ; id++){
                Team team = Team.get(id);
                if(team.data().units.size >= 1) {
                    if(countersSeparateTeams && !newRow[0]){
                        icons[0] = 0;
                        unitTable.row();
                    }

                    Map<Short, Integer> teamUnits = new HashMap<>();

                    for (Unit u : team.data().units.sort(unit -> unit.type.id)) {
                        if(CuiSettingsDialog.hiddenUnits.contains(u.type) && countersCoreUnits) break;
                        if(!settings.getBool("cui-unitsTableCoreUnits") && CuiSettingsDialog.coreUnitsTypes.contains(u.type))break;
                        if(!settings.getBool("cui-unitsTableCoreUnits") && u.spawnedByCore && countersCoreFlagged)break;

                        teamUnits.merge(u.type.id, 1, Integer::sum);
                    }
                    int style = settings.getInt("cui-unitsPlayerTableStyle"), tabSize = settings.getInt("cui-unitsPlayerTableSize");
                    if(countersTotals){
                        int total = 0;
                        for (Map.Entry<Short, Integer> entry : teamUnits.entrySet()) total += entry.getValue();

                        if(total > 0){
                            makeIcon(style, total, team, Icon.units.getRegion(), "@wavemode.counts", true);
                            icons[0]++;
                            newRow[0] = false;
                        }

                    }
                    for (Map.Entry<Short, Integer> entry : teamUnits.entrySet()) {
                        Short u = entry.getKey();
                        Integer i = entry.getValue();
                        UnitType unit = Vars.content.unit(u);

                        makeIcon(style, i, unit, team);

                        if (icons[0] >= tabSize) {
                            unitTable.row();
                            icons[0] = 0;
                            newRow[0] = true;
                        } else {
                            icons[0]++;
                            newRow[0] = false;
                        }
                    }
                }
            }
        }
        //endregion

        // region Players Table
        if(showCountersPlayers) {
            playersTable.clearChildren();
            final int[] plys = {0};

            Groups.player.each(player -> {
                if(player == Vars.player) return;
                if(settings.getBool("cui-hideNoUnitPlayers") && (player.unit() == null || !player.team().data().hasCore()))return;

                Label teamIcon = new Label(() -> player.team().emoji.equals("") ? "[#" + player.team().color + "]" +player.team().id + "[]" : player.team().emoji);
                if (!settings.getBool("cui-playerTableSummarizePlayers")) playersTable.add(teamIcon).with(w -> w.tapped( () -> setTrackPlayer(player)));
                TextureRegion playerIcon = player.unit().icon() == null ? Icon.eye.getRegion() : player.unit().icon();

                playersTable.add(new Image(playerIcon).setScaling(Scaling.bounded)).size(playerIconSize).left().with( w -> w.tapped( () -> setTrackPlayer(player)));

                if (!Core.settings.getBool("cui-playerTableSummarizePlayers")) {
                    Label playerName = new Label(() -> player.name);
                    playerName.tapped( () -> setTrackPlayer(player));
                    playersTable.add(playerName);
                }
                playersTable.tapped(() -> setTrackPlayer(player));
                plys[0]++;
                if(plys[0] >= settings.getInt("cui-unitsPlayerTableSize") || !settings.getBool("cui-playerTableSummarizePlayers")){ playersTable.row(); }
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
        if (showBlockInfo && mouseBuilding != null) {
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
        if(showDomination){
            Vars.state.teams.present.each(data -> {
                if(hiddenTeams[data.team.id]) return;
                blockCats[data.team.id][11] = data.cores.size;
                blockCats[data.team.id][12] = data.buildings.size;
                worldBlocks += data.buildings.size;

                data.buildings.each(b -> {
                    if(b.block.isHidden()) return;

                    blockCats[b.team.id][0]++;
                    switch(b.block.category){
                        case turret -> blockCats[b.team.id][1]++;
                        case production -> blockCats[b.team.id][2]++;
                        case distribution -> blockCats[b.team.id][3]++;
                        case liquid -> blockCats[b.team.id][4]++;
                        case power -> blockCats[b.team.id][5]++;
                        case defense -> blockCats[b.team.id][6]++;
                        case crafting -> blockCats[b.team.id][7]++;
                        case units -> blockCats[b.team.id][8]++;
                        case effect -> blockCats[b.team.id][9]++;
                        case logic -> blockCats[b.team.id][10]++;
                    }
                });
            });
            
            buildDominationTable();
        }

        if(settings.getInt("cui-TeamItemsUpdateRate")  == 3) buildTeamItemTable();
    }

    public void UpdateTables(){
        fastUpdate = !unitPlayerTable.hasMouse();

        //region Block info main
        if (showBlockInfo) {
            blockTable.clear();
            Vec2 mouse = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
            DecimalFormat decFor = new DecimalFormat("#.##");
            Tile mouseTile = Vars.world.tileWorld(mouse.x, mouse.y);
            showBlockTable = false;
            drawRally = false;

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
                        drawRally = true;
                    }
                }
                if(mouseBuilding.block instanceof Turret)blockTable.label(() -> "[accent]"+  decFor.format(mouseBuilding.sense(LAccess.ammo)) + "[white]/[orange]"+ ((Turret) mouseBuilding.block).maxAmmo).row();
                if(settings.getBool("cui-ShowBlockHealth") && mouseBuilding.lastAccessed != null){
                    blockTable.table(a-> a.label(() -> mouseBuilding.lastAccessed).pad(1f));
                }
                //TODO: heat, block constructors, Payload

            }
        }
        //endregion

        if(settings.getInt("cui-TeamItemsUpdateRate")  == 1) buildTeamItemTable();
    }

    public Table colouredTable (Color colour, float alpha){
        return new Table() {
            @Override
            public void draw() {
                Draw.color(colour, alpha * parentAlpha);
                Fill.rect(x + (width / 2), y + (height / 2), width, height);
                Draw.reset();
                super.draw();
            }
        };
    }

    public void buildTeamItemTable() {
        teamItemsTable.clear();

        if (settings.getBool("cui-ShowTeamItems")) {
            for (Teams.TeamData team : Vars.state.teams.active) {
                if (team.core() == null) continue;
                Table sub = colouredTable(team.team.color, team.team.color.a * (settings.getInt("cui-TeamItemsAlpha") * 0.1f));
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


    public void buildDominationTable(){
        dominationTable.clear();
        CuiVars.updateSettings(false); // array only setting update on each rebuild

        int trans = settings.getInt("cui-domination-trans");

        float[] size ={35, 20};

        Seq<Label> equlize = new Seq<>(), teamsizes = new Seq<>();

        Table iconTab = new Table();
        iconTab.defaults().pad(5f).grow().minWidth(size[0]).minHeight(size[1]).align(Align.center).scaling(Scaling.fill);
        if(dominationIcons)iconTab.label(() -> " ");

        for(int i = 0; i < dominationIconList.length; i++) {
            if (!dominationSettings[i]) continue;
            if(!dominationVertical) iconTab.row();

            Label rawTxt = new Label(dominationIconList[i]);
            rawTxt.setAlignment(Align.center);

            if(rawTxt.getMinHeight() > size[1]) size[1] = rawTxt.getMinHeight();
            if(rawTxt.getMinWidth() > size[0]) size[0] = rawTxt.getMaxWidth();

            equlize.add(rawTxt);
            iconTab.add(rawTxt);
        }
        dominationTable.add(iconTab).grow();
        if(dominationVertical)dominationTable.row();

        for (int t = 0; t < (Team.all.length); t++) {
            if(blockCats[t][0] == 0) continue;
            Table tab = dominationColoured ? colouredTable(Team.get(t).color, trans * 0.1f) : new Table();
            tab.defaults().pad(5f).grow().minWidth(size[0]).minHeight(size[1]).align(Align.center).scaling(Scaling.fill);

            float[] tsize ={35, 20};
            if(dominationIcons){
                Label team = new Label(Team.get(t).emoji.equals("") ? "[#" + Team.get(t).color + "]#"+ Team.get(t).id + "[]" :  "[white]" +Team.get(t).emoji );
                team.setStyle(Styles.outlineLabel);
                team.setAlignment(Align.center);
                tab.add(team);

                teamsizes.add(team);
                if(team.getMinHeight() > tsize[1]) tsize[1] = team.getMinHeight();
                if(team.getMinWidth() > tsize[0]) tsize[0] = team.getMinWidth();
            }

            tab.defaults().minHeight(tsize[1]).minWidth(tsize[0]);
            
            for(int i = 0; i < dominationIconList.length; i++) {
                if (!dominationSettings[i]) continue;
                //if(!dominationSettings[i]) continue;
                if(!dominationVertical) tab.row();

                String cnt = blockCats[t][i] + "";

                if(i == 12){
                    double per = ((double) blockCats[t][12] /worldBlocks) * 100f;
                    cnt = (per >= 10 ? Math.round(per)  : decForMini.format(per)) + "%";
                }
                Label rawTxt = new Label(cnt);
                rawTxt.setAlignment(Align.center);

                if(rawTxt.getMinHeight() > size[1]) size[1] = rawTxt.getMinHeight();
                if(rawTxt.getMinWidth() > size[0]) size[0] = rawTxt.getMaxWidth();
                equlize.add(rawTxt);
                tab.add(rawTxt);
            }


            dominationTable.add(tab).grow();
            if(dominationVertical)dominationTable.row();

            Arrays.fill(blockCats[t], 0);
        }

        for (Label l : equlize) l.setSize(size[0], size[1]);
        for (Label t : teamsizes) if (dominationVertical) t.setWidth(size[0]);
        worldBlocks = 0;

    }

    public static void setTrackPlayer(Player player){
        if(clickedPlayer  == null || clickedPlayer  != player) clickedPlayer  = player;
        else clickedPlayer  = null;
    }

    public void clearTables(){
        if (clickedPlayer != null) clickedPlayer = null;
        if (heldUnit != null) heldUnit = null;
        if (hoveredEntity != null) hoveredEntity = null;

        playerIconSize = settings.getInt("cui-playerIconSize");
        unitsIconSize = settings.getInt("cui-unitsIconSize");
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
            img.setSize(iconSizes);
            countTable.setColor(new Color(1, 1, 1, 0.85f));
            unitTable.stack(
                    img.setScaling(Scaling.bounded),
                    //new Table(t -> t.add(img).scaling(Scaling.bounded).size(iconSizes)),
                    countTable
            ).tooltip(name).size(fsize).scaling(Scaling.bounded).get();
        }else {
            unitTable.add(img).tooltip(name).size(fsize).scaling(Scaling.bounded).get();
            unitTable.add(new Label(() -> "[#" + team.color.toString() + "]" + i + "[white]")).style(Styles.outlineLabel).get();
        }
    }

    public void marginHandler(Table tab, int x, int y, boolean xPer, boolean yPer){
        float fx = x, fy = y;
        int xp = Math.round(Mathf.clamp(fx, -1, 1)),
             yp = Math.round(Mathf.clamp(fy, -1, 1));


        //todo: make this work
        if(xPer) fx = Mathf.lerp( Core.graphics.getWidth(), 0,  Math.abs((x -100)/100)) * xp;
        if(yPer) fy = Mathf.lerp( Core.graphics.getHeight(), 0,  Math.abs((y -100)/100)) * yp;

        tab.moveBy(fx, fy);
    }
}
