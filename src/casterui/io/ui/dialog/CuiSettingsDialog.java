package casterui.io.ui.dialog;

import arc.Core;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ScrollPane;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import casterui.CuiVars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;
import mindustry.world.blocks.storage.CoreBlock;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CuiSettingsDialog {
    public static Seq<SettingsMenuDialog.SettingsTable> allCuiOptions = new Seq<>();
    public static ObjectSet<UnitType>hiddenUnits = new ObjectSet<>(), coreUnitsTypes = new ObjectSet<>();
    public static float commonHeight = 60f;
    
    public static void buildCategory(){
        setDefults(hiddenUnits, false);
        setDefults(coreUnitsTypes, true);

        ui.settings.addCategory("@settings.cui.settings", Icon.logic, table -> {
            //Dropped the `eui` (extended-ui) prefix for `cui` (caster-ui) to avoid conflicts
            /*Legacy Features From the Original Caster-ui Fork */
            boolean[] players = {false};
            table.button("@setting.cui-players-category.name", Icon.players, Styles.togglet, () -> players[0] = !players[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> players[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-ShowPlayerList", true);
                subTable.checkPref("cui-hideNoUnitPlayers", true);
                subTable.checkPref("cui-TrackPlayerCursor", true);
                subTable.checkPref("cui-ShowOwnCursor", false);
                subTable.sliderPref("cui-playerCursorStyle", 6, 1, 7, String::valueOf);
                subTable.sliderPref("cui-playerTrackAlpha", 8, 1, 10, s -> s * 0.1 + "%");
                subTable.sliderPref("cui-playerTrackedAlpha", 10, 1, 10, s -> s * 0.1 + "%");
                subTable.sliderPref("cui-playerIconSize", 35, 1, 100, String::valueOf);
                subTable.checkPref("cui-playerHoldTrackMouse", true);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->players[0]).growX().row();

            boolean[] units = {false};
            table.button("@setting.cui-units-category.name", Icon.units, Styles.togglet, () -> units[0] = !units[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> units[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                //TODO: SAVE THESE AND LOAD THEM 
                t.button(bundle.get("cui-hiddenUnits"), () -> showBanned(bundle.get("cui-hiddenUnits"), hiddenUnits)).tooltip(bundle.get("cui-hiddenInfo")).center().width(400f).top().row();
                t.button(bundle.get("cui-hiddenCoreUnits"), () -> showBanned(bundle.get("cui-hiddenCoreUnits"), coreUnitsTypes, true)).tooltip(bundle.get("cui-hiddenCoreInfo")).center().width(400f).top().row();

                subTable.checkPref("cui-ShowUnitTable", true);
                subTable.sliderPref("cui-unitsIconSize", 32, 1, 100, String::valueOf);
                subTable.checkPref("cui-separateTeamsUnit", true);
                subTable.checkPref("cui-showUnitBar", true);
                subTable.checkPref("cui-unitFlagCoreUnitHides", true);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->units[0]).growX().row();

            boolean[] alerts = {false};
            table.button("@setting.cui-alerts-category.name", Icon.chat, Styles.togglet, () -> alerts[0] = !alerts[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> alerts[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.checkPref("cui-ShowAlerts", true);
                subTable.checkPref("cui-AlertsUseBottom", true);
                subTable.checkPref("cui-SendChatCoreLost", false);
                subTable.checkPref("cui-ShowAlertsCircles", true);
                subTable.sliderPref("cui-alertCircleSpeed", 12, 1 , 500, a -> (a*0.5f) + "x");
                subTable.sliderPref("cui-alertStyle", 1, 1 , 5, String::valueOf);

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
                subTable.checkPref("cui-TrackLogicControl", false);
                subTable.sliderPref("cui-logicLineAlpha", 4, 1, 10, s -> s * 0.1 + "%");

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->blocks[0]).growX().row();
            table.row();

            boolean[] hotkeys = {false};
            table.button("@setting.cui-hotkeys-category.name", Icon.move, Styles.togglet, () -> hotkeys[0] = !hotkeys[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> hotkeys[0]).padTop(5f).row();
            table.collapser( t -> {
                t.button(bundle.get("keybind.title"), () -> CuiVars.rebindDialog.show()).tooltip(bundle.get("cui-hiddenInfo")).center().width(400f).top().row();

                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-respectCommandMode", true);
                subTable.checkPref("cui-respectTyping", true);
                subTable.checkPref("cui-respectLockInputs", true);
                subTable.checkPref("cui-respectDialog", true);
                subTable.checkPref("cui-hideWithMenus", true);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->hotkeys[0]).growX().row();

            table.row();
            boolean[] advanceHudShown = {false};
            table.button("@setting.cui-advance-category.name", Icon.settings, Styles.togglet, () -> advanceHudShown[0] = !advanceHudShown[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> advanceHudShown[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.sliderPref("cui-buttonSize", 40, 1, 100, String::valueOf);
                subTable.sliderPref("cui-unitsPlayerTableUpdateRate", 10, 1, 100, String::valueOf);
                subTable.sliderPref("cui-unitsPlayerTableSize", 6, 1, 20, String::valueOf);
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
                    hiddenUnits.clear();
                    coreUnitsTypes.clear();
                    setDefults(hiddenUnits, false);
                    setDefults(coreUnitsTypes, true);
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

    private static void showBanned(String title, ObjectSet<UnitType> set){
        showBanned(title, set, false);
    }

    private static void showBanned(String title, ObjectSet<UnitType> set, boolean showCoreUnits){
        BaseDialog bd = new BaseDialog(title);
        bd.addCloseButton();

        Runnable[] rebuild = {null};

        rebuild[0] = () -> {
            float previousScroll = bd.cont.getChildren().isEmpty() ? 0f : ((ScrollPane)bd.cont.getChildren().first()).getScrollY();
            bd.cont.clear();
            bd.cont.pane(t -> {
                t.margin(10f);

                if(set.isEmpty()){
                    t.add("@empty");
                }

                int cols = mobile && Core.graphics.isPortrait() ? 1 : mobile ? 2 : 3;
                int i = 0;

                for(UnitType con : set){
                    t.table(Tex.underline, b -> {
                        b.left().margin(4f);
                        b.image(con.uiIcon).scaling(Scaling.bounded).size(iconMed).padRight(3);
                        b.add(con.localizedName).color(Color.lightGray).padLeft(3).growX().left().wrap();

                        b.button(Icon.cancel, Styles.clearNonei, () -> {
                            set.remove(con);
                            rebuild[0].run();
                        }).size(70f).pad(-4f).padLeft(0f).scaling(Scaling.bounded);
                    }).size(300f, 70f).padRight(5);

                    if(++i % cols == 0){
                        t.row();
                    }
                }
            }).get().setScrollYForce(previousScroll);
            bd.cont.row();
            bd.cont.button("@add", Icon.add, () -> {
                BaseDialog dialog = new BaseDialog("@add");
                dialog.cont.pane(t -> {
                    t.left().margin(14f);
                    int[] i = {0};
                    int cols = mobile && Core.graphics.isPortrait() ? 4 : 12;
                    content.units().each(b -> !set.contains(b), b ->{
                        t.button(new TextureRegionDrawable(b.uiIcon), Styles.flati, iconMed, () -> {
                            set.add(b);
                            rebuild[0].run();
                            dialog.hide();
                        }).size(60f);

                        if(++i[0] % cols == 0){
                            t.row();
                        }
                    });
                });

                dialog.addCloseButton();
                dialog.show();
            }).size(300f, 64f);
        };

        bd.shown(rebuild[0]);
        bd.buttons.button("@addall", Icon.add, () -> {
            content.units().each(u -> {
                if(!set.contains(u))set.add(u);
            });
        rebuild[0].run();
        }).size(180, 64f);

        bd.buttons.button("@defaults", Icon.exit, () -> {
            set.clear();
            setDefults(set, showCoreUnits);
            rebuild[0].run();
        }).size(180, 64f);

        bd.buttons.button("@clear", Icon.trash, () -> {
            set.clear();
            rebuild[0].run();
        }).size(120, 64f);

        bd.show();
    }

    public static void setDefults(ObjectSet<UnitType> set, boolean showCoreUnits){
        content.units().forEach(m -> {
            if(m.isHidden())set.add(m);
            if(showCoreUnits){
                content.blocks().forEach(b ->{ if(b instanceof CoreBlock && ((CoreBlock) b).unitType == m && !set.contains(m))set.add(m);});
            }
        });
    }


}
