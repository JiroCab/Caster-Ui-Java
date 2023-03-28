package casterui.io.ui.dialog;

import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;

import static mindustry.Vars.ui;

public class CuiSettingsDialog {
    public static float commonWidth = 260f, commonHeight = 60f;
    
    public static void buildCategory(){

        ui.settings.addCategory("@settings.cui.settings", Icon.logic, table -> {
            //Dropped the `eui` (extended-ui) prefix for `cui` to avoid conflicts

            /*Legacy Features From the Original Caster-ui Fork */
            boolean[] hudShown = {false};
            table.button("@setting.cui-hud-settings.name", Icon.downOpen, Styles.togglet, () -> hudShown[0] = !hudShown[0]).marginLeft(14f).width(commonWidth).height(commonHeight).checked(a -> hudShown[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-ShowUnitTable", true);
                subTable.checkPref("cui-ShowPlayerList", true);
                subTable.checkPref("cui-hideNoUnitPlayers", false);

                subTable.checkPref("cui-showPowerBar", true);
                subTable.checkPref("cui-ShowResourceRate", false);

                subTable.checkPref("cui-ShowBlockInfo", true);
                subTable.checkPref("cui-ShowBlockHealth", true);
                t.add(subTable);
            }, () ->hudShown[0]).growX().row();
            
            boolean[] worldShown = {false};
            table.button("@setting.cui-world-settings.name", Icon.downOpen, Styles.togglet, () -> worldShown[0] = !worldShown[0]).marginLeft(14f).width(commonWidth).height(commonHeight).checked(a -> worldShown[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.sliderPref("cui-maxZoom", 10, 1, 10, String::valueOf);

                subTable.checkPref("cui-showFactoryProgress", true);
                subTable.checkPref("cui-showUnitBar", true);

                subTable.checkPref("cui-ShowAlerts", true);
                subTable.checkPref("cui-AlertsUseBottom", true);
                subTable.checkPref("cui-ShowAlertsCircles", true);
                subTable.checkPref("cui-TrackLogicControl", false);
                subTable.sliderPref("cui-logicLineAlpha", 7, 1, 10, String::valueOf);

                subTable.add("cui-players").color(Pal.accent).padTop(20).padRight(100f).padBottom(-3).row();
                subTable.image().color(Pal.accent).height(3f).padRight(100f).padBottom(20).row();
                subTable.checkPref("cui-SendChatCoreLost", false);
                subTable.checkPref("cui-TrackPlayerCursor", false);
                subTable.checkPref("cui-ShowOwnCursor", false);
                subTable.sliderPref("cui-playerCursorStyle", 7, 1, 7, String::valueOf);
                subTable.sliderPref("cui-playerTrackAlpha", 10, 1, 10, String::valueOf);
                t.add(subTable);
            }, () ->worldShown[0]).growX().row();
            
            table.row();
            boolean[] advanceHudShown = {false};
            table.button("@setting.cui-hud-settings-advance.name", Icon.downOpen, Styles.togglet, () -> advanceHudShown[0] = !advanceHudShown[0]).marginLeft(14f).width(commonWidth).height(commonHeight).checked(a -> advanceHudShown[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.sliderPref("cui-buttonSize", 35, 1, 100, String::valueOf);
                subTable.sliderPref("cui-unitsPlayerTableSize", 6, 1, 20, String::valueOf);
                subTable.sliderPref("cui-unitsPlayerTableUpdateRate", 40, 1, 100, String::valueOf);
                t.add(subTable);
            }, () ->advanceHudShown[0]).growX().row();
        });


    }

}
