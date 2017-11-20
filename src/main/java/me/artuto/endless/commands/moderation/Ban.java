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

package me.artuto.endless.commands.moderation;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.utils.FinderUtil;
import java.awt.Color;
import java.time.Instant;
import java.util.List;

import me.artuto.endless.Messages;
import me.artuto.endless.cmddata.Categories;
import me.artuto.endless.loader.Config;
import me.artuto.endless.utils.FormatUtil;
import me.artuto.endless.logging.ModLogging;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artu
 */

public class Ban extends Command
{
    private final ModLogging modlog;
    private final Config config;

    public Ban(ModLogging modlog, Config config)
    {
        this.modlog = modlog;
        this.config = config;
        this.name = "ban";
        this.help = "Bans the specified user";
        this.arguments = "<@user|ID|nickname|username> for [reason]";
        this.category = Categories.MODERATION;
        this.botPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.ownerCommand = false;
        this.guildOnly = true;
    }
    
    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder builder = new EmbedBuilder();
        Member member;
        User author = event.getAuthor();
        String target;
        String reason;
        
        if(event.getArgs().isEmpty())
        {
            event.replyWarning("Invalid Syntax: "+event.getClient().getPrefix()+"ban <@user|ID|nickname|username> for [reason]");
            return;
        }

        try
        {
            String[] args = event.getArgs().split(" for ", 2);
            target = args[0].trim();
            reason = args[1].trim();
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            target = event.getArgs().trim();
            reason = "[no reason specified]";
        }
        
        List<Member> list = FinderUtil.findMembers(target, event.getGuild());
            
        if(list.isEmpty())
        {
            event.replyWarning("I was not able to found a user with the provided arguments: '"+target+"'");
            return;
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfMembers(list, target));
            return;
        }
    	else
            member = list.get(0);
    
        if(!event.getSelfMember().canInteract(member))
        {
            event.replyError("I can't ban the specified user!");
            return;
        }
        
        if(!event.getMember().canInteract(member))
        {
            event.replyError("You can't ban the specified user!");
            return;
        }
        
        String username = "**"+member.getUser().getName()+"#"+member.getUser().getDiscriminator()+"**";
        String r = reason;
              
        try
        {
            if(!(member.getUser().isBot()))
            {
                builder.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl());
                builder.setTitle("Ban");
                builder.setDescription("You were banned on the guild **"+event.getGuild().getName()+"** by **"
                        +event.getAuthor().getName()+"#"+event.getAuthor().getDiscriminator()+"**\n"
                        + "They gave the following reason: **"+reason+"**\n");
                builder.setFooter("Time", null);
                builder.setTimestamp(Instant.now());
                builder.setColor(Color.RED);
                builder.setThumbnail(event.getGuild().getIconUrl());

               member.getUser().openPrivateChannel().queue(pc -> pc.sendMessage(new MessageBuilder().setEmbed(builder.build()).build()).queue(
                    (d) ->
                    {
                        event.getGuild().getController().ban(member, 1).reason("["+author.getName()+"#"+author.getDiscriminator()+"]: "+ r).complete();
                        event.replySuccess(Messages.BAN_SUCCESS+username);
                    },
                    (e) ->
                    {
                        event.getGuild().getController().ban(member, 1).reason("["+author.getName()+"#"+author.getDiscriminator()+"]: "+ r).complete();
                        event.replySuccess(Messages.BAN_SUCCESS+username);
                    }));
            }
            else
            {
                event.getGuild().getController().ban(member, 1).reason("["+author.getName()+"#"+author.getDiscriminator()+"]: "+ reason).complete();
                event.replySuccess(Messages.BAN_SUCCESS+username);
            }

            modlog.logBan(event.getAuthor(), member, reason, event.getGuild(), event.getTextChannel());
        }
        catch(Exception e)
        {
            event.replyError(Messages.BAN_ERROR+username);
            LoggerFactory.getLogger("Ban Command").error(e.getMessage());
            if(config.isDebugEnabled())
                e.printStackTrace();
        }
    }
}