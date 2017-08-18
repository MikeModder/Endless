/*
 * Copyright (C) 2017 Artu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.artuto.endless.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Artu
 */

public class Lookup extends Command
{
    public Lookup()
        {
            this.name = "lookup";
            this.help = "Retrieves info about an invite, a guild or an user using their ID from Discord's servers.";
            this.arguments = "User ID | Invite code | Invite URL (only discord.gg) | Guild ID";
            this.category = new Command.Category("Tools");
            this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
            this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
            this.ownerCommand = false;
            this.guildOnly = false;
       }
    
    @Override
    protected void execute(CommandEvent event)
    {
        Color color;
        
        if(event.isFromType(ChannelType.PRIVATE))
        {
            color = Color.decode("#33ff00");
        }
        else
        {
            color = event.getGuild().getSelfMember().getColor();
        }
        
        EmbedBuilder builder = new EmbedBuilder();

        if(event.getArgs().isEmpty())
        {
            event.replyWarning("Please specify something!");
            return;
        }
        
        try
        {
            String code = null;
            
            if(event.getArgs().startsWith("https"))
            {
                code = event.getArgs().replace("https://discord.gg/", "");
            }
            else
            {
                code = event.getArgs();
            }
            
            Invite invite;
            invite = Invite.resolve(event.getJDA(), code).complete();
            String title = ":information_source: Invite info:";

            builder.addField(":map: Guild Name: ", "**"+invite.getGuild().getName()+"**", true);
            builder.addField(":map::1234: Guild ID: ", "**"+invite.getGuild().getId()+"**", true);
            builder.addField(":speech_balloon: Channel: ", "**"+invite.getChannel().getName()+"**", true);
            builder.addField(":speech_balloon::1234: Channel ID: ", "**"+invite.getChannel().getId()+"**", true);
            builder.addField(":bust_in_silhouette: Inviter: ", "**"+invite.getInviter().getName()+"#"+invite.getInviter().getDiscriminator()+"** ("+invite.getInviter().getId()+")", false);
            builder.setFooter("Invite Code: "+invite.getCode(), null);
            builder.setThumbnail(invite.getGuild().getIconUrl());
            builder.setColor(color);
            
            event.getChannel().sendMessage(new MessageBuilder().append(title).setEmbed(builder.build()).build()).queue();
        }
        catch(Exception ei)
        {
        }
        
        try
        {   
            String ranks = null;
            User user;
            user = event.getJDA().retrieveUserById(event.getArgs()).complete();
            
            if(user.getAvatarId().startsWith("a_"))
            {
                ranks = "<:nitro:334859814566101004> **Nitro**";
            }
            else
            {
                ranks = "**None**";
            }
            
            String title=(user.isBot()?":information_source: Information about the bot **"+user.getName()+"**"+"#"+"**"+user.getDiscriminator()+"** <:bot:334859813915983872>":":information_source: Information about the user **"+user.getName()+"**"+"#"+"**"+user.getDiscriminator()+"**");
            
            builder.addField(":1234: ID: ", "**"+user.getId()+"**", true);
            builder.addField(":medal: Special Ranks: ", ranks, true);
    	    builder.addField(":calendar_spiral: Account Creation Date: ", "**"+user.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)+"**", true);
            if(user.getName().contains("Kyle2000"))
            {
                builder.addField(":poop: Shithead: ", "**A LOT**", true);
            }
	        builder.setThumbnail(user.getEffectiveAvatarUrl());
    	    builder.setColor(color);
            event.getChannel().sendMessage(new MessageBuilder().append(title).setEmbed(builder.build()).build()).queue(); 
        }
        catch(Exception e)
        {
        }
        
        try
        {
            Guild guild;
    	    guild = event.getJDA().getGuildById(event.getArgs());
            Member owner;        
            owner = guild.getOwner();
    	    String title =":information_source: Information about the guild **"+guild.getName()+"**";
        
            long botCount = guild.getMembers().stream().filter(u -> u.getUser().isBot()).count();
        
            builder.addField(":1234: ID: ", "**"+guild.getId()+"**", true);
            builder.addField(":bust_in_silhouette: Owner: ", "**"+owner.getUser().getName()+"**#**"+owner.getUser().getDiscriminator()+"**", true);
            builder.addField(":map: Region: ", "**"+guild.getRegion()+"**", true);
            builder.addField(":one: User count: ", "**"+guild.getMembers().size()+"** (**"+botCount+"** bots)", true);
    	    builder.setThumbnail(guild.getIconUrl());
            builder.setColor(guild.getSelfMember().getColor());
    	    event.getChannel().sendMessage(new MessageBuilder().append(title).setEmbed(builder.build()).build()).queue();
        }
        catch(Exception e)
        {     
        }
    }
}