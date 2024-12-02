package casterui.io.ui.dialog;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import casterui.*;
import casterui.io.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.util.concurrent.atomic.*;

/*I'm so sorry */
public class CuiTeamMangerDialog extends BaseDialog {
    public Player selectedPlayer = Vars.player, lastPlayer = null;
    public Team selectedTeam = Vars.player.team(), lastTeam = null;
    public int size = 100, teamSize = 50;
    public Cell<ScrollPane> playerPane, teamPane;
    public Table playerTable = new Table(), teamTable = new Table(), header = new Table();
    public boolean clearUnit = true;

    public CuiTeamMangerDialog(){
        super("@cui-team-manger");
        shouldPause = true;

        addCloseButton();
        addOtherButtons();
        shown(this::setup);
        onResize(this::setup);
        mapListener(() -> {});

    }

    void setup() {
        cont.top();
        cont.clear();
        cont.defaults();

        cont.table(a -> playerPane = a.pane(playerTable).size(a.getWidth(), a.getWidth()).scrollX(false).growX());
        cont.table(a -> teamPane = a.pane(teamTable).size(a.getWidth(), a.getWidth()).scrollX(false).growX());

        playerTable.clear();
        playerTable.defaults();
        teamTable.clear();
        teamTable.defaults();
        header.clear();

        rebuildPlayerTable();
        AtomicInteger icons = new AtomicInteger();

        if (Vars.net.server() || !Vars.net.active() || Vars.player.admin) {
            int max = (Vars.mobile || Vars.testMobile) ? 4 : 7;
            for (int id = 0 ; id < Team.all.length ; id++){
                int finalId = id;

                ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
                button.clicked(() -> selectedTeam = Team.get(finalId));
                button.resizeImage(teamSize);
                button.getImageCell();
                button.getStyle().imageUpColor = Team.get(id).color;

                Label lab = new Label(Team.get(id).emoji.isEmpty() ? finalId + "":  "[white]" +Team.get(id).emoji);

                lab.touchable(() -> Touchable.disabled);
                lab.setAlignment(Align.center);
                teamTable.stack(
                    button,
                    lab
                ).size(teamSize).margin(5f).grow().get();

                if (icons.get() >= max){
                    teamTable.row();
                    icons.set(0);
                } else icons.getAndIncrement();
            }

            header.label(() -> lastTeam != null && lastPlayer != null ? Core.bundle.format( "cui-team-manger.last", lastPlayer.name, lastTeam)  : "@cui-team-manger.no-last").left().pad(15f);
            header.label(() -> Core.bundle.format("cui-team-manger.info", selectedPlayer.name(), selectedTeam.name.equals("") ? "[#" + selectedTeam.color + "]" + selectedPlayer.id + "[]" : "[white]" + selectedTeam.localized() + selectedTeam.emoji )).right().pad(15f);
        } else header.label( () -> Core.bundle.format("cui-team-manger.tip", (selectedTeam != null ?  selectedPlayer.name() :  Vars.player.name), CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.spectate_next_player).key, CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.spectate_previous_player).key));
        teamTable.row();


    }

    void rebuildPlayerTable(){
        for (Player u : Groups.player) {
            ClickListener listener = new ClickListener();

            Table iconTable = new Table() {
                @Override
                public void draw() {
                    Draw.colorMul(u.team().color, listener.isOver() ? 1.3f : 1f);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    if (u != selectedPlayer) Lines.rect(x, y, width, height);
                    else Fill.rect(x + (width / 2), y + (height / 2), width, height);
                    Draw.reset();

                    super.draw();
                }
            };
            iconTable.clear();
            iconTable.addListener(listener);
            iconTable.margin(8);
            TextureRegion plyIcon = u.icon() != null ? u.icon() : Icon.eye.getRegion();
            iconTable.add(new Image(plyIcon).setScaling(Scaling.bounded));
            iconTable.name = u.name();
            iconTable.touchable = Touchable.enabled;

            iconTable.tapped(() -> selectedPlayer = u);
            playerTable.add(iconTable).size(size);
            playerTable.labelWrap(u.name).style(Styles.outlineLabel).size(size).width(170f).pad(10).with(w -> w.tapped(() -> selectedPlayer = u)).row();

        }
        playerTable.center();
    }

    public void mapListener(Runnable callback){
        keyDown(key -> {
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_1).key) mapPlayer( selectedPlayer,  1);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_2).key) mapPlayer( selectedPlayer,  2);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_3).key) mapPlayer( selectedPlayer,  3);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_4).key) mapPlayer( selectedPlayer,  4);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_5).key) mapPlayer( selectedPlayer,  5);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_6).key) mapPlayer( selectedPlayer,  6);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_7).key) mapPlayer( selectedPlayer,  7);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_8).key) mapPlayer( selectedPlayer,  8);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_9).key) mapPlayer( selectedPlayer,  9);
            if(key == CuiVars.rebindDialog.cuiKeyBinds.get(CuiBinding.map_player_10).key) mapPlayer( selectedPlayer,  9);

            callback.run();
        });
    }

    void mapPlayer(Player ply, Integer number){
        CuiVars.mappedPlayers.put(number, ply);
    }

    void addOtherButtons() {
        titleTable.row();
        titleTable.add(header).center();
        rebuildOtherButtons();
    }

    void rebuildOtherButtons() {
        buttons.button("@cui-apply", Icon.down, this::applyChanges).size(210f, 64f);
        buttons.button(clearUnit ? "@cui-team-manger.remove" : "@cui-team-manger.keep", clearUnit ? Icon.pause : Icon.units, () -> {
            clearUnit = !clearUnit;
            rebuildButtons();
        } ).size(210f, 64f);
    }

    void rebuildButtons(){
        buttons.clear();
        addCloseButton();
        rebuildOtherButtons();
    }

    public  void applyChanges(){
        if(Vars.net.server() || !Vars.net.active()){
            if(clearUnit) Call.unitClear(selectedPlayer);
            selectedPlayer.team(selectedTeam);
            if(clearUnit) Call.unitClear(selectedPlayer); //double clear to remove the unit then respawn at the proper core
            lastTeam = selectedTeam;
            lastPlayer = selectedPlayer;

            Log.info(selectedPlayer.name + " is now " + selectedTeam + " (Server change)");
        } else if (Vars.player.admin) {
            Call.adminRequest(selectedPlayer, Packets.AdminAction.switchTeam, selectedTeam);

            Log.info(selectedPlayer.name + " is now " + selectedTeam + " (Admin command)");
        } else {
            Log.err("not a server or admin, can't apply team change");

        }
    }
}
