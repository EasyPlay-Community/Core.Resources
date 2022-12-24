package cbot;

import arc.util.Log;
import cbot.components.ConfigUtils;
import cbot.components.ResourceUtils;
import net.dv8tion.jda.api.JDABuilder;

import static cbot.Vars.config;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;
import static net.dv8tion.jda.api.requests.RestAction.setDefaultFailure;

public class Main {

    public static void main(String[] args) {
        Vars.cache.delete();

        Vars.dataDirectory.mkdirs();
        Vars.cache.mkdirs();
        Vars.resources.mkdirs();
        Vars.sprites.mkdirs();

        ConfigUtils.init();
        ResourceUtils.init();

        setDefaultFailure(null);

        try {
            Vars.jda = JDABuilder.createLight(config.token)
                    .enableIntents(GUILD_MEMBERS, MESSAGE_CONTENT)
                    .addEventListeners(new Listener())
                    .build()
                    .awaitReady();


        } catch (Exception e) {
            Log.err("Failed to launch " +
                    "Bot. Make sure the provided token and guild/channel IDs in the configuration are correct.");
            Log.err(e);
        }

        Listener.loadCommands(config.prefix);
    }
}