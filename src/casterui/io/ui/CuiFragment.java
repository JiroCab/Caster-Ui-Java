package casterui.io.ui;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Group;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import casterui.CuiVars;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.UnitType;
import mindustry.ui.Styles;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CuiFragment {
    public Table playersTable = new Table(), unitTable = new Table(), controlTable = new Table(), unitPlayerTable =new Table();
    public int
            tableSize = Core.settings.getInt("cui-unitsPlayerTableSize"),
            playerIconSize = Core.settings.getInt("cui-playerIconSize"),
            unitsIconSize = Core.settings.getInt("cui-unitsIconSize");
    float buttonSize;
    public Boolean unitTablePlayers = true, unitTableCompactPlayers = false;

    public void BuildTables(Group parent){

        if(!Core.settings.getBool("cui-ShowUnitTable") && !Core.settings.getBool("cui-ShowPlayerList") ) return;
        
        // region Control table
        controlTable.clear();
        if(Core.settings.getBool("cui-ShowUnitTable")) controlTable.button(Icon.admin, Styles.defaulti, () -> CuiVars.showCoreUnits = !CuiVars.showCoreUnits).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.core-units.tooltip");
        if(Core.settings.getBool("cui-ShowPlayerList")) {
           controlTable.button(Icon.players, Styles.defaulti, () -> unitTablePlayers = !unitTablePlayers).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.hide-player-list.tooltip");
           controlTable.button(Icon.host, Styles.defaulti, () -> unitTableCompactPlayers = !unitTableCompactPlayers).pad(1).width(buttonSize).height(buttonSize).tooltip("@units-table.button.compact-player-list.tooltip");
        }
        //endregion

        parent.fill(parentCont -> {
            parentCont.name = "cui-table";
            parentCont.bottom().left();
            unitPlayerTable.background(Tex.buttonTrans);
            parentCont.clear();
            unitPlayerTable.add(controlTable).row();
            if(Core.settings.getBool("cui-ShowPlayerList")) unitPlayerTable.add(playersTable).left();
            if(Core.settings.getBool("cui-ShowPlayerList") && Core.settings.getBool("cui-ShowUnitTable")) unitPlayerTable.row();
            if(Core.settings.getBool("cui-ShowUnitTable")) unitPlayerTable.add(unitTable).left();
            parentCont.add(unitPlayerTable).visible(() -> CuiVars.unitTableCollapse);
        });
    }

    public void UpdateTables(){

        if (CuiVars.hoveredEntity != null && !unitPlayerTable.hasMouse()) CuiVars.hoveredEntity = null;
        buttonSize = (float) Core.settings.getInt("cui-buttonSize");

        //region Units Table
        unitTable.clear();
        Seq<Unit> allUnits = new Seq<>();
        Groups.unit.copy(allUnits);

        allUnits.sort(unit -> unit.team.id);
        HashMap<String, Integer> heldCounts = new HashMap<>();

        AtomicInteger icons = new AtomicInteger();
        allUnits.forEach( unit ->{
            if (unit.spawnedByCore && CuiVars.showCoreUnits) return;
            if(CuiVars.heldUnit == null) CuiVars.heldUnit = unit;
            String teamUnits = "cui-"+unit.team.id + "=" +unit.type.name + "~";

            if (CuiVars.heldUnit.type == unit.type && CuiVars.heldUnit.team == unit.team) {
                heldCounts.put(teamUnits, heldCounts.get(teamUnits) != null ? heldCounts.get(teamUnits) +1 : 1);
            } else {

                CuiVars.heldUnit = unit;

            }
        });

        heldCounts.forEach((unitString, count) -> {
            //TODO: REDO
            UnitType type = Vars.content.unit(unitString.replaceFirst("[cui-].*?[=]", "").replace("~", ""));
            Team team = Team.get(Integer.parseInt(unitString.replaceFirst("cui-","").replaceFirst("[=].*?[~]","")));
            Image unitIcon = new Image(type.fullIcon);
            unitIcon.setScaling(Scaling.bounded);
            unitTable.add(unitIcon).tooltip(type.localizedName).size(unitsIconSize).get();
            unitTable.add(new Label( () -> "[#" +team.color.toString() + "]" + count + "[white]")).get();
            if (icons.get() >= tableSize){
                unitTable.row();
                icons.set(0);
            } else icons.getAndIncrement();

        });

        playersTable.clearChildren();
        final int[] plys = {0};

        Groups.player.each(player -> {
            if(player == Vars.player) return;
            if(player.unit() == null && Core.settings.getBool("cui-hideNoUnitPlayers"))return;

            Label teamIcon = new Label(() -> player.team().emoji.equals("") ? "[#" + player.team().color + "]" +player.team().id + "[]" : player.team().emoji);
            if (!unitTableCompactPlayers) playersTable.add(teamIcon).with( w -> w.tapped( () -> setTrackPlayer(player)));
            TextureRegion playerIcon = player.unit().isNull() ? Icon.eye.getRegion() : player.unit().icon();

            playersTable.add(new Image(playerIcon).setScaling(Scaling.bounded)).size(playerIconSize).left().with( w -> w.tapped( () -> setTrackPlayer(player)));

            if (!unitTableCompactPlayers) {
                Label playerName = new Label(() -> player.name);
                playerName.tapped( () -> setTrackPlayer(player));
                playersTable.add(playerName);
            }
            playersTable.tapped(() -> {
                setTrackPlayer(player);
            });
            plys[0]++;
            if(plys[0] >= Core.settings.getInt("cui-unitsPlayerTableSize") || !unitTableCompactPlayers){ playersTable.row(); }
        });
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
        tableSize = Core.settings.getInt("cui-unitsPlayerTableSize");
        unitPlayerTable.clear();
    }

}
