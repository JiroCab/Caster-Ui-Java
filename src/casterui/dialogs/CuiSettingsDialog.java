package casterui.dialogs;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.Dialog.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.Slider.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.dialogs.SettingsMenuDialog.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;

import static arc.Core.*;
import static casterui.CuiMain.data;
import static mindustry.gen.Tex.*;

public class CuiSettingsDialog{
    public static SliderStyle emptySlider;
    public static Drawable emptySliderIcon, paleOrangePanel, paleGreenPanel;
    public static DialogStyle emptyDialogStyle;

    public static void buildCategory(){
        emptySliderIcon = Core.atlas.drawable("slider-knob");

        emptySlider = new SliderStyle(){{
            background = pane                       ;
            knob = sliderKnob;
            emptySliderIcon.setLeftWidth(emptySliderIcon.getLeftWidth() * 0.5f);
        }};

        emptyDialogStyle = new DialogStyle(){{
            stageBackground = Styles.none;
            titleFont = Fonts.def;
            background = Styles.none;
            titleFontColor = Pal.heal;
        }};

        TextureRegionDrawable whiteui = (TextureRegionDrawable)Tex.whiteui;
        paleOrangePanel = whiteui.tint( Color.valueOf("261d0b"));
        paleGreenPanel = whiteui.tint( Color.valueOf("06260d"));

        Vars.ui.settings.getCategories().add(new CuiSettingsCategory("@settings.cui.settings", Icon.logic, t-> {

            sliderField(t, "owo", f -> data.debug = f, () -> data.debug, -1, -10, 10, 1, i -> i +"", null);
            check(t, "uwu", b -> data.trackerBackground = b, () -> data.trackerBackground, false);
            picker(t, ":m", i -> data.alertChatCondition = i, () -> data.alertChatCondition, 1, 0, 100, i -> i+"",null );
        }));

    }

    public static class CuiSettingsCategory extends SettingsCategory{


        public CuiSettingsCategory(String name, Drawable icon, Cons<SettingsTable> builder){
            super(name, icon, builder);
            table =new  CuiSettingsTable();
            builder.get(table);
        }
    }

    public static class CuiSettingsTable extends SettingsTable{

        @Override
        public void rebuild(){
            clearChildren();

            for(Setting setting : list){
                setting.add(this);
            }

            button(bundle.get("settings.reset", "Reset to Defaults"), () -> {
                for(Setting setting : list){
                    if(setting instanceof CuiSetting cs) cs.reset();
                    if(setting.name == null || setting.title == null) continue;
                    settings.remove(setting.name);
                }
                rebuild();
            }).margin(14).width(240f).pad(6);
        }
    }

    public interface CuiSetting{
        public void reset();
    }

    public static void check(SettingsTable t, String name, Boolc cons,  Boolp prov, boolean def, Boolc changed){
        t.pref(new CuiCheckSetting(name, cons, prov, def, changed));
    }

    public static void check(SettingsTable t, String name, Boolc cons,  Boolp prov, boolean def){
        t.pref(new CuiCheckSetting(name, cons, prov,  def,null));
    }

    public static class CuiCheckSetting extends Setting implements CuiSetting{
        Boolc changed, cons;
        Boolp prov;
        boolean def;

        public CuiCheckSetting(String name, Boolc cons,  Boolp prov, boolean def, Boolc changed){
            super(name);
            this.changed = changed;
            this.prov = prov;
            this.cons = cons;
        }

        @Override
        public void add(SettingsTable table){
            Button box = new Button(Styles.grayt);
            box.background(Styles.grayPanel);
            box.margin(10f);

            box.add(new Image()).update(i -> i.setDrawable(box.isOver() ? (box.isChecked() ? Tex.checkOnOver : Tex.checkOver) : box.isChecked() ? Tex.checkOn : Tex.checkOff))
            .size(32f).padRight(8f).padLeft(-4f);

            box.add(title);

            box.update(() -> box.setChecked(prov.get()));

            box.clicked(() -> {
                cons.get(box.isChecked());
                if(changed != null){
                    changed.get(box.isChecked());
                }
            });

            box.left();
            addDesc(table.add(box).minWidth(Math.min(500f, Core.graphics.getWidth() / 1.2f / Scl.scl(1f))).fillX().height(45f).left().padTop(7f).get());
            table.row();
        }

        @Override
        public void reset(){
            cons.get(def);
        }
    }

    public static void sliderField(SettingsTable t, String name, Floatc cons, Floatp prov, float def, float min, float max, int step, StringProcessor s, Floatc changed){
        t.pref(new CuiFancySliderSetting(name, cons, prov, def, min, max, step, s, changed));
    }

    public static void sliderField(SettingsTable t, String name, Floatc cons, Floatp prov, float def, float min, float max, int step, StringProcessor s){
        t.pref(new CuiFancySliderSetting(name, cons, prov, def, min, max, step, s, null));
    }

    //Fake Settings that dont actuallly put into Core.settings since we use hjson to store settings
    public static class CuiFancySliderSetting extends Setting implements CuiSetting{
        float def, min, max, step;
        StringProcessor sp;
        Floatp prov;
        Floatc cons, changed;

        public CuiFancySliderSetting(String name, Floatc cons, Floatp prov, float def, float min, float max, int step, StringProcessor s, Floatc changed){
            super(name);
            this.def = def;
            this.min = min;
            this.max = max;
            this.step = step;
            this.sp = s;
            this.changed = changed;
            this.prov = prov;
            this.cons = cons;
        }

        @Override
        public void add(SettingsTable table){
            TextField field = Elem.newField(prov.get() + "", s -> cons.get(Strings.parseFloat(s)));

            Slider slider = new Slider(min, max, step, false);
            slider.setStyle(emptySlider);

            slider.setValue(Mathf.clamp(prov.get(), min, max));

            Label value = new Label("", Styles.outlineLabel);
            Table content = new Table();
            content.add(title + ":", Styles.outlineLabel).left().growX().wrap();
            //content.add(value).padLeft(10f).right();
            content.margin(0f, 33f, 3f, 33f);
            content.touchable = Touchable.disabled;

            slider.changed(() -> {
                cons.get(slider.getValue());
                value.setText(sp.get((int)slider.getValue()));
                if(changed != null) changed.get(slider.getValue());
                field.setText(slider.getValue() + "");
            });
            slider.change();


            field.setAlignment(Align.right);

            field.changed(() -> {
                if(Strings.canParseFloat(field.getText())){
                    cons.get(Strings.parseFloat(field.getText()));
                    slider.setValue(Mathf.clamp(prov.get(), min, max));
                    if(changed != null) changed.get(Strings.parseFloat(field.getText()));
                }
            });
            field.setValidator(Strings::canParseFloat);



            table.table( bg -> {
                float wid = Math.min(Core.graphics.getWidth() / 1.2f / Scl.scl(1f), 500f);
                addDesc(
                        bg.table( t->{
                            t.table(z -> {
                                z.add(content).growX();

                                z.stack(
                                    new Table( ta -> {
                                        ta.image(Icon.pencilSmall).padRight(wid /4).left();
                                    }),
                                    field
                                ).padLeft((wid /6) * 2.75f).padRight(30f).get().clicked(() -> scene.setKeyboardFocus(field));
                            }).growX().row();
                            t.add(slider).width(wid).growX().padLeft(10).height(25).padRight(10).padBottom(5).padTop(5);

                            //.width(wid).left().padTop(3).padLeft(5).padBottom(5).padRight(10).get()
                        }).width(wid).left().padTop(3).padLeft(5).padBottom(5).padRight(10).get()

                );
                bg.background(Styles.grayPanel);
            }).padTop(7f);
            table.row();
        }

        @Override
        public void reset(){
            cons.get(def);
        }
    }

    public static void picker(SettingsTable t, String name, Intc cons, Intp prov, int def, int min, int max, StringProcessor s, Intc changed, float offset ){
        t.pref(new CuiPickerSetting(name, cons, prov, def, min, max, s, changed, offset));
    }

    public static void picker(SettingsTable t, String name, Intc cons, Intp prov, int def, int min, int max, StringProcessor s, Intc changed){
        t.pref(new CuiPickerSetting(name, cons, prov, def, min, max, s, changed, 1.75f));
    }

    //Fake Settings that dont actuallly put into Core.settings since we use hjson to store settings
    public static class CuiPickerSetting extends Setting implements CuiSetting{
        int def, min, max;
        StringProcessor sp;
        Intp prov;
        Intc cons, changed;

        float offset;

        public CuiPickerSetting(String name, Intc cons, Intp prov, int def, int min, int max, StringProcessor s, Intc changed, float offset){
            super(name);
            this.def = def;
            this.min = min;
            this.max = max;
            this.sp = s;
            this.changed = changed;
            this.prov = prov;
            this.cons = cons;
            this.offset = offset;
        }

        @Override
        public void add(SettingsTable table){
            float
                wid = Math.min(Core.graphics.getWidth() / 1.2f / Scl.scl(1f), 500f)
            ;

            Table content = new Table();
            Table pannel = new Table();
            content.add(title + ":", Styles.outlineLabel).left().growX().wrap();
            content.margin(0f, 33f, 3f, 33f);
            content.touchable = Touchable.disabled;

            TextButton button = new TextButton(sp.get(prov.get()), Styles.defaultt);
            button.add(new Image(Icon.downOpen)).size(Vars.iconMed);
            button.getLabel().setAlignment(Align.right);
            button.getCells().reverse();
            button.clicked(() -> {
                pannel.background(paleGreenPanel);
                Sounds.uiChat.play();
                Dialog owo = new BaseDialog("@configure");
                owo.background(null);
                owo.closeOnBack(() -> {
                    pannel.background(Styles.grayPanel);
                });

                Table in = new Table();
                in.defaults().pad(1f).width(wid * 0.75f);
                ScrollPane pane = new ScrollPane(in);
                pane.setStyle(Styles.smallPane);
                pane.setScrollingDisabledX(true);
                pane.setFadeScrollBars(false);

                String out;

                for(int i = min; i < max; i++){
                    out = "";
                    if (i == prov.get()) out += "[accent]" + Iconc.rightOpenOut + " ";
                    else if(i == def) out += Iconc.undo + " ";
                    else out += "  ";
                    out += sp.get(i);

                    int finalI = i;

                    TextButton clicker = Elem.newButton(out, () ->{
                        cons.get(finalI);
                        owo.hide();
                        pannel.background(Styles.grayPanel);
                        button.setText(sp.get(finalI));
                    });
                    clicker.getLabel().setAlignment(Align.left);
                    in.add(clicker).padTop(5f).padRight(15).padLeft(15).row();

                }

                pane.setScrollPercentY(0.5f);



                owo.cont.table(tab -> {
                    tab.background(Styles.grayPanel);
                    tab.add(pane);
                }).padLeft(wid * 0.5f).get().background(paleOrangePanel);
                owo.show();
                owo.background(Styles.none);
                owo.setStyle(emptyDialogStyle);
                owo.clicked( () -> {
                    owo.hide();
                    pannel.background(Styles.grayPanel);
                });

                owo.titleTable.clear();
            });

            table.add(pannel).padTop(7f);

            addDesc(
            pannel.table( t->{
                t.table(z -> {
                    z.add(content).growX();
                    z.add(button).width((wid / 3) * offset).growX().padRight(2).right();
                }).growX().row();

            }).width(wid).left().padTop(3).padLeft(5).padBottom(5).padRight(10).get()

            );
            pannel.background(Styles.grayPanel);

            table.row();
        }

        @Override
        public void reset(){
            cons.get(def);
        }
    }
}
