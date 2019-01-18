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

package me.artuto.endless.commands.utils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.Endless;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.utils.ChecksUtil;
import me.artuto.endless.utils.FormatUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.List;

/**
 * @author Artuto
 */

public class ColorMeCmd extends EndlessCommand
{
    private final Bot bot;

    public ColorMeCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "colorme";
        this.help = "Command for enabled roles that lets user change their color.";
        this.arguments = "[color in hex]";
        this.category = Categories.UTILS;
        this.children = new Command[]{new AddCmd(), new RemoveCmd()};
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(CommandEvent event)
    {
        if(!(bot.dataEnabled))
        {
            event.replyError("Endless is running on No-data mode.");
            return;
        }

        String args = event.getArgs();
        Guild guild = event.getGuild();
        List<Role> colorMeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getColorMeRoles();
        Member member = event.getMember();
        Role role;

        if(args.isEmpty())
        {
            if(colorMeRoles.isEmpty())
                event.replyWarning("This guild doesn't has any ColorMe roles.");
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.append(":performing_arts: ColorMe roles available on **").append(guild.getName()).append("**:").append("\n");

                for(Role r : colorMeRoles)
                    sb.append(Const.LINE_START).append(" ").append(r.getName()).append("\n");

                event.reply(sb.toString());
            }
        }
        else
        {
            Color color = getColor(event.getArgs());
            role = member.getRoles().stream().filter(r -> !(r.getColor()==null)).findFirst().orElse(null);
            if(color==null)
            {
                event.replyError("The string you specified is not a valid HEX value!");
                return;
            }
            if(role==null)
            {
                event.replyError("You don't have any colored roles! Make sure you have at least one colored role **already** created!");
                return;
            }

            if(colorMeRoles.contains(role))
            {
                if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), role)))
                    event.replyError("I can't interact with that role!");
                else
                {
                    User author = event.getAuthor();
                    role.getManager().setColor(color).reason(author.getName()+"#"+author.getDiscriminator()+": ColorMe")
                            .queue(s -> event.replySuccess(String.format("Successfully changed *%s's* color to %s", role.getName(), event.getArgs())),
                                    e -> {
                        event.replyError(String.format("Could not change *%s's* color to %s", role.getName(), event.getArgs()));
                        Endless.LOG.error("Error while changing the color of role {}", role.getId());
                    });
                }
            }
            else
                event.replyWarning("The role *"+role.getName()+"* is not enabled for RoleMe!");
        }
    }

    private class AddCmd extends EndlessCommand
    {
        AddCmd()
        {
            this.name = "add";
            this.help = "Adds a role to the list of available ColorMe roles.";
            this.arguments = "<role>";
            this.category = Categories.UTILS;
            this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.parent = ColorMeCmd.this;
        }

        @Override
        protected void executeCommand(CommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("Endless is running on No-data mode.");
                return;
            }

            Guild guild = event.getGuild();
            List<Role> colormeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getColorMeRoles();
            String args = event.getArgs();
            Role role;

            List<Role> list = FinderUtil.findRoles(args, event.getGuild());

            if(list.isEmpty())
            {
                event.replyWarning("I was not able to found a role with the provided arguments: '"+event.getArgs()+"'");
                return;
            }
            else if(list.size()>1)
            {
                event.replyWarning(FormatUtil.listOfRoles(list, event.getArgs()));
                return;
            }
            else role = list.get(0);

            if(colormeRoles.contains(role))
            {
                event.replyError("That role is already on the ColorMe roles list!");
                return;
            }

            if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), role)))
                event.replyError("I can't interact with that role!");
            else
            {
                bot.gsdm.addColormeRole(guild, role);
                event.replySuccess("Successfully added the role *"+role.getName()+"* to the ColorMe roles list.");
            }
        }
    }

    private class RemoveCmd extends EndlessCommand
    {
        RemoveCmd()
        {
            this.name = "remove";
            this.help = "Removes a role from the list of available ColorMe roles.";
            this.arguments = "<role>";
            this.category = Categories.UTILS;
            this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.parent = ColorMeCmd.this;
        }

        @Override
        protected void executeCommand(CommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("Endless is running on No-data mode.");
                return;
            }

            Guild guild = event.getGuild();
            List<Role> colormeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getColorMeRoles();
            String args = event.getArgs();
            Role role;

            List<Role> list = FinderUtil.findRoles(args, event.getGuild());

            if(list.isEmpty())
            {
                event.replyWarning("I was not able to found a role with the provided arguments: '"+event.getArgs()+"'");
                return;
            }
            else if(list.size()>1)
            {
                event.replyWarning(FormatUtil.listOfRoles(list, event.getArgs()));
                return;
            }
            else role = list.get(0);


            if(!(colormeRoles.contains(role)))
            {
                event.replyError("That role isn't on the ColorMe roles list!");
                return;
            }

            bot.gsdm.removeColormeRole(guild, role);
            event.replySuccess("Successfully removed the role *"+role.getName()+"* from the ColorMe roles list.");
        }
    }

    private Color getColor(String hex)
    {
        if(hex.contains("000000"))
           hex = "#010101";

        try
        {
            return Color.decode(hex);
        }
        catch(NumberFormatException e)
        {
            return null;
        }
    }
}
