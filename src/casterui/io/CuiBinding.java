package casterui.io;

import arc.KeyBinds;
import arc.input.InputDevice;
import arc.input.KeyCode;

public enum CuiBinding implements KeyBinds.KeyBind {
    track_cursor(KeyCode.h, "cui"),
    last_destroyed_core(KeyCode.g),
    toggle_cui_menu(KeyCode.f7),
    change_teams(KeyCode.home),

    spectate_next_player(KeyCode.semicolon, "cui-cycle"), // [ & ] is used by default cargo pickup/drop mb
    spectate_previous_player(KeyCode.apostrophe),
    spectate_next_core(KeyCode.unknown),
    spectate_previous_core(KeyCode.unknown),
    spectate_ignore_coreless(KeyCode.unknown),

    toggle_units_player_table_controls(KeyCode.unknown, "cui-counter"),
    toggle_table_core_units(KeyCode.unknown),
    toggle_table_summarize_players(KeyCode.unknown),

    toggle_player_cursor(KeyCode.unknown, "cui-trackers"),
    filter_player_cursor(KeyCode.unknown),
    toggle_track_logic(KeyCode.unknown),
    toggle_unit_cmd(KeyCode.unknown),
    toggle_unit_Cmd_type(KeyCode.unknown),


    toggle_unit_hp_bars(KeyCode.unknown, "cui-settings"),
    toggle_shorten_items_info(KeyCode.unknown),
    toggle_block_hp(KeyCode.unknown),
    toggle_team_items(KeyCode.unknown),
    toggle_shorten_team_items(KeyCode.unknown),
    toggle_alerts_circle(KeyCode.unknown),
    toggle_alerts_circle_reverse_growth(KeyCode.unknown),
    toggle_alerts_toast(KeyCode.unknown),
    toggle_alerts_toast_bottom(KeyCode.unknown),
    toggle_factory_style(KeyCode.unknown),
    toggle_domination(KeyCode.unknown),

    toggle_cui_kill_switch(KeyCode.unknown),

    map_player_1 (KeyCode.num1,"cui-player-mapping"),
    map_player_2(KeyCode.num2),
    map_player_3(KeyCode.num3),
    map_player_4(KeyCode.num4),
    map_player_5(KeyCode.num5),
    map_player_6(KeyCode.num6),
    map_player_7(KeyCode.num7),
    map_player_8(KeyCode.num8),
    map_player_9(KeyCode.num9),
    map_player_10(KeyCode.unknown),

    save_camera (KeyCode.unknown,"cui-camera-mapping"),
    move_camera (KeyCode.unknown),
    map_camera_1 (KeyCode.unknown),
    map_camera_2(KeyCode.unknown),
    map_camera_3(KeyCode.unknown),
    map_camera_4(KeyCode.unknown),
    map_camera_5(KeyCode.unknown),
    map_camera_6(KeyCode.unknown),
    map_camera_7(KeyCode.unknown),
    map_camera_8(KeyCode.unknown),
    map_camera_9(KeyCode.unknown),
    map_camera_10(KeyCode.unknown),


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
