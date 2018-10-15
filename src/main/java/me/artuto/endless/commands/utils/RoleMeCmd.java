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

import java.util.List;

/**
 * @author Artuto
 */

public class RoleMeCmd extends EndlessCommand
{
    private final Bot bot;

    public RoleMeCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "roleme";
        this.help = "Self-assignable roles.";
        this.arguments = "[roleme role]";
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
        List<Role> rolemeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getRoleMeRoles();
        Member member = event.getMember();

        if(args.isEmpty())
        {
            if(rolemeRoles.isEmpty())
                event.replyWarning("command.roleme.empty");
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.append(":performing_arts: ").append(event.localize("command.roleme.list", guild.getName())).append("\n");

                for(Role r : rolemeRoles)
                    sb.append(Const.LINE_START).append(" ").append(r.getName()).append("\n");

                event.reply(false, sb.toString());
            }
        }
        else
        {
            Role role = ArgsUtils.findRole(event, args);
            if(role==null)
                return;

            if(rolemeRoles.contains(role))
            {
                if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), role)))
                    event.replyError("core.error.cantInteract.role.bot");
                else
                {
                    if(member.getRoles().contains(role))
                    {
                        guild.getController().removeSingleRoleFromMember(member, role).queue(s ->
                                event.replySuccess("command.roleme.removed", role.getName()), e -> {
                            event.replyError("command.roleme.error.removing");
                            Endless.LOG.error("Could not remove roleme role {} from member {}", role.getId(), member.getUser().getId(), e);
                        });
                    }
                    else
                    {
                        guild.getController().addSingleRoleToMember(member, role).queue(s ->
                                event.replySuccess("command.roleme.given"), e -> {
                            event.replyError("command.roleme.error.giving");
                            Endless.LOG.error("Could not add roleme role {} to member {}", role.getId(), member.getUser().getId(), e);
                        });
                    }
                }
            }
            else
                event.replyWarning("command.roleme.notEnabled");
        }
    }

    private class AddCmd extends EndlessCommand
    {
        AddCmd()
        {
            this.name = "add";
            this.help = "Adds a role to the list of available RoleMe roles.";
            this.arguments = "<role>";
            this.category = Categories.UTILS;
            this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.parent = RoleMeCmd.this;
        }

        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Guild guild = event.getGuild();
            List<Role> rolemeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getRoleMeRoles();
            String args = event.getArgs();
            Role role = ArgsUtils.findRole(event, args);
            if(role==null)
                return;

            if(rolemeRoles.contains(role))
            {
                event.replyError("command.roleme.add.alreadyAdded");
                return;
            }

            if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), role)))
                event.replyError("core.error.cantInteract.role.bot");
            else
            {
                bot.gsdm.addRolemeRole(guild, role);
                event.replySuccess("command.roleme.add.added", role.getName());
            }
        }
    }

    private class RemoveCmd extends EndlessCommand
    {
        RemoveCmd()
        {
            this.name = "remove";
            this.help = "Removes a role from the list of available RoleMe roles.";
            this.arguments = "<role>";
            this.category = Categories.UTILS;
            this.botPerms = new Permission[]{Permission.MANAGE_ROLES};
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.parent = RoleMeCmd.this;
        }

        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Guild guild = event.getGuild();
            List<Role> rolemeRoles = ((GuildSettings)event.getClient().getSettingsFor(guild)).getRoleMeRoles();
            String args = event.getArgs();
            Role role = ArgsUtils.findRole(event, args);
            if(role == null) return;

            if(!(rolemeRoles.contains(role)))
            {
                event.replyError("command.roleme.remove.notAdded");
                return;
            }

            bot.gsdm.removeRolemeRole(guild, role);
            event.replySuccess("command.roleme.remove.removed", role.getName());
        }
    }
}
