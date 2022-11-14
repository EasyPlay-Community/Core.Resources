package Core.Resources;

import Core.Resources.components.ContentHandler;
import arc.func.Cons;
import arc.graphics.Color;
import arc.util.Log;
import arc.util.UnsafeRunnable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static Core.Resources.Vars.config;
import static Core.Resources.Vars.handler;
import static arc.graphics.Color.scarlet;
import static arc.util.Strings.format;
import static arc.util.Strings.getSimpleMessage;
import static mindustry.graphics.Pal.accent;
import static mindustry.world.meta.BlockFlag.reactor;
import static net.dv8tion.jda.api.utils.AttachedFile.fromData;


public class Listener extends ListenerAdapter {
    static ExecutorService executor = Executors.newSingleThreadExecutor();
    public static void loadCommands(String prefix) {
        Vars.handler.setPrefix(prefix);

        Vars.handler.<Message>register("help", "РЎРїРёСЃРѕРє РІСЃРµС… РєРѕРјР°РЅРґ.", (args, message) -> {
            var builder = new StringBuilder();
            Vars.handler.getCommandList().each(command -> builder.append(prefix).append("**").append(command.text).append("**").append(command.paramText).append(" - ").append(command.description).append("\n"));
            reply(message, ":newspaper: РЎРїРёСЃРѕРє РІСЃРµС… РєРѕРјР°РЅРґ:", builder.toString(), accent);
        });
    }

    private static void reply(Message message, String title, String description, Color color) {
        message.replyEmbeds(new EmbedBuilder().setTitle(title).setDescription(description).setColor(color.argb8888()).build()).queue();
    }

    private static void tryWorkWithFile(File file, UnsafeRunnable runnable, Cons<Throwable> error) {
        try {
            runnable.run();
        } catch (Throwable t) {
            error.get(t);
        } finally {
            file.deleteOnExit();
        }
    }

    static void mapParser(Message message, Message.Attachment attachment) {

        executor.submit(() -> {

            attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
                Log.info(attachment.getFileName());
                var map = ContentHandler.parseMap(file);
                var image = ContentHandler.parseMapImage(map);

                var embed = new EmbedBuilder()
                        .setTitle(map.name())
                        .setDescription(map.description())
                        .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                        .setFooter(map.width + "x" + map.height)
                        .setColor(accent.argb8888())
                        .setImage("attachment://image.png");

                var channel = Vars.jda.getTextChannelById(Vars.config.mapsChannelId);

                channel.sendMessageEmbeds(embed.build()).addFiles(fromData(image, "image.png"), fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":map: РЈСЃРїРµС€РЅРѕ", "РљР°СЂС‚Р° РѕС‚РїСЂР°РІР»РµРЅР° РІ " + channel.getAsMention(), accent));
            }, t -> {
                t.printStackTrace();
                reply(message, ":warning: РћС€РёР±РєР°", getSimpleMessage(t), scarlet);
            }));
        });
    }


    static void schemParser(Message message, Message.Attachment attachment) {

        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            Log.info(attachment.getFileName());
            var schematic = ContentHandler.parseSchematic(file);
            var image = ContentHandler.parseSchematicImage(schematic);

            var builder = new StringBuilder();
            schematic.requirements().each((item, amount) -> builder.append(Vars.emojis.containsKey(item) ? format("<:@:@>", item.name.replaceAll("-", ""), Vars.emojis.get(item)) : item.name).append(" ").append(amount).append(" "));

            var embed = new EmbedBuilder()
                    .setTitle(schematic.name())
                    .setDescription(schematic.description())
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .addField("Requirements", builder.toString(), true)
                    .setFooter(schematic.width + "x" + schematic.height + ", " + schematic.tiles.size + " blocks")
                    .setColor(accent.argb8888())
                    .setImage("attachment://image.png");

            var channel = Vars.jda.getTextChannelById(Vars.config.schematicsChannelId);

            channel.sendMessageEmbeds(embed.build()).addFiles(fromData(image, "image.png"), fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":wrench: РЈСЃРїРµС€РЅРѕ", "РЎС…РµРјР° РѕС‚РїСЂР°РІР»РµРЅР° РІ " + channel.getAsMention(), accent));
        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: РћС€РёР±РєР°", getSimpleMessage(t), scarlet);
        }));

    }

    public static void modParser(Message message, Message.Attachment attachment) {
        Log.info(attachment.getFileName());
        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            String footer = message.getContentRaw().substring(5);
            var channel = Vars.jda.getTextChannelById(Vars.config.modsChannelId);


            var embed = new EmbedBuilder()
                    .setTitle(attachment.getFileName().replace(".zip", ""))
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .setFooter(footer)
                    .setColor(java.awt.Color.decode("#00FF00"))
                    .setImage("attachment://image.png");
            channel.sendMessageEmbeds(embed.build()).addFiles(fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":wrench: РЈСЃРїРµС€РЅРѕ", "РњРѕРґ РѕС‚РїСЂР°РІР»РµРЅ РІ " + channel.getAsMention(), accent));

        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: РћС€РёР±РєР°", getSimpleMessage(t), scarlet);
        }));

    }

    public static void artParser(Message message, Message.Attachment attachment) {
        Log.info(attachment.getFileName());
        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            var channel = Vars.jda.getTextChannelById(Vars.config.artsChannelId);

            attachment.getHeight();
            var embed = new EmbedBuilder()
                    .setTitle(attachment.getFileName())
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .setColor(java.awt.Color.decode("#00FF00"))
                    .setFooter(attachment.getWidth() + "x" + attachment.getHeight() + ", " + attachment.getSize() + "B")
                    .setImage("attachment://image.png");
            channel.sendMessageEmbeds(embed.build()).addFiles(fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":wrench: РЈСЃРїРµС€РЅРѕ", "РђСЂС‚ РѕС‚РїСЂР°РІР»РµРЅ РІ " + channel.getAsMention(), accent));

        }, t ->  {
            t.printStackTrace();
            reply(message, ":warning: РћС€РёР±РєР°", getSimpleMessage(t), scarlet);
        }));


    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        var message = event.getMessage();

        if (!event.isFromGuild() || event.getAuthor().isBot()) return;

        if (message.getChannel().getIdLong() != config.botChannelId) return;


        for (Message.Attachment attachment : message.getAttachments()) {

            executor.submit(() -> {
                if (attachment.getFileName().endsWith(".msch")) {
                    schemParser(message, attachment);
                } else if (attachment.getFileName().endsWith(".msav")) {
                    mapParser(message, attachment);
                } else if (attachment.getFileName().endsWith(".zip")) {
                    modParser(message, attachment);
                } else {
                    artParser(message, attachment);
                }

            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }
}