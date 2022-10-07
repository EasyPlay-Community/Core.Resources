package cbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.awt.*;

public class Bot extends ListenerAdapter {

    public static JDA jda;

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(args[0]);
        builder.setActivity(EntityBuilder.createActivity("+помощь", null, ActivityType.DEFAULT));
        builder.addEventListeners(new Bot());
        jda = builder.build();
    }


    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        User author = message.getAuthor();
        if (author.isBot()) {
            return;
        }
        if (message.getChannel().getIdLong() != 659588640636403713L) {
            return;
        }
        System.out.println("Catch message");
        BHandler.handleMsg(message);
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        TextChannel channel = jda.getTextChannelById(917794148990459925L);
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Core.Resources запущен!")
                .setColor(Color.green);
        channel.sendMessage(builder.build()).queue();;
    }


}
