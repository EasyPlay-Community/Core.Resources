package cbot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class Bot extends ListenerAdapter {

public static JDA jda;
    public static void main(String[] args) throws InterruptedException, LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(args[0]); //
		System.out.println("token");
        builder.setActivity(EntityBuilder.createActivity("+помощь", null, ActivityType.DEFAULT));
		System.out.println("activity");
        builder.addEventListeners(new Bot());
		System.out.println("listener");
        jda=builder.build();
        System.out.println("runned");
    }

    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        Message message = event.getMessage();
        User author = message.getAuthor();
        String content = message.getContentRaw();
        if (author.isBot()){ return;}
        if(message.getChannel().getIdLong()!=659588640636403713L){return;}
        if(message.getAuthor().getIdLong()==417610494895980545L){return;}
        System.out.println("Catch message");
        BHandler.handleMsg(message);
    }

  


}