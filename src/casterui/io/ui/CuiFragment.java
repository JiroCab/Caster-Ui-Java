package casterui.io.ui;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.UnitType;
import mindustry.ui.Styles;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CuiFragment {
    public Unit heldUnit, clickedEntity, hoveredEntity;
    public Player hoveredPlayer, clickedPlayer;
    public Table playersTable = new Table(), unitTable = new Table(), controlTable = new Table(), subControlTable = new Table(), unitPlayerTable =new Table();
    private final Seq<Player> sortedPlayers = new Seq<>();
    public int
            buttonSize = Core.settings.getInt("cui-buttonSize"),
            tableSize = Core.settings.getInt("cui-unitsPlayerTableSize"),
            updateDelay = Core.settings.getInt("cui-unitsPlayerTableUpdateRate"),
            playerIconSize = Core.settings.getInt("cui-playerIconSize"),
            unitsIconSize = Core.settings.getInt("cui-unitsIconSize");
    float buttonSizeAlt = buttonSize;
    public Boolean unitTableCollapse = true, unitTableCoreUnits = true, unitTablePlayers = true, unitTableCompactPlayers = false, initialized = false;

    public void BuildTables(Group parent){

        if(!Core.settings.getBool("cui-ShowUnitTable") && !Core.settings.getBool("cui-ShowPlayerList") ) return;

        // region Control table
        controlTable.clear();
        controlTable.button(Icon.play, Styles.defaulti, () -> unitTableCollapse = !unitTableCollapse).size(buttonSize);
            if(Core.settings.getBool("cui-ShowUnitTable")) controlTable.button(Icon.admin, Styles.defaulti, () -> unitTableCoreUnits = !unitTableCoreUnits).size(buttonSizeAlt).tooltip("@units-table.button.core-units.tooltip");
            if(Core.settings.getBool("cui-ShowPlayerList")) {
               controlTable.button(Icon.players, Styles.defaulti, () -> unitTablePlayers = !unitTablePlayers).size(buttonSizeAlt).tooltip("@units-table.button.hide-player-list.tooltip");
               controlTable.button(Icon.host, Styles.defaulti, () -> unitTableCompactPlayers = !unitTableCompactPlayers).size(buttonSizeAlt).tooltip("@units-table.button.compact-player-list.tooltip");
            }
        //endregion

        parent.fill(parentCont -> {
            parentCont.name = "cui-table";
            parentCont.bottom().left();
            unitPlayerTable.background(Tex.buttonTrans);
            unitPlayerTable.add(controlTable);
            unitPlayerTable.row();
            unitPlayerTable.collapser(a -> {
                    a.add(playersTable).visible(Core.settings.getBool("cui-ShowPlayerList"));
                    a.row();
                    a.add(unitTable).visible(Core.settings.getBool("cui-ShowUnitTable"));
            }, true, () -> unitTableCollapse);
            parentCont.add(unitPlayerTable);

        });
    }

    public void UpdateTables(){

        if(!Vars.state.isPlaying())return;
        Timer.schedule(() -> {
            if (hoveredEntity != null && !unitPlayerTable.hasMouse()) hoveredEntity = null;

            buttonSizeAlt = unitTableCollapse ? buttonSize : 0.01f;

            //region Units Table
            //**unitTable.clearChildren();
            unitTable.clear();
            heldUnit = null;
            Seq<Unit> allUnits = new Seq<>();
            Groups.unit.copy(allUnits);

            /*Arrays.stream(Team.all).forEach(team -> {
                if (team.data().units.size <= 0)return;
                team.data().units.forEach(allUnits::add);
            });*/

            allUnits.sort(unit -> unit.team.id);
            HashMap<String, Integer> heldCounts = new HashMap<>();

            AtomicInteger icons = new AtomicInteger();
            allUnits.forEach( unit ->{
                if (unit.spawnedByCore && unitTableCoreUnits) return;
                if(heldUnit == null) heldUnit = unit;
                String teamUnits = "cui-"+unit.team.id + "=" +unit.type.name + "~";

                if (heldUnit.type == unit.type && heldUnit.team == unit.team) {
                    heldCounts.put(teamUnits, heldCounts.get(teamUnits) != null ? heldCounts.get(teamUnits) +1 : 1);
                } else {

                    heldUnit = unit;

                }
            });

            heldCounts.forEach((unitString, count) -> {
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
            Log.err(heldCounts.toString());


            //endregion
            //region Players table

            playersTable.clear();
            final int[] plys = {0};
            sortedPlayers.clear();
            Groups.player.copy(sortedPlayers);
            sortedPlayers.sort(Structs.comps(Structs.comparing(Player::team), Structs.comparingBool(p -> !p.admin )));
            sortedPlayers.removeAll( p -> (p.unit() == null && Core.settings.getBool("cui-hideNoUnitPlayers")));

            sortedPlayers.each(player -> {
                if(player == Vars.player) return;
                if(player.unit().isNull() && Core.settings.getBool("cui-hideNoUnitPlayers"))return;

                TextureRegion playerIcon = player.unit().isNull() ? Icon.eye.getRegion() : player.unit().icon();

                Table eachPLayerTable = new Table();
                eachPLayerTable.add(new Image(playerIcon).setScaling(Scaling.bounded)).size(playerIconSize);
                eachPLayerTable.name = player.name();
                eachPLayerTable.touchable = Touchable.enabled;

                eachPLayerTable.tapped(() -> {
                    if (clickedPlayer != player)clickedPlayer = player;
                    else  clickedPlayer = null;
                });
                eachPLayerTable.hovered( ()-> hoveredPlayer = player);
                if (!unitTableCompactPlayers) {
                    Label playerName = new Label(() -> player.name);
                    playerName.clicked(() -> clickedPlayer = player);
                    playerName.hovered(() -> hoveredPlayer = player);
                    eachPLayerTable.add(playerName);
                }
                eachPLayerTable.add();
                playersTable.add(eachPLayerTable);
                plys[0]++;
                if(plys[0] >= Core.settings.getInt("cui-unitsPlayerTableSize") || !unitTableCompactPlayers){ playersTable.row(); }
            });

            //endregion
        }, initialized ? updateDelay : 0);
    }

    public void clearTables(){
        if (clickedPlayer != null) clickedPlayer = null;
        if (heldUnit != null) heldUnit = null;
        if (hoveredEntity != null) hoveredEntity = null;
        if (initialized) initialized = false;


        buttonSize = Core.settings.getInt("cui-buttonSize");
        playerIconSize = Core.settings.getInt("cui-playerIconSize");
        unitsIconSize = Core.settings.getInt("cui-unitsIconSize");
        tableSize = Core.settings.getInt("cui-unitsPlayerTableSize");
        updateDelay = Core.settings.getInt("cui-unitsPlayerTableUpdateRate");
    }

}
