package casterui.util;

import arc.util.*;
import arc.util.serialization.*;
import casterui.*;
import mindustry.mod.Mods.*;

import static arc.Core.bundle;
import static mindustry.Vars.*;

//Stolen from: https://github.com/ApsZoldat/MindustryMappingUtilities/blob/main/src/java/mu/utils/UpdateChecker.java
public class CuiUpdateChecker{
    public boolean out = false;

    public void run(){
        LoadedMod mod = mods.getMod(CuiMain.class);
        String repo = mod.getRepo() != null ? mod.getRepo() : "JiroCab/Caster-Ui-Java";
        out = false;

        Http.get(ghApi + "/repos/" + repo + "/releases", res -> {
            var json = Jval.read(res.getResultAsString());
            Jval.JsonArray releases = json.asArray();

            if(releases.size == 0){
                Log.err("No CUI available for auto-updating 3:");
                return;
            }

            Jval release = releases.get(0);
            String modVersion = mod.meta.version;
            modVersion = (modVersion.contains(".") ? modVersion : modVersion + ".0");
            CuiVars.nextVersion = Strings.parseFloat(release.getString("tag_name").replace("v", ""));

            if(Strings.parseFloat(modVersion) >= CuiVars.nextVersion){
                Log.info("Cui is up to date! ^w^ (c" + modVersion + " r" + Strings.parseFloat(release.getString("tag_name").replace("v", "")) + ")");
                return;
            }
            out = true;
            Log.info(bundle.format("setting.cui-updateAvailable", CuiVars.nextVersion));


        }, thr -> Log.info("Cui failed to fetch updates qmq: @", thr));
    }

}
