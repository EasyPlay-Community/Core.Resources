package cbot;

import arc.func.Cons;
import arc.graphics.Color;
import arc.util.Log;
import arc.util.Strings;
import arc.util.UnsafeRunnable;
import cbot.components.ContentHandler;
import cbot.components.TriangledData;
import cbot.components.TriangledGenerator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.File;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static arc.graphics.Color.scarlet;
import static arc.util.Strings.format;
import static arc.util.Strings.getSimpleMessage;
import static cbot.Vars.*;
import static mindustry.Vars.*;
import static mindustry.graphics.Pal.accent;
import static net.dv8tion.jda.api.utils.AttachedFile.fromData;


public class Listener extends ListenerAdapter {
    static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void loadCommands(String prefix) {
        Vars.handler.setPrefix(prefix);

        Vars.handler.<Message>register("help", "Список всех команд.", (args, message) -> {
            var builder = new StringBuilder();
            Vars.handler.getCommandList().each(command -> builder.append(prefix).append("**").append(command.text).append("**").append(command.paramText).append(" - ").append(command.description).append("\n"));
            reply(message, ":newspaper: Список всех команд:", builder.toString(), accent);
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

        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            var map = ContentHandler.parseMap(file);
            var image = ContentHandler.parseMapImage(map);

            var embed = new EmbedBuilder()
                    .setTitle(map.name())
                    .setDescription(map.description())
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .setFooter(map.width + "x" + map.height)
                    .setColor(accent.argb8888())
                    .setImage("attachment://image.png");

            var forum = message.getGuild().getForumChannelById(config.mapsForumId);
            var post = MessageCreateBuilder.from(MessageCreateData.fromEmbeds(embed.build()));

            post.addFiles(fromData(image, "image.png"), fromData(attachment.getProxy().download().get(), attachment.getFileName()));
            forum.createForumPost(Strings.stripColors(map.name()), post.build()).queue(queue -> reply(message, ":map: Успешно", "Карта отправлена в " + forum.getAsMention(), accent));
        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
        }));

    }


    static void schemParser(Message message, Message.Attachment attachment) {

        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            var schematic = ContentHandler.parseSchematic(file);
            var image = ContentHandler.parseSchematicImage(schematic);
            var builder = new StringBuilder();
            schematic.requirements().each((item, amount) ->
                    builder.append(Vars.emojis.containsKey(item) ? format("<:@:@>", item.name.replaceAll("-", ""), Vars.emojis.get(item)) : item.name).append(" ").append(amount).append(" "));
            var forum = message.getGuild().getForumChannelById(Vars.config.schematicsForumId);
            var embed = new EmbedBuilder()
                    .setTitle(schematic.name())
                    .setDescription(schematic.description())
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .addField("Requirements", builder.toString(), true)
                    .setFooter(schematic.width + "x" + schematic.height + ", " + schematic.tiles.size + " blocks")
                    .setColor(accent.argb8888()).setImage("attachment://image.png");

            var post = MessageCreateBuilder.from(MessageCreateData.fromEmbeds(embed.build()));
            post.addFiles(fromData(image, "image.png"), fromData(attachment.getProxy().download().get(), attachment.getFileName()));
            forum.createForumPost(Strings.stripColors(schematic.name()), post.build()).queue(queue -> reply(message, ":wrench: Успешно", "Схема отправлена в " + forum.getAsMention(), accent));
        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
        }));

    }

    public static void modParser(Message message, Message.Attachment attachment) {
        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            var forum = message.getGuild().getForumChannelById(Vars.config.modsForumId);
            var embed = new EmbedBuilder()
                    .setTitle(attachment.getFileName().replace(".zip", ""))
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .setFooter(message.getContentRaw())
                    .setColor(java.awt.Color.decode("#00FF00"))
                    .setImage("attachment://image.png");
            var post = MessageCreateBuilder.from(MessageCreateData.fromEmbeds(embed.build()));

            post.addFiles(fromData(attachment.getProxy().download().get(), attachment.getFileName()));
            forum.createForumPost(Strings.stripColors(attachment.getFileName().replace(".zip", "")), post.build()).queue(queue -> reply(message, ":wrench: Успешно", "Мод отправлен в " + forum.getAsMention(), accent));
        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
        }));

    }

    public static void artParser(Message message, Message.Attachment attachment) {
        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            var forum = Vars.jda.getForumChannelById(config.artsForumId);

            var embed = new EmbedBuilder()
                    .setTitle(attachment.getFileName())
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .setColor(java.awt.Color.decode("#00FF00"))
                    .setFooter(attachment.getWidth() + "x" + attachment.getHeight() + ", " + attachment.getSize() + "B")
                    .setImage("attachment://image.png");


            var post = MessageCreateBuilder.from(MessageCreateData.fromEmbeds(embed.build()));

            post.addFiles(fromData(attachment.getProxy().download().get(), attachment.getFileName()));
            forum.createForumPost(Strings.stripColors(attachment.getFileName().replace(".zip", "")), post.build()).queue(queue -> reply(message, ":wrench: Успешно", "Арт отправлен в " + forum.getAsMention(), accent));
        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
        }));
    }

    private static void jsonParser(Message message, Message.Attachment attachment) {
        executor.submit(() -> {
            attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
                var data = new TriangledData();
                Log.info("Генерация карты...");
                TriangledGenerator generator = new TriangledGenerator(file);
                world.loadGenerator(generator.width, generator.height, generator);
                data.initTriangles(generator.getTriangle());
                Log.info("Карта сгенерирована.");


                var map = state.map;
                var image = ContentHandler.parseMapImage(map);

                var embed = new EmbedBuilder().setTitle(map.name()).setDescription(map.description()).setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl()).setFooter(map.width + "x" + map.height).setColor(accent.argb8888()).setImage("attachment://image.png");

                var channel = Vars.jda.getTextChannelById(config.mapsForumId);

                channel.sendMessageEmbeds(embed.build())
                        .addFiles(fromData(image, "image.png"), fromData(attachment.getProxy().download().get(), attachment.getFileName()))
                        .queue(queue -> reply(message, ":map: Успешно", "Карта отправлена в " + channel.getAsMention(), accent));
            }, t -> {
                t.printStackTrace();
                reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
            }));
        });
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        var message = event.getMessage();

        if (!event.isFromGuild() || event.getAuthor().isBot()) return;

        if (message.getChannel().getIdLong() != config.botChannelId) return;

        for (Message.Attachment attachment : message.getAttachments()) {

            executor.submit(() -> {

                if (attachment.getFileExtension() == null) {
                    reply(message, ":warning: Ошибка!", "Тип файла не определен.\n Для корректной работы файл должен иметь тип:\n `msch`, `msav`, `jpeg`, `jpg`, `png` или `zip`", scarlet);
                    return;
                }
                Log.info(attachment.getFileName());
                switch (attachment.getFileExtension()) {

                    case "msch" -> schemParser(message, attachment);

                    case "msav" -> mapParser(message, attachment);

                    case "zip" -> modParser(message, attachment);

                    case "webp","gif", "jpg", "jpeg", "png", "mp4", "mov" -> artParser(message, attachment);
                }
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cache.delete();
            cache.mkdirs();
        }

    }
}
