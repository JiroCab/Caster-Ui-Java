package casterui.io.ui.dialog;

import arc.Core;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;

import static arc.Core.*;
import static mindustry.Vars.ui;

public class CuiSettingsDialog {
    public static Seq<SettingsMenuDialog.SettingsTable> allCuiOptions = new Seq<>();
    public static float commonHeight = 60f;
    
    public static void buildCategory(){

        ui.settings.addCategory("@settings.cui.settings", Icon.logic, table -> {
            //Dropped the `eui` (extended-ui) prefix for `cui` (caster-ui) to avoid conflicts

            /*Legacy Features From the Original Caster-ui Fork */
            boolean[] players = {false};
            table.button("@setting.cui-players-category.name", Icon.players, Styles.togglet, () -> players[0] = !players[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> players[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-ShowPlayerList", true);
                subTable.checkPref("cui-hideNoUnitPlayers", false);
                subTable.checkPref("cui-TrackPlayerCursor", false);
                subTable.checkPref("cui-ShowOwnCursor", false);
                subTable.sliderPref("cui-playerCursorStyle", 6, 1, 7, String::valueOf);
                subTable.sliderPref("cui-playerTrackAlpha", 8, 1, 10, String::valueOf);
                subTable.sliderPref("cui-playerTrackedAlpha", 10, 1, 10, String::valueOf);
                subTable.sliderPref("cui-playerIconSize", 35, 1, 100, String::valueOf);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->players[0]).growX().row();

            boolean[] units = {false};
            table.button("@setting.cui-units-category.name", Icon.units, Styles.togglet, () -> units[0] = !units[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> units[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.checkPref("cui-ShowUnitTable", false);
                subTable.sliderPref("cui-unitsIconSize", 32, 1, 100, String::valueOf);
                subTable.checkPref("cui-showUnitBar", true);
                subTable.checkPref("cui-TrackLogicControl", false);
                subTable.sliderPref("cui-logicLineAlpha", 4, 1, 10, String::valueOf);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->units[0]).growX().row();

            boolean[] alerts = {false};
            table.button("@setting.cui-alerts-category.name", Icon.chat, Styles.togglet, () -> alerts[0] = !alerts[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> alerts[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.checkPref("cui-ShowAlerts", true);
                subTable.checkPref("cui-AlertsUseBottom", true);
                subTable.checkPref("cui-ShowAlertsCircles", true);
                subTable.checkPref("cui-SendChatCoreLost", true);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->alerts[0]).growX().row();

            boolean[] blocks = {false};
            table.button("@setting.cui-blocks-category.name", Icon.effect, Styles.togglet, () -> blocks[0] = !blocks[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> blocks[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.checkPref("cui-showPowerBar", true);
                subTable.checkPref("cui-ShowResourceRate", false);

                subTable.checkPref("cui-ShowBlockInfo", true);
                subTable.checkPref("cui-ShowBlockHealth", true);
                subTable.checkPref("cui-showFactoryProgress", true);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->blocks[0]).growX().row();

            table.row();
            boolean[] advanceHudShown = {false};
            table.button("@setting.cui-advance-category.name", Icon.settings, Styles.togglet, () -> advanceHudShown[0] = !advanceHudShown[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> advanceHudShown[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.sliderPref("cui-buttonSize", 40, 1, 100, String::valueOf);
                subTable.sliderPref("cui-unitsPlayerTableUpdateRate", 10, 1, 100, String::valueOf);
                subTable.sliderPref("cui-unitsPlayerTableSize", 6, 1, 20, String::valueOf);
                subTable.checkPref("cui-hideWithMenus", true);
                subTable.sliderPref("cui-maxZoom", 10, 1, 10, String::valueOf);
                subTable.checkPref("cui-animateSettings", true);
                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->advanceHudShown[0]).growX().row();

            settings.getBoolOnce("cui-firstLoad", CuiSettingsDialog::firstLoadReset);
            table.button(bundle.get("settings.cui-reset", "Reset [scarlet]ALL[] to Defaults"), Icon.warning, () -> allCuiOptions.forEach(a ->{
                for(Setting setting : a.getSettings()){
                    if(setting.name == null || setting.title == null) continue;
                    settings.remove(setting.name);
                }

                Log.info("All Caster User Interface settings rested [ ;; m ;;]");
            })).margin(14).width(260f).pad(6);
        });

    }


    public static void firstLoadReset(){
        /* Bandied fix to the player-unit table's control buttons having a weird size when not rest at first time loading the mod*/
        Log.info("Caster User Interface Loaded for 1st time! hope you enjoy it >w< ");
        allCuiOptions.forEach(a ->{
            for(Setting setting : a.getSettings()){
                if(setting.name == null || setting.title == null) continue;
                settings.remove(setting.name);
            }
        });
    }

}
