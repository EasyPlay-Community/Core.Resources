package cbot;

import arc.func.Cons;
import arc.graphics.Color;
import arc.util.Log;
import arc.util.UnsafeRunnable;
import cbot.components.ContentHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static arc.graphics.Color.scarlet;
import static arc.util.Strings.format;
import static arc.util.Strings.getSimpleMessage;
import static cbot.Vars.cache;
import static cbot.Vars.config;
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

                var channel = Vars.jda.getTextChannelById(config.mapsChannelId);

                channel.sendMessageEmbeds(embed.build()).addFiles(fromData(image, "image.png"), fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":map: Успешно", "Карта отправлена в " + channel.getAsMention(), accent));
            }, t -> {
                t.printStackTrace();
                reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
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

            channel.sendMessageEmbeds(embed.build()).addFiles(fromData(image, "image.png"), fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":wrench: Успешно", "Схема отправлена в " + channel.getAsMention(), accent));
        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
        }));

    }

    public static void modParser(Message message, Message.Attachment attachment) {
        Log.info(attachment.getFileName());
        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            String footer = message.getContentRaw().substring(5);
            var channel = Vars.jda.getTextChannelById(config.modsChannelId);


            var embed = new EmbedBuilder()
                    .setTitle(attachment.getFileName().replace(".zip", ""))
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .setFooter(footer)
                    .setColor(java.awt.Color.decode("#00FF00"))
                    .setImage("attachment://image.png");
            channel.sendMessageEmbeds(embed.build()).addFiles(fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":wrench: Успешно", "Мод отправлен в " + channel.getAsMention(), accent));

        }, t -> {
            t.printStackTrace();
            reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
        }));

    }

    public static void artParser(Message message, Message.Attachment attachment) {
        Log.info(attachment.getFileName());
        attachment.getProxy().downloadToFile(Vars.cache.child(attachment.getFileName()).file()).thenAccept(file -> tryWorkWithFile(file, () -> {
            var channel = Vars.jda.getTextChannelById(config.artsChannelId);

            attachment.getHeight();
            var embed = new EmbedBuilder()
                    .setTitle(attachment.getFileName())
                    .setAuthor(message.getMember().getEffectiveName(), attachment.getUrl(), message.getMember().getEffectiveAvatarUrl())
                    .setColor(java.awt.Color.decode("#00FF00"))
                    .setFooter(attachment.getWidth() + "x" + attachment.getHeight() + ", " + attachment.getSize() + "B")
                    .setImage("attachment://image.png");
            channel.sendMessageEmbeds(embed.build()).addFiles(fromData(attachment.getProxy().download().get(), attachment.getFileName())).queue(queue -> reply(message, ":wrench: Успешно", "Арт отправлен в " + channel.getAsMention(), accent));

        }, t ->  {
            t.printStackTrace();
            reply(message, ":warning: Ошибка", getSimpleMessage(t), scarlet);
        }));


    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        var message = event.getMessage();

        if (!event.isFromGuild() || event.getAuthor().isBot()) return;

        if (message.getChannel().getIdLong() != config.botChannelId) return;


        for (Message.Attachment attachment : message.getAttachments()) {

            executor.submit(() -> {

                if (attachment.getFileExtension() == null || !(
                        Objects.equals(attachment.getFileExtension(), "msch") ||
                        Objects.equals(attachment.getFileExtension(), "msav") ||
                        Objects.equals(attachment.getFileExtension(), "zip") ||
                        Objects.equals(attachment.getFileExtension(), "jpg") ||
                        Objects.equals(attachment.getFileExtension(), "jpeg") ||
                        Objects.equals(attachment.getFileExtension(), "png") ||
                        Objects.equals(attachment.getFileExtension(), "mp4"))) {
                    reply(message, ":warning: Ошибка!", "Тип файла не определен.\n Для корректной работы файл должен иметь тип:\n `msch`, `msav`, `jpeg`, `jpg`, `png` или `zip`", scarlet);
                    return;
                }
                switch (attachment.getFileExtension()) {
                    case "msch" -> schemParser(message, attachment);
                    case "msav" -> mapParser(message, attachment);
                    case "zip" -> modParser(message, attachment);
                    case "gif", "jpg" , "jpeg" , "png", "mp4" -> artParser(message, attachment);

                }
            });
            try {
                Thread.sleep(500);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        cache.delete();
        cache.mkdirs();

    }
}