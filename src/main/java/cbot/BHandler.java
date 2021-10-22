package cbot;

import arc.files.Fi;
import arc.util.io.Streams;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.type.ItemStack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import cbot.CHandler.Map;
import java.util.UUID;

public class BHandler {
    public static Long mapId=662054803651821618L;
    public static Long schId=671647908000694292L;
    public static Long modId=668100104120565760L;
    public static Long artId=811903482499694632L;
    public static Long botId=659588640636403713L;

    public static void handleMsg(Message msg){
        if(msg.getContentRaw().equalsIgnoreCase("+помощь")){
            help(msg);
        }
        if(msg.getAttachments().size()!=1){msg.delete();return;}
        if(msg.getContentRaw().equalsIgnoreCase("+арт")){
            art(msg,msg.getAttachments().get(0));
        }
        if(!msg.getAttachments().get(0).getFileName().endsWith(".msch")&&!msg.getAttachments().get(0).getFileName().endsWith(".msav")&&!msg.getAttachments().get(0).getFileName().endsWith(".zip")){
            System.out.println(msg.getAttachments().get(0).getFileName());msg.delete();return;
        }
        System.out.println("content");
        Message.Attachment a = msg.getAttachments().get(0);
        //Отличия версий начинаются здесь
        //разкидка по типу контента
        if(msg.getAttachments().get(0).getFileName().endsWith(".msch")){schema(msg,a);}
        if(msg.getAttachments().get(0).getFileName().endsWith(".zip")){mod(msg,a);}
        if(msg.getAttachments().get(0).getFileName().endsWith(".msav")){karta(msg,a);}
    }
    public static void schema(Message msg, Message.Attachment a){
        CHandler f=new CHandler();
        try {
            Schematic schem = f.parseSchematicURL(a.getUrl());
            System.out.print("***");
            BufferedImage preview = f.previewSchematic(schem);
            File previewFile = new File("img_" + UUID.randomUUID().toString() + ".png");
            File schemFile = new File(schem.name() + "." + Vars.schematicExtension);
            Schematics.write(schem, new Fi(schemFile));
            ImageIO.write(preview, "png", previewFile);
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode("#00FF00")).setColor(Color.decode("#00FF00"))
                    .setImage("attachment://" + previewFile.getName())
                    .setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatarUrl(), msg.getAuthor().getAvatarUrl()).setTitle(schem.name());

            StringBuilder field = new StringBuilder();
           System.out.print(255);
            for(ItemStack stack : schem.requirements()){
                List<Emote> emotes = msg.getGuild().getEmotesByName(stack.item.name.replace("-", ""), true);
                Emote result = emotes.isEmpty() ? msg.getGuild().getEmotesByName("ohno", true).get(0) : emotes.get(0);

                field.append(result.getAsMention()).append(stack.amount).append("  ");
            }

            builder.addField(" :arrow_lower_right: Требуемые Ресурсы :arrow_lower_left: ", field.toString(), false);
            TextChannel schC = cbot.Bot.jda.getTextChannelById(schId);
            schC.sendFile(schemFile).addFile(previewFile).embed(builder.build()).queue();
            System.out.println("schemm posted");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void art(Message msg, Message.Attachment a){
        new File("arts/").mkdir();
        File artFile = new File("arts/" + a.getFileName());
        TextChannel artC = cbot.Bot.jda.getTextChannelById(artId);
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode("#00FF00")).setColor(Color.decode("#00FF00"))
                .setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatarUrl(), msg.getAuthor().getAvatarUrl()).setTitle(a.getFileName().replace(".*", ""));
        try{ Streams.copy(CHandler.download(a.getUrl()), new FileOutputStream(artFile));}catch(Exception e){return;}
        artC.sendMessage(builder.build()).addFile(artFile).queue();
    }
    public static void mod(Message msg, Message.Attachment a){
        new File("mods/").mkdir();
        File modFile = new File("mods/" + a.getFileName());
        String baka=msg.getContentRaw().substring(5);
        TextChannel modC = cbot.Bot.jda.getTextChannelById(modId);
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode("#00FF00")).setColor(Color.decode("#00FF00"))
        .setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatarUrl(), msg.getAuthor().getAvatarUrl()).setTitle(a.getFileName().replace(".zip", ""));
        builder.setFooter(baka);
        try{ Streams.copy(CHandler.download(a.getUrl()), new FileOutputStream(modFile));}catch(Exception e){return;}
        modC.sendMessage(builder.build()).addFile(modFile).queue();
    }
    public static void karta(Message msg, Message.Attachment a){
        try{
            CHandler f=new CHandler();
            Map map = f.readMap(CHandler.download(a.getUrl()));
            new File("maps/").mkdir();
            File mapFile = new File("maps/"+ a.getFileName());
            File imageFile = new File("maps/image_output.png");
            Streams.copy(CHandler.download(a.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile);

            EmbedBuilder builder = new EmbedBuilder().setColor(Color.decode("#00FF00")).setColor(Color.decode("#00FF00"))
                    .setImage("attachment://" + imageFile.getName())
                    .setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatarUrl(), msg.getAuthor().getAvatarUrl()).setTitle(map.name == null ? a.getFileName().replace(".msav", "") : map.name);
            if(map.description != null) builder.setFooter(map.description);

            TextChannel mapC = cbot.Bot.jda.getTextChannelById(mapId);
            File mapFileO = new File("maps/"+ a.getFileName());

            mapC.sendFile(mapFileO).addFile(imageFile).embed(builder.build()).queue();


            System.out.println("posted map");
        }catch(Exception e){}
    }
    public static void help(Message msg){
        System.out.println("help requested");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.decode("#00FF00")).setColor(Color.decode("#00FF00"))
                .setAuthor(msg.getAuthor().getName(), msg.getAuthor().getAvatarUrl(), msg.getAuthor().getAvatarUrl()).setTitle("Помощь");
        builder.addField("+арт","Отправляет прикрепленное изображение в канал с артами.",false);
        builder.addField("Отправка карт, схем и модов","Просто отправьте соответствующий фаил в бот канал.",false);
        TextChannel botC = cbot.Bot.jda.getTextChannelById(botId);
        botC.sendMessage(builder.build()).queue();
    }
}

