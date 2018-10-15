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

import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.Endless;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.utils.ArgsUtils;
import me.artuto.endless.utils.ChecksUtil;
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
        this.children = new EndlessCommand[]{new AddCmd(), new RemoveCmd()};
        this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(!(bot.dataEnabled))
        {
            event.replyError("core.data.disabled");
            return;
        }

        String args = event.getArgs();
        Guild guild = event.getGuild();
        List<Role> colorMeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getColorMeRoles();
        Member member = event.getMember();

        if(args.isEmpty())
        {
            if(colorMeRoles.isEmpty())
                event.replyWarning("command.colorme.empty");
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.append(":performing_arts: ").append(event.localize("command.colorme.list", guild.getName())).append("\n");

                for(Role r : colorMeRoles)
                    sb.append(Const.LINE_START).append(" ").append(r.getName()).append("\n");

                event.reply(sb.toString());
            }
        }
        else
        {
            Color color = getColor(args);
            Role role = member.getRoles().stream().filter(r -> !(r.getColor()==null)).findFirst().orElse(null);
            if(color==null)
            {
                event.replyError("command.colorme.invalidHex");
                return;
            }
            if(role==null)
            {
                event.replyError("command.colorme.noColorRoles");
                return;
            }

            if(colorMeRoles.contains(role))
            {
                if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), role)))
                    event.replyError("core.error.cantInteract.role.bot");
                else
                {
                    User author = event.getAuthor();
                    role.getManager().setColor(color).reason(author.getName()+"#"+author.getDiscriminator()+": ColorMe").queue(s ->
                            event.replySuccess("command.colorme.success", role.getName()+"'s", args), e -> {
                        event.replyError("command.colorme.error", role.getName()+"'s", event.getArgs());
                        Endless.LOG.error("Error while changing the color of role {}", role.getId(), e);
                    });
                }
            }
            else
                event.replyWarning("command.colorme.notEnabled", role.getName());
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
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Guild guild = event.getGuild();
            List<Role> colormeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getColorMeRoles();
            String args = event.getArgs();
            Role role = ArgsUtils.findRole(event, args);
            if(role==null)
                return;

            if(colormeRoles.contains(role))
            {
                event.replyError("command.colorme.add.alreadyAdded");
                return;
            }

            if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), role)))
                event.replyError("core.error.cantInteract.role.bot");
            else
            {
                bot.gsdm.addColormeRole(guild, role);
                event.replySuccess("command.colorme.add.added", role.getName());
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
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Guild guild = event.getGuild();
            List<Role> colormeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getColorMeRoles();
            String args = event.getArgs();
            Role role = ArgsUtils.findRole(event, args);
            if(role==null)
                return;

            if(!(colormeRoles.contains(role)))
            {
                event.replyError("command.colorme.remove.notAdded");
                return;
            }

            if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), role)))
                event.replyError("core.error.cantInteract.role.bot");
            else
            {
                bot.gsdm.removeColormeRole(guild, role);
                event.replySuccess("command.colorme.remove.removed", role.getName());
            }
        }
    }

    private Color getColor(String hex)
    {
        try {return Color.decode(hex.startsWith("#")?hex:"#"+hex);}
        catch(NumberFormatException ignored) {return null;}
    }
}
