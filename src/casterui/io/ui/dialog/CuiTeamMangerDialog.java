package casterui.io.ui.dialog;

import arc.Core;
import arc.graphics.g2d.*;
import arc.scene.event.ClickListener;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.util.concurrent.atomic.AtomicInteger;

/*I'm so sorry */
public class CuiTeamMangerDialog extends BaseDialog {
    public Player selectedPlayer = Vars.player, lastPlayer = null;
    public Team selectedTeam = Vars.player.team(), lastTeam = null;
    public int size = 100, teamSize = 40;
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

        rebuildPlayerTable();
        AtomicInteger icons = new AtomicInteger();

        for (int id = 0 ; id < Team.all.length ; id++){
            int finalId = id;
            ImageButton button = teamTable.button(Tex.whiteui, Styles.clearNoneTogglei, 40f, () -> selectedTeam = Team.get(finalId)).tooltip(Team.get(id).emoji.equals("") ? "[#" + Team.get(id).color + "]" + Team.get(id).id + "[]" :  "[white]" +Team.get(id).emoji ).size(teamSize).margin(6f).get();
            button.getImageCell();
            button.getStyle().imageUpColor = Team.get(id).color;

            if (icons.get() >= 7){
                teamTable.row();
                icons.set(0);
            } else icons.getAndIncrement();
        }
        teamTable.row();

    }

    void rebuildPlayerTable(){
        Groups.player.forEach(u -> {
            ClickListener listener = new ClickListener();

            Table iconTable = new Table() {
                @Override
                public void draw() {
                    super.draw();
                    Draw.colorMul(u.team().color, listener.isOver() ? 1.3f : 1f);
                    Draw.alpha(parentAlpha);
                    Lines.stroke(Scl.scl(4f));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
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

        });
        playerTable.center();
    }


    void addOtherButtons() {
        titleTable.row();
        titleTable.add(header).center();
        header.label(() -> lastTeam != null && lastPlayer != null ? Core.bundle.format( "cui-team-manger.last", lastPlayer, lastTeam)  : "@cui-team-manger.no-last").left().pad(15f);
        header.label(() -> Core.bundle.format("cui-team-manger.info", selectedPlayer.name(), selectedTeam.name.equals("") ? "[#" + selectedTeam.color + "]" + selectedPlayer.id + "[]" : "[white]" + selectedTeam.localized() + selectedTeam.emoji )).right().pad(15f);
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

            Log.info(selectedPlayer.name + " is now " + selectedTeam);
        } else Log.err("not a server, can't apply team change");
    }

    public void shouldShow(){
        if(!Vars.state.isMenu() && Vars.player.admin) show();
        else Log.err("Not a server/admin!");
    }
}
