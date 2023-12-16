package casterui.io;

import arc.KeyBinds;
import arc.input.InputDevice;
import arc.input.KeyCode;

public enum CuiBinding implements KeyBinds.KeyBind {
    track_cursor(KeyCode.h, "cui"),
    last_destroyed_core(KeyCode.g),
    toggle_cui_menu(KeyCode.f7),
    change_teams(KeyCode.home),
    spectate_next_player(KeyCode.semicolon), // [ & ] is used by default cargo pickup/drop mb
    spectate_previous_player(KeyCode.apostrophe),

    toggle_unit_hp_bars(KeyCode.unknown, "cui-settings"),
    toggle_player_cursor(KeyCode.unknown),
    toggle_track_logic(KeyCode.unknown),
    toggle_shorten_items_info(KeyCode.unknown),
    toggle_block_hp(KeyCode.unknown),
    toggle_units_player_table_controls(KeyCode.unknown),
    toggle_table_core_units(KeyCode.unknown),
    toggle_table_summarize_players(KeyCode.unknown),
    toggle_team_items(KeyCode.unknown),
    toggle_shorten_team_items(KeyCode.unknown),

    ;


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


}
