package casterui.io.ui.dialog;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import casterui.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.io.*;
import java.util.*;

import static arc.Core.bundle;
import static casterui.CuiVars.*;
import static mindustry.Vars.*;

public class TeamBlackListerDialog extends BaseDialog{
    public Cell<ScrollPane> selectedPane, unselectedPane;
    public int teamSize = 50;
    public Table selectedTable = new Table(), unselectedTable = new Table();
    public int sort = 0;
    public String sortTxt = "";
    private int write;
    public static String headerSub[] = {"teams", "counter", "team", "cycle"};


    public TeamBlackListerDialog(){
        super("@cui-teamblacklister");
        rebuildOtherButtons();
        shown(this::setup);
        onResize(this::setup);
        setHideAction(() -> {
            StringBuilder out = new StringBuilder();
            boolean first = true;
            for (int id = 0; id < Team.all.length ; id++){
                if(hiddenTeamList[write][id]){
                    if(first) first = false;
                    else out.append(",");
                    out.append(id);
                }
            }

            Core.settings.put("cui-hiddenTeams-" + headerSub[write], out.toString());
            updateHiddenTeams();
            return Actions.fadeOut(0.2F, Interp.fade);
        });
    }

    public void show(int write){
        this.write = write;
        this.title.setText(Core.bundle.get("cui-teamblacklister") + " - " + bundle.get("cui-filter" + write));
        show();
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


        int max = (Vars.mobile || Vars.testMobile) ? 4 : 7;
        for (int id = 0; id < tmSz.size ; id++){
            int finalId = id;
            boolean selected = hiddenTeamList[write][tmSz.get(finalId).id];
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
                if (icons[1] >= max){
                    tab.row();
                    icons[1] = 0;
                } else icons[1]++;
            } else {
                icons[2]++;
                if (icons[0] >= max){
                    tab.row();
                    icons[0] = 0;
                } else icons[0]++;
            }
        }
        if(icons[2] == 0) unselectedTable.table(t -> t.label(() -> "@empty").growX().center().style(Styles.outlineLabel).pad(20f)).width(selectedPane.maxWidth());
        if(icons[3] == 0) selectedTable.table(t -> t.label(() -> "@empty").growX().center().style(Styles.outlineLabel).pad(20f)).width(selectedPane.maxWidth());
    }

    public void click(boolean add, int id){
        hiddenTeamList[write][id] = !add;
        setup();
    }

    void rebuildOtherButtons() {
        buttons.clear();
        //buttons.defaults().size(width, 64f);

        addCloseListener();
        Table but = new Table(), but1 = new Table();

        but1.button("@back", Icon.left, this::hide).size(170f, 64f).tooltip("@cui-domination.tip");
        but1.button("@defaults", Icon.exit, () -> {
            hiddenTeamList[write] = new boolean[Team.all.length];
            hiddenTeamList[write][0] = true;
            setup();

        }).size(170, 64f);
        but1.button(Icon.export, this::exportOptions).size(60, 64f);
        
        but.button("@clear", Icon.trash, () -> {
            hiddenTeamList[write] = new boolean[Team.all.length];
            setup();
        }).size(120, 64f);

        but.button("@waves.spawn.all", Icon.move, () -> {
            hiddenTeamList[write] = new boolean[Team.all.length];
            for(int i = 0; i < Team.all.length; i++) hiddenTeamList[write][i] = true;
            setup();
        }).size(120, 64f);
        sortTxt = Core.bundle.get("cui-teams-sort." + sort);
        but.button(Core.bundle.get("cui-teams-sort.base") + sortTxt, () -> {
            sort++;
            if(sort > 3) sort = 0;
            setup();
            rebuildOtherButtons();
        }).size(150, 64f);

        buttons.add(but1).center();
        if(Vars.mobile || Vars.testMobile) buttons.row();
        buttons.add(but).center();
    }

    public void exportOptions(){
        BaseDialog dialog = new BaseDialog("@waves.edit");
        dialog.addCloseButton();
        dialog.setFillParent(false);
        dialog.cont.table(Tex.button, t -> {
            var style = Styles.cleart;
            t.defaults().size(280f, 64f).pad(2f);

            t.button("@waves.copy", Icon.copy, style, () -> {
                ui.showInfoFade("@waves.copied");
                writeTeams();
                dialog.hide();
            }).marginLeft(12f).row();

            t.button("@waves.load", Icon.download, style, () -> {
                readTeams();
                dialog.hide();
            }).disabled(Core.app.getClipboardText() == null || !Core.app.getClipboardText().startsWith("[")).marginLeft(12f).row();

        });

        dialog.show();
    }

    public void writeTeams(){
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        boolean first = true;
        for(int i = 0; i < hiddenTeamList[write].length; i++){
            if(hiddenTeamList[write][i]){
                if(first)first = false;
                else buffer.append(",");

                buffer.append(i);
        }}
        buffer.append("]");

        Core.app.setClipboardText(buffer.toString());
        ui.showInfoFade(buffer.toString());

    }

    public void readTeams(){
        if(Core.app.getClipboardText() == null || Core.app.getClipboardText().isEmpty()) return;
        String[] buffer = Core.app.getClipboardText().replace("[", "").replace("]", "").split(",");

        hiddenTeamList[write] = new boolean[Team.all.length];
        try{
            for(String param : buffer) if(Integer.parseInt(param) >= 0 && Integer.parseInt(param) < Team.all.length) hiddenTeamList[write][Integer.parseInt(param)] = true;
        }catch(Exception e){
            e.printStackTrace();
            ui.showErrorMessage("@invalid");
        }
        setup();

    }
}
