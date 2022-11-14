package Core.Resources;

import Core.Resources.components.ConfigUtils;
import Core.Resources.components.ResourceUtils;
import arc.util.Log;
import net.dv8tion.jda.api.JDABuilder;

import static Core.Resources.Vars.*;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;
import static net.dv8tion.jda.api.requests.RestAction.setDefaultFailure;

public class Main {

    public static void main(String[] args) {
        cache.delete();

        dataDirectory.mkdirs();
        cache.mkdirs();
        resources.mkdirs();
        sprites.mkdirs();

        ConfigUtils.init();
        ResourceUtils.init();

        setDefaultFailure(null);

        try {
            jda = JDABuilder.createLight(config.token)
                    .enableIntents(GUILD_MEMBERS, MESSAGE_CONTENT)
                    .addEventListeners(new Listener())
                    .build()
                    .awaitReady();

            Log.info(1);

        } catch (Exception e) {
            Log.err("Failed to launch Community Bot. Make sure the provided token and guild/channel IDs in the configuration are correct.");
            Log.err(e);
        }

        Listener.loadCommands(config.prefix);
    }
}