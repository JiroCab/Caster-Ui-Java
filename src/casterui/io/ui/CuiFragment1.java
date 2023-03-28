package casterui.io.ui;

import arc.Core;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.ui.Styles;
import mindustry.world.Tile;

import javax.swing.text.Position;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static arc.Core.bundle;

/*Previous attempt's archive pre commit
* TODO: WHY DO I EXIST AND PLEASE END ME*/
public class CuiFragment1 {
    public Position trackedUnit = null, hoveredUnit = null;
    public Unit heldUnit = null;
    public Table unitCounterTable;
    public int buttonSize = 40, tableSize = 6, updateDelay = 5;
    public Boolean unitTableCollapse = false, unitTableCoreUnits = true, unitTablePlayers = true, unitTableCompactPlayers = false, initialized = false;

    public void BuildBlockInfo(Group parent){
        parent.fill(null,blockInfoCont -> {
            blockInfoCont.name = "cui-block-info";

            blockInfoCont.clearChildren();
            if(!Core.settings.getBool("cui-ShowBlockInfo")) return;
            blockInfoCont.table(blockInfoTable -> {
                Tile mouse = Vars.world.tile(Math.round(Vars.player.mouseX), Math.round(Vars.player.mouseY));

                if(mouse == null)return;

                boolean powered = mouse.block().hasPower;
                boolean storage = mouse.block().hasItems;
                boolean config = mouse.block().configurable;
                int health = mouse.block().health;

                if(storage){
                    blockInfoTable.table(storageTable ->{
                        AtomicInteger i = new AtomicInteger();

                        mouse.build.items.each((item, amount)->{
                            storageTable.image(item.uiIcon).left();
                            storageTable.label(() -> bundle.formatString("{0}", amount)).padLeft(2).left().padRight(4);

                            if(i.incrementAndGet() % 4 == 0) {
                                storageTable.row();
                            }
                        });
                    });
                    blockInfoTable.row();

                }

                if (config){
                    blockInfoTable.table(configTable -> {
                        configTable.label(() -> mouse.block().configurations.toString());
                    });
                    blockInfoTable.row();
                }

                if(powered){
                    blockInfoTable.table(powerTable ->{
                        float storedNetPower =  mouse.build.power.graph.getBatteryStored();
                        float maxNetPower =  mouse.build.power.graph.getTotalBatteryCapacity();
                        float currentNetPower =  mouse.build.power.graph.getPowerBalance();
                        String color = currentNetPower >= 0 ? "[stat]" : "[red]";
                        String sign = currentNetPower > 0 ? "+" : "";

                        powerTable.label(() -> "block-info.power" + ": " + color + sign + Math.round(currentNetPower*60f) +  "[white]");
                        powerTable.row();
                        if (maxNetPower != 0    ) {
                            powerTable.label(() -> Core.bundle.get("block-info.stored") + ": " + Math.round(storedNetPower/maxNetPower*100) + "%");
                        }
                        blockInfoTable.row();

                    });
                }

                if(health > 0){
                    blockInfoTable.table(healthTable ->{
                        String armor;
                        if (mouse.block().armor > 0){
                            armor = " [white](" + mouse.block().armor + ")";
                        } else {
                            armor = "";
                        }
                        blockInfoTable.label(() -> "[red]"+ health + "/" + mouse.block().buildType.get().maxHealth + armor );
                    } );
                    blockInfoTable.row();

                }
            });
        });
    }

    public void BuildUnitPlayerCounter(Group parent){
        boolean unitTableCheck  = Core.settings.getBool("cui-ShowUnitTable");
        boolean playerTableCheck = Core.settings.getBool("cui-ShowPlayerList");


        parent.fill(ucCont -> {
            ucCont.name = "cui-unit-players";
            Timer.schedule(() -> {
                ucCont.clearChildren();
                ucCont.clear();
                hoveredUnit = null;
                if(!unitTableCheck && !playerTableCheck ) return;

                ucCont.center().bottom();

                ucCont.table(Tex.buttonEdge3, table -> {
                    table.clear();
                    table.button(Icon.play, Styles.defaulti, () -> unitTableCollapse = !unitTableCollapse).width(buttonSize).height(buttonSize);
                    if(unitTableCollapse)return;

                    if(unitTableCheck){
                        table.button(Icon.admin, Styles.defaulti, () -> unitTableCoreUnits = !unitTableCoreUnits).width(buttonSize).height(buttonSize);
                    }

                    if(playerTableCheck){
                        table.button(Icon.players, Styles.defaulti, () -> unitTablePlayers = !unitTablePlayers).width(buttonSize).height(buttonSize);
                        table.button(Icon.host, Styles.defaulti, () -> unitTableCompactPlayers = !unitTableCompactPlayers).width(buttonSize).height(buttonSize);
                    }

                    table.row();
                    table.collapser(sub -> {
                        sub.table( contentTable ->{
                            if(playerTableCheck)AddPlayerTable(contentTable);
                            if(playerTableCheck && unitTableCheck) contentTable.row();
                            if (unitTableCheck)AddUnitsTable(contentTable);
                        });
                    }, true, () -> unitTableCollapse);

                });
                initialized = true;
            }, initialized ? updateDelay : 0);
        });
    }

    public void AddUnitsTable(Table table){
        /*Yes this is overly complicated and i have no idea either*/
        HashMap<Team, Unit> top = new HashMap<Team, Unit>();

        Arrays.stream(Team.all).forEach(team -> {
            if (team.data().units.size <= 0)return;

            team.data().units.forEach( unit -> {
                top.put(team, unit);
            });
        });
        AtomicInteger heldCount = new AtomicInteger();
        AtomicInteger icons = new AtomicInteger();
        table.table(unitsSubTable -> {
            top.forEach((team, unit)->{
                if(heldUnit == null)heldUnit = unit;

                if(heldUnit.type != unit.type){
                    unitsSubTable.image(unit.icon()).height(buttonSize).width(buttonSize).tooltip(unit.type.localizedName +" (" + unit.team.localized() +")");
                    unitsSubTable.label(() -> unit.team.color + heldCount.toString() + "[white]");

                    if (icons.get() >= tableSize){
                        unitsSubTable.row();
                        icons.set(0);
                    } else icons.getAndIncrement();

                    heldCount.set(0);
                }else {
                    heldCount.getAndIncrement();
                }
            });
        });

    }
    public void AddPlayerTable(Table table){
        final int[] plys = {0};
        table.table(playerSubTable -> {
            Groups.player.each((player) -> {
                if(player == Vars.player) return;
                plys[0]++;
                table.image(player.unit().icon()).tooltip(unitTableCompactPlayers ? "" : player.name);
                table.label(()->unitTableCompactPlayers ? player.name : "");
                if(plys[0] >= 6 || !unitTableCompactPlayers){ table.row(); }
                playerSubTable.left();
            });
        });

    }

    public void BuildTables(Group parent){
        BuildBlockInfo(parent);
        BuildUnitPlayerCounter(parent);
    }
}
