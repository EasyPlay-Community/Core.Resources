package cbot.components;

import arc.util.Log;
import arc.util.serialization.JsonWriter;
import cbot.Vars;

public class ConfigUtils {

    public static void init() {
        Vars.json.setOutputType(JsonWriter.OutputType.json);
        Vars.json.setUsePrototypes(false);

        var file = Vars.dataDirectory.child("config.json");
        if (file.exists()) {
            Vars.config = Vars.json.fromJson(Config.class, file.reader());
            Log.info("Config loaded. (@)", file.absolutePath());
        } else {
            file.writeString(Vars.json.toJson(Vars.config = new Config()));
            Log.info("Config file generated. (@)", file.absolutePath());
            System.exit(0);
        }
    }

    public static class Config {
        public String token = "token";
        public String prefix = "!";
        public long schematicsForumId = 0L;
        public long mapsForumId = 0L;
        public long modsForumId = 0L;
        public long artsForumId = 0L;
        public long botChannelId = 0L;
        public long mindustryCategory = 0L;
    }
}