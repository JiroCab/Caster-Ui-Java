package casterui.io;

import arc.KeyBinds;
import arc.input.InputDevice;
import arc.input.KeyCode;
import mindustry.input.Binding;
import mindustry.ui.dialogs.KeybindDialog;

import static arc.Core.*;
import static mindustry.Vars.ui;

/*code from https://github.com/xzxADIxzx/Scheme-Size/blob/main/src/java/scheme/moded/ModedBinding.java
* Also the two are incompatible, only one or the either adds their respective keybinds TODO work around*/
public enum CuiBinding implements KeyBinds.KeyBind {
    track_cursor(KeyCode.h, "cui"),
    last_destroyed_core(KeyCode.g),
    toggle_cui_menu(KeyCode.f7),
    host_teams(KeyCode.home),
    spectate_next_player(KeyCode.leftBracket),
    spectate_previous_player(KeyCode.rightBracket);


    private final KeyBinds.KeybindValue defaultValue;
    private final String category;

    CuiBinding(KeyBinds.KeybindValue defaultValue, String category) {
        this.defaultValue = defaultValue;
        this.category = category;
    }

    CuiBinding(KeyBinds.KeybindValue defaultValue) {
        this(defaultValue, null);
    }

    @Override
    public KeyBinds.KeybindValue defaultValue(InputDevice.DeviceType type) {
        return defaultValue;
    }

    @Override
    public String category() {
        return category;
    }

    public static void load() {
        KeyBinds.KeyBind[] orign = (KeyBinds.KeyBind[]) Binding.values();
        KeyBinds.KeyBind[] moded = (KeyBinds.KeyBind[]) values();
        KeyBinds.KeyBind[] binds = new KeyBinds.KeyBind[orign.length + moded.length];

        System.arraycopy(orign, 0, binds, 0, orign.length);
        System.arraycopy(moded, 0, binds, orign.length, moded.length);

        keybinds.setDefaults(binds);
        settings.load(); // update controls
        ui.controls = new KeybindDialog();
    }

}
