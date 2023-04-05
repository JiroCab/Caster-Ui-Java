package casterui;

import casterui.io.CuiInputs;
import casterui.io.ui.*;
import casterui.io.ui.dialog.CuiSettingsDialog;
import mindustry.Vars;

public class CuiVars {
    public static CuiWorldRenderer renderer = new CuiWorldRenderer();
    public static CuiFragment1 fragment1 = new CuiFragment1();
    public static CuiFragment fragment = new CuiFragment();
    public static CuiInputs inputs = new CuiInputs();

    public static void init(){
        CuiSettingsDialog.buildCategory();
        renderer.worldRenderer();
        fragment.BuildTables(Vars.ui.hudGroup);
    }

    public static void postInt(){
        fragment.clearTables();
    }

    public static void update(){
        fragment.UpdateTables();
        inputs.update();
    }

}
