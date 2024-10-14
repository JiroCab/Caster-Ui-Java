package casterui.io.ui.dialog;

import arc.Core;
import arc.KeyBinds;
import arc.graphics.Color;
import arc.input.InputDevice;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.*;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import casterui.CuiVars;
import casterui.io.CuiBinding;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.KeybindDialog;

import java.util.Objects;

import static arc.Core.*;

public class CuiRebindDialog extends KeybindDialog {
    public arc.KeyBinds cuiKeyBinds = new KeyBinds();
    private KeyBinds.KeyBind[] definitions;

    public CuiRebindDialog(){
        cuiKeyBinds.setDefaults(CuiBinding.values());
        setup();
        setFillParent(true);
        title.setAlignment(Align.center);
        titleTable.row();
        titleTable.add(new Image()).growX().height(3f).pad(4f).get().setColor(Pal.accent);
    }

    public void BindInt(){
        for(KeyBinds.KeyBind keybind : cuiKeyBinds.getKeybinds()){
            if(settings.has("cui-keybind-"+keybind.name())){
                for (KeyCode key : KeyCode.values()){
                    if(!Objects.equals(key.value, settings.getString("cui-keybind-" + keybind.name()))){
                        section.binds.get(section.device.type(), OrderedMap::new).put(rebindKey, new KeyBinds.Axis(key));
                    }
                }
                Log.err(settings.getString("cui-keybind-"+keybind.name()));
            }
        }
    }

    private void setup(){
        cont.clear();

        KeyBinds.Section[] sections = cuiKeyBinds.getSections();
        definitions = cuiKeyBinds.getKeybinds();

        Stack stack = new Stack();
        ButtonGroup<TextButton> group = new ButtonGroup<>();
        ScrollPane pane = new ScrollPane(stack);
        pane.setFadeScrollBars(false);
        this.section = sections[0];


        for(KeyBinds.Section section : sections){

            if(!sectionControls.containsKey(section))
                sectionControls.put(section, input.getDevices().indexOf(section.device, true));

            if(sectionControls.get(section, 0) >= input.getDevices().size){
                sectionControls.put(section, 0);
                section.device = input.getDevices().get(0);
            }

            if(sections.length != 1){
                TextButton button = new TextButton(bundle.get("section." + section.name + ".name", Strings.capitalize(section.name)));
                if(section.equals(this.section))
                    button.toggle();

                button.clicked(() -> this.section = section);

                group.add(button);
                cont.add(button).fill();
            }

            Table table = new Table();

            Label device = new Label("Keyboard");
            //device.setColor(style.controllerColor);
            device.setAlignment(Align.center);

            Seq<InputDevice> devices = input.getDevices();

            Table stable = new Table();

            stable.button("<", () -> {
                int i = sectionControls.get(section, 0);
                if(i - 1 >= 0){
                    sectionControls.put(section, i - 1);
                    section.device = devices.get(i - 1);
                    setup();
                }
            }).disabled(sectionControls.get(section, 0) - 1 < 0).size(40);

            stable.add(device).minWidth(device.getMinWidth() + 60);

            device.setText(input.getDevices().get(sectionControls.get(section, 0)).name());

            stable.button(">", () -> {
                int i = sectionControls.get(section, 0);

                if(i + 1 < devices.size){
                    sectionControls.put(section, i + 1);
                    section.device = devices.get(i + 1);
                    setup();
                }
            }).disabled(sectionControls.get(section, 0) + 1 >= devices.size).size(40);

            table.add().height(10);
            table.row();
            if(section.device.type() == InputDevice.DeviceType.controller){
                table.table(info -> info.add("Controller Type: [lightGray]" +
                        Strings.capitalize(section.device.name())).left());
            }
            table.row();

            String lastCategory = null;
            var tstyle = Styles.defaultt;

            for(KeyBinds.KeyBind keybind : cuiKeyBinds.getKeybinds()){

                if(!Objects.equals(lastCategory, keybind.category()) && keybind.category() != null){
                    table.add(bundle.get("category." + keybind.category() + ".name", Strings.capitalize(keybind.category()))).color(Color.gray).colspan(4).pad(10).padBottom(4).row();
                    table.image().color(Color.gray).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();
                    lastCategory = keybind.category();
                }

                if(keybind.defaultValue(section.device.type()) instanceof KeyBinds.Axis){
                    table.add(bundle.get("keybind." + keybind.name() + ".name", Strings.capitalize(keybind.name())), Color.white).left().padRight(40).padLeft(8);

                    table.labelWrap(() -> {
                        KeyBinds.Axis axis = cuiKeyBinds.get(section, keybind);
                        return axis.key != null ? axis.key.toString() : axis.min + " [red]/[] " + axis.max;
                    }).color(Pal.accent).left().minWidth(90).fillX().padRight(20);

                    table.button("@settings.rebind", tstyle, () -> {
                        rebindAxis = true;
                        rebindMin = true;
                        openDialog(section, keybind);
                    }).width(130f);
                }else{
                    table.add(bundle.get("keybind." + keybind.name() + ".name", Strings.capitalize(keybind.name())), Color.white).left().padRight(40).padLeft(8);
                    table.label(() -> cuiKeyBinds.get(section, keybind).key.toString()).color(Pal.accent).left().minWidth(90).padRight(20);

                    table.button("@settings.rebind", tstyle, () -> {
                        rebindAxis = false;
                        rebindMin = false;
                        openDialog(section, keybind);
                    }).width(130f);
                }
                table.button("@settings.resetKey", tstyle, () ->{
                    cuiKeyBinds.resetToDefault(section, keybind);
                    settings.remove("cui-keybind-"+keybind.name());
                }).width(130f).pad(2f).padLeft(4f);
                table.row();
            }

            table.visible(() -> this.section.equals(section));

            table.button("@settings.reset", () -> {
                cuiKeyBinds.resetToDefaults();
                for(KeyBinds.KeyBind keybind : cuiKeyBinds.getKeybinds()) {
                    settings.remove("cui-keybind-" + keybind.name());
                }

            }).colspan(4).padTop(4).fill();

            stack.add(table);
        }

        cont.row();
        cont.add(pane).growX().colspan(sections.length);

    }

    void rebind(KeyBinds.Section section, KeyBinds.KeyBind bind, KeyCode newKey){
        if(rebindKey == null) return;
        rebindDialog.hide();
        boolean isAxis = bind.defaultValue(section.device.type()) instanceof KeyBinds.Axis;

        if(isAxis){
            if(newKey.axis || !rebindMin){
                section.binds.get(section.device.type(), OrderedMap::new).put(rebindKey, newKey.axis ? new KeyBinds.Axis(newKey) : new KeyBinds.Axis(minKey, newKey));
            }
        }else{
            section.binds.get(section.device.type(), OrderedMap::new).put(rebindKey, new KeyBinds.Axis(newKey));
        }

        if(rebindAxis && isAxis && rebindMin && !newKey.axis){
            rebindMin = false;
            minKey = newKey;
            openDialog(section, rebindKey);
        }else{
            rebindKey = null;
            rebindAxis = false;
        }
        save();
    }


    private void openDialog(KeyBinds.Section section, KeyBinds.KeyBind name){
        rebindDialog = new Dialog(rebindAxis ? bundle.get("keybind.press.axis") : bundle.get("keybind.press"));

        rebindKey = name;

        rebindDialog.titleTable.getCells().first().pad(4);

        if(section.device.type() == InputDevice.DeviceType.keyboard){

            rebindDialog.addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(Core.app.isAndroid()) return false;
                    rebind(section, name, button);
                    return false;
                }

                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode){
                    rebindDialog.hide();
                    if(keycode == KeyCode.escape) return false;
                    rebind(section, name, keycode);
                    return false;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                    if(!rebindAxis) return false;
                    rebindDialog.hide();
                    rebind(section, name, KeyCode.scroll);
                    return false;
                }
            });
        }

        rebindDialog.show();
        Time.runTask(1f, () -> getScene().setScrollFocus(rebindDialog));
    }

    public void load(){
        if(definitions == null) return;

        for(KeyBinds.Section sec : cuiKeyBinds.getSections()){
            for(InputDevice.DeviceType type : InputDevice.DeviceType.values()){
                for(KeyBinds.KeyBind def : cuiKeyBinds.getKeybinds()){
                    String rname = "keybind-" + sec.name + "-" + type.name() + "-" + def.name();

                    KeyBinds.Axis loaded = load(rname);

                    if(loaded != null) sec.binds.get(type, OrderedMap::new).put(def, loaded);
                }
            }

            sec.device = input.getDevices().get(Mathf.clamp(settings.getInt(sec.name + "-last-device-type", 0), 0, input.getDevices().size - 1));
        }
    }

    private KeyBinds.Axis load(String name){
        if(settings.getBool(name + "-single", true)){
            KeyCode key = KeyCode.byOrdinal(settings.getInt(name + "-key", KeyCode.unset.ordinal()));
            return key == KeyCode.unset ? null : new KeyBinds.Axis(key);
        }else{
            KeyCode min = KeyCode.byOrdinal(settings.getInt(name + "-min", KeyCode.unset.ordinal()));
            KeyCode max = KeyCode.byOrdinal(settings.getInt(name + "-max", KeyCode.unset.ordinal()));
            return min == KeyCode.unset || max == KeyCode.unset ? null : new KeyBinds.Axis(min, max);
        }
    }

    void save(){
        if(definitions == null) return;

        for(KeyBinds.Section sec : cuiKeyBinds.getSections()){
            for(InputDevice.DeviceType type : sec.binds.keys()){
                for(ObjectMap.Entry<KeyBinds.KeyBind, KeyBinds.Axis> entry : sec.binds.get(type).entries()){
                    String rname = "keybind-" + sec.name + "-" + type.name() + "-" + entry.key.name();
                    save(entry.value, rname);
                }
            }
            settings.put(sec.name + "-last-device-type", input.getDevices().indexOf(sec.device, true));
        }
    }

    private void save(KeyBinds.Axis axis, String name){
        settings.put(name + "-single", axis.key != null);

        if(axis.key != null){
            settings.put(name + "-key", axis.key.ordinal());
        }else{
            settings.put(name + "-min", axis.min.ordinal());
            settings.put(name + "-max", axis.max.ordinal());
        }
    }
}
