package casterui.io;

import arc.input.*;

public class CuiBinding {

    public static final KeyBind
        toggle_cui_kill_switch = KeyBind.add("cui-toggle_cui_kill_switch", KeyCode.unset, "cui"),
        track_cursor = KeyBind.add("cui-track_cursor", KeyCode.h, "cui"),
        last_destroyed_core = KeyBind.add("cui-last_destroyed_core", KeyCode.g, "cui"),
        toggle_cui_menu = KeyBind.add("cui-toggle_cui_menu", KeyCode.f7, "cui"),
        change_teams = KeyBind.add("cui-change_teams", KeyCode.home, "cui"),

        spectate_next_player = KeyBind.add("cui-spectate_next_player", KeyCode.semicolon, "cui-spectate"),
        spectate_previous_player = KeyBind.add("cui-spectate_previous_player", KeyCode.apostrophe, "cui-spectate"),
        spectate_next_core = KeyBind.add("cui-spectate_next_core", KeyCode.unset, "cui-spectate"),
        spectate_previous_core = KeyBind.add("cui-spectate_previous_core", KeyCode.unset, "cui-spectate"),
        spectate_ignore_coreless = KeyBind.add("cui-spectate_ignore_coreless", KeyCode.unset, "cui-spectate"),

        toggle_units_player_table_controls = KeyBind.add("cui-toggle_units_player_table_controls", KeyCode.unset, "cui-counter"),
        toggle_table_core_units = KeyBind.add("cui-toggle_table_core_units", KeyCode.unset, "cui-counter"),
        toggle_table_summarize_players = KeyBind.add("cui-toggle_table_summarize_players", KeyCode.unset, "cui-counter"),

        toggle_player_cursor = KeyBind.add("cui-toggle_player_cursor", KeyCode.unset, "cui-trackers"),
        filter_player_cursor = KeyBind.add("cui-filter_player_cursor", KeyCode.unset, "cui-trackers"),
        toggle_track_logic = KeyBind.add("cui-toggle_track_logic", KeyCode.unset, "cui-trackers"),
        toggle_unit_cmd = KeyBind.add("cui-toggle_unit_cmd", KeyCode.unset, "cui-trackers"),
        toggle_unit_Cmd_type = KeyBind.add("cui-toggle_unit_Cmd_type", KeyCode.unset, "cui-trackers"),

        toggle_unit_hp_bars = KeyBind.add("cui-toggle_unit_hp_bars", KeyCode.unset, "cui-settings"),
        toggle_shorten_items_info = KeyBind.add("cui-toggle_shorten_items_info", KeyCode.unset, "cui-settings"),
        toggle_block_hp = KeyBind.add("cui-toggle_block_hp", KeyCode.unset, "cui-settings"),
        toggle_team_items = KeyBind.add("cui-toggle_team_items", KeyCode.unset, "cui-settings"),
        toggle_shorten_team_items = KeyBind.add("cui-toggle_shorten_team_items", KeyCode.unset, "cui-settings"),
        toggle_alerts_circle = KeyBind.add("cui-toggle_alerts_circle", KeyCode.unset, "cui-settings"),
        toggle_alerts_circle_reverse_growth = KeyBind.add("cui-toggle_alerts_circle_reverse_growth", KeyCode.unset, "cui-settings"),
        toggle_alerts_toast = KeyBind.add("cui-toggle_alerts_toast", KeyCode.unset, "cui-settings"),
        toggle_alerts_toast_bottom = KeyBind.add("cui-toggle_alerts_toast_bottom", KeyCode.unset, "cui-settings"),
        toggle_factory_style = KeyBind.add("cui-toggle_factory_style", KeyCode.unset, "cui-settings"),
        toggle_domination = KeyBind.add("cui-toggle_domination", KeyCode.unset, "cui-settings"),

        map_player_1 = KeyBind.add("cui-map_player_1", KeyCode.num1, "cui-mapping"),
        map_player_2 = KeyBind.add("cui-map_player_2", KeyCode.num2, "cui-mapping"),
        map_player_3 = KeyBind.add("cui-map_player_3", KeyCode.num3, "cui-mapping"),
        map_player_4 = KeyBind.add("cui-map_player_4", KeyCode.num4, "cui-mapping"),
        map_player_5 = KeyBind.add("cui-map_player_5", KeyCode.num5, "cui-mapping"),
        map_player_6 = KeyBind.add("cui-map_player_6", KeyCode.num6, "cui-mapping"),
        map_player_7 = KeyBind.add("cui-map_player_7", KeyCode.num7, "cui-mapping"),
        map_player_8 = KeyBind.add("cui-map_player_8", KeyCode.num8, "cui-mapping"),
        map_player_9 = KeyBind.add("cui-map_player_9", KeyCode.num9, "cui-mapping"),
        map_player_10 = KeyBind.add("cui-map_player_10", KeyCode.num0, "cui-mapping"),

        save_camera = KeyBind.add("cui-save_camera", KeyCode.unset, "cui-camera"),
        move_camera = KeyBind.add("cui-move_camera", KeyCode.unset, "cui-camera"),
        map_camera_1 = KeyBind.add("cui-map_camera_1", KeyCode.unset, "cui-camera"),
        map_camera_2 = KeyBind.add("cui-map_camera_2", KeyCode.unset, "cui-camera"),
        map_camera_3 = KeyBind.add("cui-map_camera_3", KeyCode.unset, "cui-camera"),
        map_camera_4 = KeyBind.add("cui-map_camera_4", KeyCode.unset, "cui-camera"),
        map_camera_5 = KeyBind.add("cui-map_camera_5", KeyCode.unset, "cui-camera"),
        map_camera_6 = KeyBind.add("cui-map_camera_6", KeyCode.unset, "cui-camera"),
        map_camera_7 = KeyBind.add("cui-map_camera_7", KeyCode.unset, "cui-camera"),
        map_camera_8 = KeyBind.add("cui-map_camera_8", KeyCode.unset, "cui-camera"),
        map_camera_9 = KeyBind.add("cui-map_camera_9", KeyCode.unset, "cui-camera"),
        map_camera_10 = KeyBind.add("cui-map_camera_10", KeyCode.unset, "cui-camera")
    ;

    public static void init(){}

    public static boolean isUnSet(KeyBind e) {
        return e.value == null || e.value.key == KeyCode.anyKey || e.value.key == KeyCode.unknown;
    }
}
