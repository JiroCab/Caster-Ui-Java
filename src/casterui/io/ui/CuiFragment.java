package casterui.io.ui;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Group;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Timer;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.ui.Styles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CuiFragment {
    public Unit heldUnit, hoveredPlayer, clickedPlayer, heldHoveredEntity;;
    public Table playerTable = new Table(), unitTable = new Table(), controlTable = new Table();
    public int
            buttonSize = Core.settings.getInt("cui-buttonSize"),
            tableSize = Core.settings.getInt("cui-unitsPlayerTableSize"),
            updateDelay = Core.settings.getInt("cui-unitsPlayerTableUpdateRate");
    public Boolean
            unitTableCollapse = false, unitTableCoreUnits = true, unitTablePlayers = true, unitTableCompactPlayers = false, initialized = false,
            unitTableCheck  = Core.settings.getBool("cui-ShowUnitTable"),
            playerTableCheck = Core.settings.getBool("cui-ShowPlayerList")
    ;

    public void BuildTables(Group parent){

        if(!unitTableCheck && !playerTableCheck ) return;

        parent.fill(parentCont -> {
            // region Control table
            controlTable.clear();
            controlTable.button(Icon.play, Styles.defaulti, () -> unitTableCollapse = !unitTableCollapse).width(buttonSize).height(buttonSize);
            controlTable.collapser(subControl -> {
                if(!unitTableCollapse){
                    if(unitTableCheck) controlTable.button(Icon.admin, Styles.defaulti, () -> unitTableCoreUnits = !unitTableCoreUnits).width(buttonSize).height(buttonSize);
                    if(playerTableCheck){
                        controlTable.button(Icon.players, Styles.defaulti, () -> unitTablePlayers = !unitTablePlayers).width(buttonSize).height(buttonSize);
                        controlTable.button(Icon.host, Styles.defaulti, () -> unitTableCompactPlayers = !unitTableCompactPlayers).width(buttonSize).height(buttonSize);
                    }
                }
            }, true, () -> unitTableCollapse);

            //endregion

            parentCont.name = "cui-table";
            parentCont.bottom().left();
            parentCont.table(Tex.buttonEdge2, parentTable -> {
                parentTable.add(controlTable);
                parentTable.row();
                parentTable.collapser(a -> {
                        a.add(playerTable).visible(playerTableCheck);
                        a.row();
                        a.add(unitTable).visible(unitTableCheck);
                }, true, () -> unitTableCollapse);
            });
        });
    }

    public void UpdateTables(){
        Timer.schedule(() -> {
            //region Units Table
            unitTable.clear();
            /*Yes this is overly complicated and I have no idea either*/
            HashMap<Team, Unit> top = new HashMap<>();
            Arrays.stream(Team.all).forEach(team -> {
                if (team.data().units.size <= 0)return;

                team.data().units.forEach( unit -> top.put(team, unit));
            });
            AtomicInteger heldCount = new AtomicInteger();
            AtomicInteger icons = new AtomicInteger();
            unitTable.table(unitsSubTable -> top.forEach((team, unit)->{
                if(heldUnit == null)heldUnit = unit;

                if(heldUnit.type != unit.type){
                    unitsSubTable.image(unit.icon()).height(buttonSize).width(buttonSize).tooltip(unit.type.localizedName +" (" + unit.team.localized() +")");
                    unitsSubTable.label(() -> "[#" +unit.team.color.toString() + "]" + heldCount + "[white]");

                    if (icons.get() >= tableSize){
                        unitsSubTable.row();
                        icons.set(0);
                    } else icons.getAndIncrement();

                    heldCount.set(1);
                }else {
                    heldCount.getAndIncrement();
                }
            }));
            //endregion
            //region Players table
            playerTable.clear();
            final int[] plys = {0};
            Groups.player.each((player) -> {
                //if(player == Vars.player) return;

                plys[0]++;
                if(player.unit().isNull() && Core.settings.getBool("cui-hideNoUnitPlayers"))return;

                TextureRegion playerIcon = player.unit().isNull() ? Icon.eye.getRegion() : player.unit().icon();
                ImageButton playerButton = new ImageButton(playerIcon , Styles.emptyi);
                playerButton.hovered(() ->  hoveredPlayer = player.unit());
                playerButton.clicked(() -> clickedPlayer = player.unit());
                playerTable.add(playerButton);

                playerTable.label(()->unitTableCompactPlayers ? player.name : "");
                if(plys[0] >= 6 || !unitTableCompactPlayers){ playerTable.row(); }
            });

            //endregion
        }, initialized ? updateDelay : 0);
    }


}
