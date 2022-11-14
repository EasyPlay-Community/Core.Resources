package Core.Resources.components;

import Core.Resources.Vars;
import arc.util.Log;
import arc.util.serialization.JsonWriter;

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
        public long schematicsChannelId = 0L;
        public long mapsChannelId = 0L;
        public long modsChannelId = 0L;
        public long artsChannelId = 0L;
        public long botChannelId = 0L;
    }
}