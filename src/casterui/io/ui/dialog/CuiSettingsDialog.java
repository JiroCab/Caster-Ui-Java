package casterui.io.ui.dialog;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import casterui.*;
import casterui.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;

import java.text.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CuiSettingsDialog {
    public static Seq<SettingsMenuDialog.SettingsTable> allCuiOptions = new Seq<>();
    public static ObjectSet<UnitType>hiddenUnits = new ObjectSet<>(), coreUnitsTypes = new ObjectSet<>();
    public static float commonHeight = 60f;
    private static final int cursorStyles = 8, offsetMinMax  = 200;
    static DecimalFormat decFor = new DecimalFormat("#.##");

    //Foo's complaint categories
    public static class CollapserSetting extends SettingsMenuDialog.SettingsTable.Setting{
        int type = -1;
        public CollapserSetting (String name){
            super(name);
        }
        public CollapserSetting (String name, int type){
            super(name);
            this.type = type;
        }

        public void add(SettingsMenuDialog.SettingsTable table) {
            switch (type) {
                case 0 -> trackingCategory(table);
                case 1 -> counterCategory(table);
                case 2 -> alertsCategory(table);
                case 3 -> blocksCategory(table);
                case 4 -> teamsCategory(table);
                case 5 -> inputCategory(table);
                case 6 -> categoryDivider(table);
                case 7 -> {
                    table.image().color(Pal.accent).height(3f).padTop(10).growX().row();
                    table.table(tab -> {
                        tab.button(bundle.get("settings.cui-reset"), Icon.warning, () -> {
                            restAllSettings();
                            Log.info("All Caster User Interface settings rested [ ;; m ;;]");
                        }).margin(14).width(260f).pad(6);
                        tab.button(bundle.get("settings.cui-preset"), Icon.export, CuiSettingsDialog::showPreset).margin(14).width(260f).pad(6);
                    }).growX().row();
                }
                case 8 -> unitsCategory(table);
                case 9 -> dominationSubCategory(table);
                case 10 -> updateHeader(table);
                default -> advanceCategory(table);
            }
        }
        
        public void trackingCategory(SettingsMenuDialog.SettingsTable table){
            boolean[] tracking = {false};
            table.button("@setting.cui-tracker-category.name", Icon.players, Styles.togglet, () -> tracking[0] = !tracking[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> tracking[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-TrackPlayerCursor", true);
                subTable.checkPref("cui-ShowOwnCursor", false);
                subTable.checkPref("cui-playerHoldTrackMouse", true);
                subTable.sliderPref("cui-playerCursorStyle", 3, 0, cursorStyles, s -> s == 0 ? "@off" : bundle.get("cui-cursor" + s ));
                subTable.sliderPref("cui-playerLineStyle", 1, 0, 3, s -> s == 0 ? "@off" : bundle.get("cui-lines" + s ));
                subTable.sliderPref("cui-playerDrawNames", 1, 0, 2, s -> s == 0 ? "@off" : bundle.get("cui-name" + s ));
                subTable.sliderPref("cui-playerTrackAlpha", 7, 0, 10, s -> s  > 0 ? s != 10 ? s + "0%" : "100%" : "@off");
                subTable.sliderPref("cui-playerTrackedAlpha", 10, 0, 10, s -> s > 0 ? s != 10 ? s + "0%" : "100%" : "@off");
                subTable.sliderPref("cui-playerIconSize", 35, 1, 100, String::valueOf);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, CuiVars.animateCats , () ->tracking[0]).growX().row();
        }

        public void counterCategory(SettingsMenuDialog.SettingsTable table) {
            boolean[] counter = {false};
            table.button("@setting.cui-counter-category.name", Icon.menu, Styles.togglet, () -> counter[0] = !counter[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> counter[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                //TODO: SAVE THESE AND LOAD THEM
                t.button(bundle.get("cui-hiddenUnits"), Icon.eyeOffSmall,() -> showBanned(bundle.get("cui-hiddenUnits"), hiddenUnits)).tooltip(bundle.get("cui-hiddenInfo")).center().width(400f).top().row();
                t.button(bundle.get("cui-hiddenCoreUnits"), Icon.cancel,() -> showBanned(bundle.get("cui-hiddenCoreUnits"), coreUnitsTypes, true)).tooltip(bundle.get("cui-hiddenCoreInfo")).center().width(400f).top().row();
                t.button(bundle.get("cui-filterTeams"), Icon.eraserSmall,() ->{
                    TeamBlackListerDialog teamBlackListerDialog = new TeamBlackListerDialog();
                    teamBlackListerDialog.show(1);
                }).marginLeft(14f).width(400f).scaling(Scaling.bounded).row();

                //Counter table
                subTable.checkPref("cui-ShowUnitTable", true);
                subTable.checkPref("cui-ShowPlayerList", true);
                subTable.checkPref("cui-hideNoUnitPlayers", true);
                subTable.sliderPref("cui-unitsPlayerTableStyle", 0, 0, 1, s -> bundle.get("cui-unitsplayer-style" + s));
                subTable.sliderPref("cui-unitsIconSize", 32, 1, 100, String::valueOf);
                subTable.checkPref("cui-unitFlagCoreUnitHides", true);
                subTable.checkPref("cui-separateTeamsUnit", true);
                subTable.checkPref("cui-teamtotalunitcount", true);
                subTable.sliderPref("cui-unitsPlayerTableSize", 6, 1, 20, String::valueOf);
                subTable.checkPref("cui-playerunitstablecontols", false);
                subTable.sliderPref("cui-buttonSize", 40, 1, 100, String::valueOf);

                subTable.pref(new CollapserSetting("cui-offset-div", 6));
                subTable.sliderPref("cui-PlayerUnitsTableSide", 1, 0, 8, s -> bundle.get("cui-side"+s));
                subTable.sliderPref("cui-playerunitstablestyle", 0, 0 , 9, s -> bundle.get("cui-blockinfostyle-s" + s ));
                subTable.sliderPref("cui-playerunitstables-x", 0, -offsetMinMax , offsetMinMax, String::valueOf);
                subTable.sliderPref("cui-playerunitstables-y", 0, -offsetMinMax , offsetMinMax, String::valueOf);
//                subTable.checkPref("cui-playerunitstables-x-abs", false);
//                subTable.checkPref("cui-playerunitstables-y-abs", false);

                allCuiOptions.add(subTable);

                t.add(subTable).row();
            }, CuiVars.animateCats , () ->counter[0]).growX().row();
        }

        public void unitsCategory(SettingsMenuDialog.SettingsTable table) {
            boolean[] units = {false};
            table.button("@setting.cui-units-category.name", Icon.units, Styles.togglet, () -> units[0] = !units[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> units[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                //hp bars
                subTable.sliderPref("cui-showUnitBarStyle", 7, 0, 9, s -> s == 0 ? "@off" :bundle.get("cui-unitshealtbar-style" + s));
                subTable.sliderPref("cui-showUnitBarSize", 4, 0, 100, s -> s == 0 ? "@off" : decFor.format(s * 0.25f));
                subTable.sliderPref("cui-showUnitBarAlpha", 10, 1, 10, s ->  s + "0%");
                subTable.sliderPref("cui-showUnitTextStyle", 1, 0, 3, s -> s == 0 ? "@off" :bundle.get("cui-unittext-style" + s));

                subTable.pref(new CollapserSetting("cui-category-div", 6));
                //cmd preview
                subTable.sliderPref("cui-unitscommands", 1, 0, 3, s -> s == 0 ? "@off" : bundle.get("cui-cmd-limits" + s));
                subTable.sliderPref("cui-unitCmdRange", 6, 1, 40, s -> decFor.format( s* 0.5) + " " + bundle.get("unit.blocks"));
                subTable.sliderPref("cui-unitCmdTrans", 10, 1, 10, s ->  s + "0%");
                subTable.sliderPref("cui-unitCmdStyle", 1, 0, 3, s -> s == 0 ? "@off" : bundle.get("cui-lines" + s ));
                subTable.sliderPref("cui-unitCmdPointer", 3, 0, cursorStyles, s -> s == 0 ? "@off" : bundle.get("cui-cursor" + s ));
                subTable.checkPref("cui-unitCmdNonMv", true);
                allCuiOptions.add(subTable);
                t.add(subTable);
            }, CuiVars.animateCats , () ->units[0]).growX().row();
        }

        public void alertsCategory(SettingsMenuDialog.SettingsTable table) {
            boolean[] alerts = {false};
            table.button("@setting.cui-alerts-category.name", Icon.chat, Styles.togglet, () -> alerts[0] = !alerts[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> alerts[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.checkPref("cui-ShowAlerts", true);
                subTable.checkPref("cui-AlertsUseBottom", true);
                subTable.checkPref("cui-AlertsHideWithUi", true);
                subTable.checkPref("cui-SendChatCoreLost", false);
                subTable.checkPref("cui-alertsOtherPlayers", false);
                subTable.checkPref("cui-ShowAlertsCircles", true);
                subTable.sliderPref("cui-alertCircleSpeed", 12, 1 , 500, a -> (a*0.5f) + "x");
                subTable.sliderPref("cui-alertStyle", 1, 1 , 5, s -> bundle.get("cui-alert" + s ));
                subTable.checkPref("cui-alertReverseGrow", false);

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, CuiVars.animateCats , () ->alerts[0]).growX().row();
        }

        public void blocksCategory(SettingsMenuDialog.SettingsTable table){
            boolean[] blocks = {false};
            table.button("@setting.cui-blocks-category.name", Icon.effect, Styles.togglet, () -> blocks[0] = !blocks[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> blocks[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-ShowBlockInfo", true);
                subTable.checkPref("cui-ShowBlockHealth", true);
                subTable.checkPref("cui-BlockInfoShortenItems", true);
                subTable.checkPref("cui-BlockInfoLastPlayer", true);

                subTable.pref(new CollapserSetting("cui-offset-div", 6));
                subTable.sliderPref("cui-blockinfoSide", 7, 0, 8, s -> bundle.get("cui-side"+s));
                subTable.sliderPref("cui-blockinfo-x", 0, -offsetMinMax , offsetMinMax, String::valueOf);
                subTable.sliderPref("cui-blockinfo-y", 0, -offsetMinMax , offsetMinMax, String::valueOf);
//                subTable.checkPref("cui-blockinfo-x-abs", false);
//                subTable.checkPref("cui-blockinfo-y-abs", false);

                subTable.pref(new CollapserSetting("cui-cat-div-bars", 6));

                subTable.sliderPref("cui-showFactoryProgressStyle", 2, 1, 6, s -> s > 1 ? bundle.get("cui-factoryProgress" + s ) : "@off");
                subTable.sliderPref("cui-rallyPointAlpha", 4, 1, 11,s -> s == 1 ? "@off" : s != 11 ? (s - 1) + "0%" : "100%");
                subTable.sliderPref("cui-blockinfostyle", 2, 0 , 9, s -> bundle.get("cui-blockinfostyle-s" + s ));

                subTable.pref(new CollapserSetting("cui-logic-bars", 6));
                subTable.sliderPref("cui-logicLineAlpha", 5, 1, 11, s ->  s == 1 ? "@off" : s != 11 ?  (s - 1) + "0%" : "100%");
                subTable.sliderPref("cui-logicLineLimit", 0, 1, 3,  s -> s == 0 ? "@off" : bundle.get("cui-cmd-limits" + s));
                subTable.sliderPref("cui-logicLineRange", 6, 1, 40, s -> decFor.format( s* 0.5) + " " + bundle.get("unit.blocks"));
                //subTable.checkPref("cui-showPowerBar", true);
                //subTable.checkPref("cui-ShowResourceRate", false); // TODO

                allCuiOptions.add(subTable);
                t.add(subTable);
            }, CuiVars.animateCats , () ->blocks[0]).growX().row();
        }

        public void teamsCategory(SettingsMenuDialog.SettingsTable table){
            boolean[] teams = {false};
            table.button("@setting.cui-teams-category.name", Icon.modePvp , Styles.togglet, () -> teams[0] = !teams[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> teams[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-ShowTeamItems", true);
                subTable.checkPref("cui-TeamItemsShortenItems", true);
                subTable.sliderPref("cui-TeamItemsAlpha", 8, 0, 10, s -> s  > 0 ? s != 10 ? s + "0%" : "100%" : "@off");
                subTable.sliderPref("cui-TeamItemsRow", 6, 0, 10, s -> s +1 + "");

                subTable.pref(new CollapserSetting("cui-offset-div", 6));
                subTable.sliderPref("cui-TeamItemsSide", 8, 0, 8, s -> bundle.get("cui-side"+s));
                subTable.sliderPref("cui-TeamItems-x", 0, -offsetMinMax , offsetMinMax, String::valueOf );
                subTable.sliderPref("cui-TeamItems-y", 0, -offsetMinMax , offsetMinMax, String::valueOf);
//                subTable.checkPref("cui-TeamItems-x-abs", false);
//                subTable.checkPref("cui-TeamItems-y-abs", false);

                subTable.pref(new CollapserSetting("cui-cat-div-counter", 6));
                subTable.checkPref("cui-domination-toggle", false);
                subTable.checkPref("cui-domination-vertical", false);
                subTable.checkPref("cui-domination-TeamIcons", true);
                subTable.sliderPref("cui-domination-trans", 8, 0, 10, s -> s  > 0 ? s != 10 ? s + "0%" : "100%" : "@off");
                subTable.pref(new CollapserSetting("cui-domination-more", 9));

                subTable.sliderPref("cui-domination-side", 0, 0, 8, s -> bundle.get("cui-side"+s));
                subTable.sliderPref("cui-domination-x", 0, -offsetMinMax , offsetMinMax, String::valueOf);
                subTable.sliderPref("cui-domination-y", 0, -offsetMinMax , offsetMinMax, String::valueOf);

                t.button(bundle.get("cui-filterTeams"), Icon.eraserSmall,() ->{
                    TeamBlackListerDialog teamBlackListerDialog = new TeamBlackListerDialog();
                    teamBlackListerDialog.show(2);
                }).marginLeft(14f).width(400f).scaling(Scaling.bounded).row();
                allCuiOptions.add(subTable);
                t.add(subTable);
            }, CuiVars.animateCats , () ->teams[0]).growX().row();
        }

        public void inputCategory(SettingsMenuDialog.SettingsTable table){
            boolean[] inputs = {false};
            table.button("@setting.cui-input-category.name", Icon.down , Styles.togglet, () -> inputs[0] = !inputs[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> inputs[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();

                subTable.checkPref("cui-respectCommandMode", true);
                subTable.checkPref("cui-respectTyping", false);
                subTable.checkPref("cui-respectLockInputs", true);
                subTable.checkPref("cui-respectDialog", true);
                subTable.checkPref("cui-hideWithMenus", true);
                subTable.checkPref("cui-auto-toggle-menu", true);

                subTable.pref(new CollapserSetting("cui-category-div", 6));

                subTable.sliderPref("cui-maxZoom", 12, 0, 125, s -> {
                    if(s == 12) return "@off";
                    float f = s * 0.5f;
                    if(s == 0) f = 0.01f;
                    renderer.maxZoom = f;
                    return decFor.format(f) + "x";
                }); //Nearly every ui mod changes this lmao
                subTable.sliderPref("cui-minZoom", 3, 1, 125, s -> {
                    if(s == 3) return "@off";
                    float f = s * 0.5f;
                    renderer.minZoom = f;
                    return decFor.format(f) + "x";
                });
                subTable.checkPref("cui-minimalCursor", false);

                allCuiOptions.add(subTable);
                t.button(bundle.get("keybind.title"), Icon.move,() -> CuiVars.rebindDialog.show()).tooltip(bundle.get("cui-hiddenInfo")).center().width(400f).top().row();
                t.add(subTable);
            }, CuiVars.animateCats , () ->inputs[0]).growX().row();
        }

        public void categoryDivider(SettingsMenuDialog.SettingsTable table){
            table.image().color(Color.lightGray).height(3f).padTop(5).padBottom(5).growX().row();
        }

        public void advanceCategory(SettingsMenuDialog.SettingsTable table){
            boolean[] advanceHudShown = {false};
            table.button("@setting.cui-advance-category.name", Icon.settings, Styles.togglet, () -> advanceHudShown[0] = !advanceHudShown[0]).marginLeft(14f).growX().height(commonHeight).checked(a -> advanceHudShown[0]).padTop(5f).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.sliderPref("cui-unitsPlayerTableUpdateRate", 10, 1, 100, String::valueOf);
                subTable.sliderPref("cui-TeamItemsUpdateRate", 2, 1, 3, s -> s == 1 ? "Fast" :  s == 2 ? "Normal" : "Slow");
                subTable.checkPref("cui-animateSettings", true);

                allCuiOptions.add(subTable);
                t.button("@cui-rebuild", Icon.export, () -> {
                    CuiVars.postInt();

                    for (SettingsMenuDialog.SettingsTable a : allCuiOptions) a.rebuild();
                }).center().width(400f).top().tooltip(bundle.get("cui-rebuild-info")).row();
                t.add(subTable).row();
            }, CuiVars.animateCats , () ->advanceHudShown[0]).growX().row();
        }

        public void dominationSubCategory(SettingsMenuDialog.SettingsTable table){
            boolean[] dominactionShown = {false};
            table.button(bundle.get("cui-filterTeams"), Icon.eraserSmall,() ->{
                TeamBlackListerDialog teamBlackListerDialog = new TeamBlackListerDialog();
                teamBlackListerDialog.show(0);
            }).marginLeft(14f).width(400f).scaling(Scaling.bounded).row();
            table.button("@setting.cui-domination-category.name", Icon.export, Styles.togglet, () -> dominactionShown[0] = !dominactionShown[0]).marginLeft(14f).width(400f).height(commonHeight).checked(a -> dominactionShown[0]).padTop(5f).scaling(Scaling.bounded).row();
            table.collapser( t -> {
                SettingsMenuDialog.SettingsTable subTable = new SettingsMenuDialog.SettingsTable();
                subTable.checkPref("cui-domination-totals", false);
                subTable.checkPref("cui-domination-raw", false);
                subTable.checkPref("cui-domination-core", false);
                subTable.checkPref("cui-domination-percent", false);

                subTable.pref(new CollapserSetting("cui-offset-div", 6));

                subTable.checkPref("cui-domination-turret", false);
                subTable.checkPref("cui-domination-production", false);
                subTable.checkPref("cui-domination-distribution", false);
                subTable.checkPref("cui-domination-liquid", false);
                subTable.checkPref("cui-domination-power", false);
                subTable.checkPref("cui-domination-defence", false);
                subTable.checkPref("cui-domination-crafting", false);
                subTable.checkPref("cui-domination-units", false);
                subTable.checkPref("cui-domination-effect", false);
                subTable.checkPref("cui-domination-logic", false);

                allCuiOptions.add(subTable);
                t.add(subTable).row();
            }, CuiVars.animateCats , () ->dominactionShown[0]).growX().row();
        }

        public void updateHeader(SettingsMenuDialog.SettingsTable table){
            table.add(CuiVars.updateCheckTable).growX().center().row();
        }
    }


    private static void showPreset() {
        BaseDialog bd = new BaseDialog("cui-preset-dialog");
        bd.addCloseButton();

        bd.cont.setOrigin(Align.center);
        Label disclaimer = new Label(Core.bundle.get("settings.cui-preset.disclaimer"));
        disclaimer.setAlignment(Align.center);
        disclaimer.setWrap(true);

        bd.cont.add(disclaimer).row();
        bd.cont.image(Tex.whiteui, Pal.accent).fillX().height(3f).pad(4f).row();

        bd.cont.pane(t -> {
            t.defaults().size(220f, 55f).pad(3f);
            t.button("@cui-preset1", Styles.defaultt, () -> {
                restAllSettings();
                settings.put("cui-playerunitstablecontols", true);
                settings.put("cui-playerunitstables-y", -80);
                settings.put("cui-PlayerUnitsTableSide", 4);
                settings.put("cui-teamtotalunitcount", false);
                settings.put("cui-TeamItemsSide", 3);
                settings.put("cui-blockinfoSide", 1);
                settings.put("cui-TrackPlayerCursor", false);
                settings.put("cui-playerCursorStyle", 0);
                settings.put("cui-ShowPlayerList", false);
                settings.put("cui-unitscommands", 0);
                settings.put("cui-showUnitTextStyle", 0);
                settings.put("cui-showUnitBarStyle", 8);
                bd.hide();
                Log.info("Caster-ui present 1 loaded!");
            });
        });
        bd.show();
    }

    private static void restAllSettings(){
        for (SettingsMenuDialog.SettingsTable a : allCuiOptions) {
            for (Setting setting : a.getSettings()) {
                if (setting.name == null || setting.title == null) continue;
                settings.remove(setting.name);
                hiddenUnits.clear();
                coreUnitsTypes.clear();
                setDefaults(hiddenUnits, false);
                setDefaults(coreUnitsTypes, true);
            }
            a.rebuild();
        }
    }


    public static void buildCategory(){
        setDefaults(hiddenUnits, false);
        setDefaults(coreUnitsTypes, true);

        ui.settings.addCategory("@settings.cui.settings", Icon.logic, table -> {
            table.pref(new CollapserSetting("cui-update-header", 10));
            table.pref(new CollapserSetting("cui-category-trackers", 0));
            table.pref(new CollapserSetting("cui-category-counter", 1));
            table.pref(new CollapserSetting("cui-category-alerts", 2));
            table.pref(new CollapserSetting("cui-category-units", 8));
            table.pref(new CollapserSetting("cui-category-blocks", 3));
            table.pref(new CollapserSetting("cui-category-teams", 4));
            table.pref(new CollapserSetting("cui-category-inputs", 5));
            table.pref(new CollapserSetting("cui-category-advance"));
            table.pref(new CollapserSetting("cui-category-reset-all", 7));
            settings.getBoolOnce("cui-firstLoad", CuiSettingsDialog::firstLoadReset);
            if(settings.getBool("cui-showFactoryProgress")) settings.remove("cui-showFactoryProgress");
            table.checkPref("cui-killswitch", false);

        });

    }


    public static void firstLoadReset(){
        /* Bandied fix to the player-unit table's control buttons having a weird size when not rest at first time loading the mod*/
        Log.info("Caster User Interface Loaded for 1st time! hope you enjoy it >w< ");
        for (SettingsMenuDialog.SettingsTable a : allCuiOptions) {
            for (Setting setting : a.getSettings()) {
                if (setting.name == null || setting.title == null) continue;
                settings.remove(setting.name);
            }
        }
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
                        String mod = b.isModded() ? "\n(" + b.minfo.mod.meta.displayName + ")": "";
                        t.button(new TextureRegionDrawable(b.uiIcon), Styles.flati, iconMed, () -> {
                            set.add(b);
                            rebuild[0].run();
                            dialog.hide();
                        }).tooltip(b.localizedName + mod).size(60f);

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
            setDefaults(set, showCoreUnits);
            rebuild[0].run();
        }).size(180, 64f);

        bd.buttons.button("@clear", Icon.trash, () -> {
            set.clear();
            rebuild[0].run();
        }).size(120, 64f);

        bd.show();
    }

    public static void setDefaults(ObjectSet<UnitType> set, boolean showCoreUnits){
        for (UnitType m : content.units()) {
            if (m.isHidden()) set.add(m);
            if (showCoreUnits) {
                for (Block b : content.blocks()) {
                    if (b instanceof CoreBlock && ((CoreBlock) b).unitType == m && !set.contains(m)) set.add(m);
                }
            }
        }
    }


}
