package casterui.util;

import arc.struct.*;
import casterui.*;
import casterui.dialogs.*;
import casterui.modules.*;
import mindustry.*;
import rhino.*;

public class CuiClassMap{
    // stolen mostly from Zhttps://github.com/SMOLKEYS/yellow-java/blob/master/src/yellow/JSLink.java

    static boolean loaded = false;
    static ImporterTopLevel scope = null;

    //I have no idea how this is working lmao -rushie
    public static void load(String s){
        if(!loaded){
            scope = (ImporterTopLevel) Vars.mods.getScripts().scope;
        }

        Seq<NativeJavaPackage> out = new Seq<>();
        out.add(new NativeJavaPackage(s, Vars.mods.mainLoader()));
        out.add(new NativeJavaPackage(CuiVars.class.getPackage().getName(), Vars.mods.mainLoader()));
        out.add(new NativeJavaPackage(CuiJsonHandler.class.getPackage().getName(), Vars.mods.mainLoader()));
        out.add(new NativeJavaPackage(CuiHoverBlockInfo.class.getPackage().getName(), Vars.mods.mainLoader()));
        out.add(new NativeJavaPackage(CuiSettingsDialog.class.getPackage().getName(), Vars.mods.mainLoader()));
        for(NativeJavaPackage pkg : out){
            pkg.setParentScope(scope);
            scope.importPackage(pkg);
        }

    }

}
