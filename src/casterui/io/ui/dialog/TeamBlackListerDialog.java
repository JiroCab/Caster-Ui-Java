package casterui.io.ui.dialog;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import casterui.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

public class TeamBlackListerDialog extends BaseDialog{
    public Cell<ScrollPane> selectedPane, unselectedPane;
    public int teamSize = 50;
    public Table selectedTable = new Table(), unselectedTable = new Table(), header = new Table();
    public int sort = 0;
    public String sortTxt = "";


    public TeamBlackListerDialog(){
        super("@cui-teamblacklister");
        rebuildOtherButtons();
        shown(this::setup);
        onResize(this::setup);
        setHideAction(() -> {
            StringBuilder out = new StringBuilder();
            boolean first = true;
            for (int id = 0; id < Team.all.length ; id++){
                if(CuiVars.hiddenTeams[id] ){
                    if(first) first = false;
                    else out.append(",");
                    out.append(id);
                }
            }

            Core.settings.put("cui-hiddenTeamsList", out.toString());
            CuiVars.updateHiddenTeams();
            return Actions.fadeOut(0.2F, Interp.fade);
        });
    }


    void setup() {
        cont.top();
        cont.clear();
        cont.defaults();

        cont.table(a ->{
            a.label(() -> Core.bundle.get("server.hidden") + " "+ Core.bundle.get("editor.teams")).row();
            a.image().color(Color.scarlet).height(3f).padTop(5).padBottom(5).growX().row();
            selectedPane = a.pane(selectedTable).size(a.getWidth(), a.getWidth()).scrollX(false).growX();
        });
        cont.table(a ->{
            a.label(() -> Core.bundle.get("server.shown") + " "+ Core.bundle.get("editor.teams")).row();
            a.image().color(Pal.accent).height(3f).padTop(5).padBottom(5).growX().row();
            unselectedPane = a.pane(unselectedTable).size(a.getWidth(), a.getWidth()).scrollX(false).growX();
        });

        selectedTable.clear();
        selectedTable.defaults().growX();
        unselectedTable.clear();
        unselectedTable.defaults().growX();

        int[] icons = {0, 0, 0, 0};

        Seq<Team> tmSz = new Seq<>();
        tmSz.addAll(Team.all);

        if(sort == 1) tmSz.sort(t -> t.color.hue());
        else if(sort == 2)  tmSz.sort(t ->t.color.saturation());
        else if(sort == 3)  tmSz.sort(t ->t.color.sum());
        else tmSz.sort(t -> t.id);



        for (int id = 0; id < tmSz.size ; id++){
            int finalId = id;
            boolean selected = CuiVars.hiddenTeams[tmSz.get(finalId).id];
            Table tab = selected ? selectedTable : unselectedTable;

            ImageButton button = new ImageButton(Tex.whiteui, Styles.clearNoneTogglei);
            button.clicked(() -> click(selected, tmSz.get(finalId).id));
            button.resizeImage(teamSize);
            button.getImageCell();
            button.getStyle().imageUpColor = tmSz.get(finalId).color;

            Label lab = new Label(tmSz.get(id).emoji.isEmpty() ? tmSz.get(id).id + "":  "[white]" +tmSz.get(id).emoji);

            lab.touchable(() -> Touchable.disabled);
            lab.setAlignment(Align.center);
            lab.setStyle(Styles.outlineLabel);
            tab.stack(
                button,
                lab
            ).size(teamSize).margin(5f).grow().get();

            if(selected){
                icons[3]++;
                if (icons[1] >= 7){
                    tab.row();
                    icons[1] = 0;
                } else icons[1]++;
            } else {
                icons[2]++;
                if (icons[0] >= 7){
                    tab.row();
                    icons[0] = 0;
                } else icons[0]++;
            }
        }
        if(icons[2] == 0) unselectedTable.table(t -> t.label(() -> "@empty").growX().center().style(Styles.outlineLabel).pad(20f)).width(selectedPane.maxWidth());
        if(icons[3] == 0) selectedTable.table(t -> t.label(() -> "@empty").growX().center().style(Styles.outlineLabel).pad(20f)).width(selectedPane.maxWidth());
    }

    public void click(boolean add, int id){
        CuiVars.hiddenTeams[id] = !add;
        setup();
    }

    void rebuildOtherButtons() {
        buttons.defaults().size(width, 64f);
        buttons.button("@back", Icon.left, this::hide).size(210f, 64f).tooltip("@cui-domination.tip");

        addCloseListener();

        buttons.button("@defaults", Icon.exit, () -> {
            CuiVars.hiddenTeams = new boolean[Team.all.length];
            CuiVars.hiddenTeams[0] = true;
            setup();

        }).size(180, 64f);
        buttons.button("@clear", Icon.trash, () -> {
            CuiVars.hiddenTeams = new boolean[Team.all.length];
            setup();
        }).size(120, 64f);
        buttons.button("@waves.spawn.all", Icon.move, () -> {
            CuiVars.hiddenTeams = new boolean[Team.all.length];
            for(int i = 0; i < Team.all.length; i++) CuiVars.hiddenTeams[i] = true;
            setup();
        }).size(120, 64f);
        sortTxt = Core.bundle.get("cui-teams-sort." + sort);
        buttons.button(Core.bundle.get("cui-teams-sort.base") + sortTxt, () -> {
            sort++;
            if(sort > 3) sort = 0;
            setup();
            buttons.clear();
            rebuildOtherButtons();
        }).size(150, 64f);
        titleTable.row();
        titleTable.add(header);

    }
}
