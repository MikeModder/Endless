/*
 * Copyright (C) 2017-2018 Artuto
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

package me.artuto.endless.commands.serverconfig;

import com.jagrosh.jdautilities.command.Command;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.core.entities.Ignore;
import me.artuto.endless.utils.ArgsUtils;
import me.artuto.endless.utils.GuildUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.List;

/**
 * @author Artuto
 */

public class IgnoreCmd extends EndlessCommand
{
    private final Bot bot;

    public IgnoreCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "ignore";
        this.arguments = "<addX|list|removeX>";
        this.children = new Command[]{new AddChannelCmd(), new AddRoleCmd(), new AddUserCmd(),
                new ListIgnoresCmd(), new RemoveChannelCmd(), new RemoveRoleCmd(), new RemoveUserCmd()};
        this.help = "Ignores the specified channel, role or user. Endless won't respond to commands in those channels," +
                " these users or by users with those roles. Admins won't be ignored.";
        this.category = Categories.SERVER_CONFIG;
        this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        event.replyError("command.ignore.noArgs");
    }

    private class AddChannelCmd extends EndlessCommand
    {
        AddChannelCmd()
        {
            this.name = "addchannel";
            this.arguments = "<channel>";
            this.help = "Ignores the specified channel.";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = true;
            this.parent = IgnoreCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            Guild guild = event.getGuild();
            TextChannel tc = ArgsUtils.findTextChannel(event, event.getArgs());
            if(tc==null)
                return;

            if(bot.endless.getIgnore(guild, tc.getIdLong())==null)
            {
                bot.gsdm.addIgnore(guild, tc.getIdLong());
                event.replySuccess("command.ignore.added", tc.getAsMention());
            }
            else
                event.replyError("command.ignore.alreadyIgnored", "channel");
        }
    }

    private class AddRoleCmd extends EndlessCommand
    {
        AddRoleCmd()
        {
            this.name = "addrole";
            this.arguments = "<role>";
            this.help = "Ignores the users with the specified role.";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = true;
            this.parent = IgnoreCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            Guild guild = event.getGuild();
            Role role = ArgsUtils.findRole(event, event.getArgs());
            if(role==null)
                return;

            if(bot.endless.getIgnore(guild, role.getIdLong())==null)
            {
                bot.gsdm.addIgnore(guild, role.getIdLong());
                event.replySuccess("command.ignore.added", role.getName());
            }
            else
                event.replyError("command.ignore.alreadyIgnored", "role");
        }
    }

    private class AddUserCmd extends EndlessCommand
    {
        AddUserCmd()
        {
            this.name = "adduser";
            this.arguments = "<user>";
            this.help = "Ignores the specified user.";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = true;
            this.parent = IgnoreCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            Guild guild = event.getGuild();
            Member member = ArgsUtils.findMember(event, event.getArgs());
            if(member==null)
                return;
            User user = member.getUser();

            if(bot.endless.getIgnore(guild, user.getIdLong())==null)
            {
                bot.gsdm.addIgnore(guild, user.getIdLong());
                event.replySuccess("command.ignore.added", user.getName()+"#"+user.getDiscriminator());
            }
            else
                event.replyError("command.ignore.alreadyAdded", "user");
        }
    }

    private class ListIgnoresCmd extends EndlessCommand
    {
        ListIgnoresCmd()
        {
            this.name = "list";
            this.help = "Shows the list of ignored entities.";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = true;
            this.needsArguments = false;
            this.parent = IgnoreCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            EmbedBuilder builder = new EmbedBuilder();
            IMentionable entity;
            List<Ignore> ignores = ((GuildSettings)event.getClient().getSettingsFor(event.getGuild())).getIgnoredEntities();
            StringBuilder sb = new StringBuilder();

            if(ignores.isEmpty())
                event.replyWarning("command.ignore.empty");
            else
            {
                for(Ignore ignore : ignores)
                {
                    entity = event.getGuild().getRoleById(ignore.getEntityId());
                    if(entity==null || ((Role)entity).isPublicRole())
                        entity = event.getGuild().getTextChannelById(ignore.getEntityId());
                    if(entity==null)
                        entity = event.getGuild().getMemberById(ignore.getEntityId());

                    sb.append(entity.getAsMention());
                }

                builder.setDescription(sb);
                builder.setColor(event.getSelfMember().getColor());
                builder.setFooter(event.localize("command.ignore.list")+" "+event.getGuild().getName(), event.getGuild().getIconUrl());
                event.reply(builder.build());
            }
        }
    }

    private class RemoveChannelCmd extends EndlessCommand
    {
        RemoveChannelCmd()
        {
            this.name = "removechannel";
            this.arguments = "<channel>";
            this.help = "Un-ignores the specified channel.";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = true;
            this.parent = IgnoreCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            Guild guild = event.getGuild();
            TextChannel tc = ArgsUtils.findTextChannel(event, event.getArgs());
            if(tc==null)
                return;

            if(!(bot.endless.getIgnore(guild, tc.getIdLong())==null))
            {
                bot.gsdm.removeIgnore(guild, tc.getIdLong());
                event.replySuccess("command.ignore.removed", tc.getAsMention());
            }
            else
                event.replyError("command.ignore.notIgnored", "channel");
        }
    }

    private class RemoveRoleCmd extends EndlessCommand
    {
        RemoveRoleCmd()
        {
            this.name = "removerole";
            this.arguments = "<role>";
            this.help = "Un-ignores the specified role.";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = true;
            this.parent = IgnoreCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            Guild guild = event.getGuild();
            Role role = ArgsUtils.findRole(event, event.getArgs());
            if(role==null)
                return;

            if(!(bot.endless.getIgnore(guild, role.getIdLong())==null))
            {
                bot.gsdm.removeIgnore(guild, role.getIdLong());
                event.replySuccess("command.ignore.removed", role.getName());
            }
            else
                event.replyWarning("command.ignore.notIgnored", "role");
        }
    }

    private class RemoveUserCmd extends EndlessCommand
    {
        RemoveUserCmd()
        {
            this.name = "removeuser";
            this.arguments = "<user>";
            this.help = "Un-ignores the specified user.";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.guildOnly = true;
            this.parent = IgnoreCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            Guild guild = event.getGuild();
            Member member = ArgsUtils.findMember(event, event.getArgs());
            if(member==null)
                return;
            User user = member.getUser();

            if(!(bot.endless.getIgnore(guild, user.getIdLong())==null))
            {
                bot.gsdm.removeIgnore(guild, user.getIdLong());
                event.replySuccess("command.ignore.removed", user.getName()+"#"+user.getDiscriminator());
            }
            else
                event.replyWarning("command.ignore.notIgnored", "user");
        }
    }
}
