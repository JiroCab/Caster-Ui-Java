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
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting;
import mindustry.world.blocks.storage.CoreBlock;

import java.text.DecimalFormat;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CuiSettingsDialog {
    public static Seq<SettingsMenuDialog.SettingsTable> allCuiOptions = new Seq<>();
    public static ObjectSet<UnitType>hiddenUnits = new ObjectSet<>(), coreUnitsTypes = new ObjectSet<>();
    public static float commonHeight = 60f;
    static DecimalFormat decFor = new DecimalFormat("#.##");
    
    public static void buildCategory(){
        setDefults(hiddenUnits, false);
        setDefults(coreUnitsTypes, true);

        ui.settings.addCategory("@settings.cui.settings", Icon.logic, table -> {
            //Dropped the `eui` (extended-ui) prefix for `cui` (caster-ui) to avoid conflicts
            /*Legacy Features From the Original Caster-ui Fork */
            boolean[] players = {false};
            table.row(); //foo's
            table.button("@setting.cui-players-category.name", Icon.players, Styles.togglet, () -> players[0] = !players[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> players[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-ShowPlayerList", true);
                subTable.checkPref("cui-hideNoUnitPlayers", true);
                subTable.checkPref("cui-TrackPlayerCursor", true);
                subTable.checkPref("cui-ShowOwnCursor", false);
                subTable.sliderPref("cui-playerCursorStyle", 3, 0, 4, s -> s == 0 ? "@off" : bundle.get("cui-cursor" + s ));
                subTable.sliderPref("cui-playerLineStyle", 1, 0, 3, s -> s == 0 ? "@off" : bundle.get("cui-lines" + s ));
                subTable.sliderPref("cui-playerDrawNames", 1, 0, 2, s -> s == 0 ? "@off" : bundle.get("cui-name" + s ));
                subTable.sliderPref("cui-playerTrackAlpha", 7, 0, 10, s -> s  > 0 ? s != 10 ? s + "0%" : "100%" : "@off");
                subTable.sliderPref("cui-playerTrackedAlpha", 10, 0, 10, s -> s > 0 ? s != 10 ? s + "0%" : "100%" : "@off");
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
                subTable.checkPref("cui-AlertsHideWithUi", true);
                subTable.checkPref("cui-SendChatCoreLost", false);
                subTable.checkPref("cui-ShowAlertsCircles", true);
                subTable.sliderPref("cui-alertCircleSpeed", 12, 1 , 500, a -> (a*0.5f) + "x");
                subTable.sliderPref("cui-alertStyle", 1, 1 , 5, s -> bundle.get("cui-alert" + s ));
                subTable.checkPref("cui-alertReverseGrow", false);

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
                subTable.checkPref("cui-BlockInfoShortenItems", true);
                subTable.sliderPref("cui-showFactoryProgressStyle", 2, 1, 6, s -> s > 1 ? bundle.get("cui-factoryProgress" + s ) : "@off");
                subTable.sliderPref("cui-logicLineAlpha", 5, 1, 11, s ->  s == 1 ? "@off" : s != 11 ?  (s - 1) + "0%" : "100%");
                subTable.sliderPref("cui-rallyPointAlpha", 4, 1, 11,s -> s == 1 ? "@off" : s != 11 ? (s - 1) + "0%" : "100%");

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->blocks[0]).growX().row();
            table.row();

            boolean[] teams = {false};
            table.button("@setting.cui-teams-category.name", Icon.modePvp , Styles.togglet, () -> teams[0] = !teams[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> teams[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-ShowTeamItems", true);
                subTable.checkPref("cui-TeamItemsShortenItems", true);
                subTable.sliderPref("cui-TeamItemsAlpha", 8, 0, 10, s -> s  > 0 ? s != 10 ? s + "0%" : "100%" : "@off");


                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->teams[0]).growX().row();

            boolean[] inputs = {false};
            table.button("@setting.cui-input-category.name", Icon.down , Styles.togglet, () -> inputs[0] = !inputs[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> inputs[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.sliderPref("cui-maxZoom", 12, 0, 125, s -> {
                    float f = s * 0.5f;
                    if(s == 0) f = 0.01f;
                    renderer.maxZoom = f;
                    return decFor.format(f) + "x";
                }); //Nearly every ui mod changes this lmao
                subTable.sliderPref("cui-minZoom", 3, 1, 125, s -> {
                    float f = s * 0.5f;
                    renderer.minZoom = f;
                    return decFor.format(f) + "x";
                });
                subTable.checkPref("cui-minimalCursor", false);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->inputs[0]).growX().row();

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
                subTable.sliderPref("cui-blockinfostyle", 2, 0 , 9, s -> bundle.get("cui-blockinfostyle-s" + s ));
                subTable.sliderPref("cui-TeamItemsUpdateRate", 2, 1, 3, s -> s == 1 ? "Fast" :  s == 2 ? "Normal" : "Slow");

                subTable.sliderPref("cui-buttonSize", 40, 1, 100, String::valueOf);
                subTable.sliderPref("cui-unitsPlayerTableUpdateRate", 10, 1, 100, String::valueOf);

                subTable.checkPref("cui-animateSettings", true);

                subTable.checkPref("cui-playerunitstablecontols", true);
                subTable.sliderPref("cui-playerunitstablestyle", 0, 0 , 9, s -> bundle.get("cui-blockinfostyle-s" + s ));
                subTable.sliderPref("cui-unitsPlayerTableSize", 6, 1, 20, String::valueOf);

                subTable.sliderPref("cui-blockinfoSide", 7, 0, 8, s -> bundle.get("cui-side"+s));
                subTable.sliderPref("cui-PlayerUnitsTableSide", 1, 0, 8, s -> bundle.get("cui-side"+s));
                subTable.sliderPref("cui-TeamItemsSide", 8, 0, 8, s -> bundle.get("cui-side"+s));
                allCuiOptions.add(subTable);
                t.add(subTable);
            }, settings.getBool("cui-animateSettings") , () ->advanceHudShown[0]).growX().row();

            settings.getBoolOnce("cui-firstLoad", CuiSettingsDialog::firstLoadReset);
            if(settings.getBool("cui-showFactoryProgress")) settings.remove("cui-showFactoryProgress");


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
