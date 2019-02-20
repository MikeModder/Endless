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

package me.artuto.endless.commands.cmddata;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.Const;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.music.MusicCommand;
import me.artuto.endless.utils.ChecksUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Artuto
 */

public class CommandHelper
{
    public static void getHelp(CommandEvent event)
    {
        EmbedBuilder eBuilder = new EmbedBuilder();
        eBuilder.setColor(Color.decode("#33ff00"));
        String args = event.getArgs().trim();
        String prefix = event.getClient().getPrefix();

        if(args.isEmpty())
        {
            eBuilder.addField(Const.ENDLESS+" Bot Commands:", prefix+"help bot", false);
            if(event.isOwner())
                eBuilder.addField(Const.BOTADM+" Bot Administrator Commands:", prefix+"help botadm", false);
            eBuilder.addField(Const.GIPHY+" Fun Commands:", prefix+"help fun", false);
            if(event.isFromType(ChannelType.TEXT))
            {
                eBuilder.addField(Const.BAN+" Moderation Commands:", prefix+"help moderation", false);
                eBuilder.addField(Const.MUSIC+" Music Commands:", prefix+"help music", false);
                eBuilder.addField(Const.MUSIC+" Music DJ Commands:", prefix+"help musicdj", false);
                eBuilder.addField(Const.SERVER_SETTINGS+" Server Settings Commands:", prefix+"help settings", false);
            }
            eBuilder.addField(Const.PEOPLE+" Tool Commands:", prefix+"help tools", false);
            eBuilder.addField(Const.GOOGLE+" Util Commands:", prefix+"help utils", false);
        }
        else
        {
            args = args.split("\\s", 2)[0];
            if(args.equalsIgnoreCase("bot"))
                getHelpCategoryEmbed(Categories.BOT, event, eBuilder);
            else if(args.equalsIgnoreCase("botadm"))
                if(event.isOwner())
                    getHelpCategoryEmbed(Categories.BOTADM, event, eBuilder);
                else
                {
                    event.replyError("This Category is only available for Bot Administrators!");
                    return;
                }
            else if(args.equalsIgnoreCase("fun"))
                getHelpCategoryEmbed(Categories.FUN, event, eBuilder);
            else if(args.equalsIgnoreCase("moderation"))
            {
                if(event.isFromType(ChannelType.TEXT))
                    getHelpCategoryEmbed(Categories.MODERATION, event, eBuilder);
                else
                {
                    event.replyError("This Category is only available in a Guild!");
                    return;
                }
            }
            else if(args.equalsIgnoreCase("music"))
            {
                if(event.isFromType(ChannelType.TEXT))
                    getHelpCategoryEmbed(Categories.MUSIC, event, eBuilder);
                else
                {
                    event.replyError("This Category is only available in a Guild!");
                    return;
                }
            }
            else if(args.equalsIgnoreCase("musicdj"))
            {
                if(event.isFromType(ChannelType.TEXT))
                    getHelpCategoryEmbed(MusicCommand.DJ, event, eBuilder);
                else
                {
                    event.replyError("This Category is only available in a Guild!");
                    return;
                }
            }
            else if(args.equalsIgnoreCase("settings"))
                if(event.isFromType(ChannelType.TEXT))
                    getHelpCategoryEmbed(Categories.SERVER_CONFIG, event, eBuilder);
                else
                {
                    event.replyError("This Category is only available in a Guild!");
                    return;
                }
            else if(args.equalsIgnoreCase("tools"))
                getHelpCategoryEmbed(Categories.TOOLS, event, eBuilder);
            else if(args.equalsIgnoreCase("utils"))
                getHelpCategoryEmbed(Categories.UTILS, event, eBuilder);
            else if(args.equalsIgnoreCase("support"))
                getSupport(event, eBuilder);
            else
            {
                event.replyWarning("Category not found!");
                return;
            }
        }

        if(!(eBuilder.isEmpty()))
            event.replyInDm(new MessageBuilder(Const.ENDLESS+" Endless Help:").setEmbed(eBuilder.build()).build(), s -> event.reactSuccess(),
                    e -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
        else
            event.replyWarning("Commands not available for this category! (Missing permissions?)");
    }

    public static void getHelpBiConsumer(CommandEvent event, Command preCmd)
    {
        EndlessCommand command = (EndlessCommand)preCmd;
        String prefix = event.getClient().getPrefix();
        StringBuilder sb = new StringBuilder("**Help for `");
        String[] aliases = command.getAliases();
        Command[] children = command.getChildren();
        sb.append(command.getName()).append("` (");

        if(event.isFromType(ChannelType.TEXT))
            sb.append(event.getTextChannel().getAsMention()).append("):**\n");
        else
            sb.append("Direct Message):**\n");

        sb.append("**Usage:** `").append(prefix).append(command.getParent()==null?"":command.getParent().getName()+" ")
                .append(command.getName()).append(" ").append(command.getArguments()==null? "":command.getArguments()).append("`\n");

        if(!(aliases.length==0))
        {
            StringBuilder aliasesBuilder = new StringBuilder("**Aliases:** ");
            Arrays.stream(aliases).forEach(a -> aliasesBuilder.append("`").append(a).append("` "));
            sb.append(aliasesBuilder).append("\n");
        }

        sb.append("*").append(command.getHelp()).append("*\n\n");

        if(!(children.length==0))
        {
            StringBuilder childrenBuilder = new StringBuilder("**Subcommands:**");
            Arrays.stream(children).filter(preC -> {
                EndlessCommand c = (EndlessCommand)preC;
                if(event.isFromType(ChannelType.TEXT))
                {
                    if(ChecksUtil.hasPermission(event.getMember(), event.getTextChannel(), c.getUserPerms()))
                        return true;
                    else if(event.isOwner())
                        return true;
                    else
                        return false;
                }
                else if(c.isGuildOnly())
                    return false;
                return true;
            }).forEach(c -> {
                childrenBuilder.append("\n`").append(prefix).append(command.getName()).append(" "
                ).append(c.getName()).append(" ").append(c.getArguments()==null?"":c.getArguments()).append("` - *").append(c.getHelp()).append("*");
            });
            if(!(childrenBuilder.toString().replace("**Subcommands:**", "").length()==0))
                sb.append(childrenBuilder).append("\n");
        }

        event.replyInDm(sb.toString(), s -> event.reactSuccess(),
                e -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
    }

    private static void getHelpCategoryEmbed(Command.Category cat, CommandEvent event, EmbedBuilder eBuilder)
    {
        List<Command> cmds = event.getClient().getCommands().stream().filter(c -> c.getCategory().equals(cat))
                .filter(preCmd -> {
                    EndlessCommand c = (EndlessCommand)preCmd;
                    if(event.isFromType(ChannelType.TEXT))
                    {
                        if(ChecksUtil.hasPermission(event.getMember(), event.getTextChannel(), c.getUserPerms()))
                            return true;
                        else if(event.isOwner())
                            return true;
                        else
                            return false;
                    }
                    else if(c.isGuildOnly())
                        return false;
                    return true;
                }).collect(Collectors.toList());

        String prefix = event.getClient().getPrefix();
        for(Command cmd : cmds)
            eBuilder.addField(prefix+cmd.getName()+" "+(cmd.getArguments()==null?"":cmd.getArguments()), cmd.getHelp(), false);

        if(!(eBuilder.isEmpty()))
            eBuilder.setFooter("For support type "+prefix+"help support", null);
    }

    private static void getSupport(CommandEvent event, EmbedBuilder eBuilder)
    {
        eBuilder.setColor(Color.blue);
        eBuilder.setFooter(null, null);
        eBuilder.setDescription(Const.INFO+" Need support? Join the official server, where you can get, support (woah), " +
                "announcements about the bot, updates of the bot, request features or report bugs.\n\n" +
                ":interrobang: **Invite:** [Support Server]("+Const.INVITE+")\n" +
                Const.GITHUB+" **GitHub:** [GitHub Repository](https://github.com/MikeModder/Endless)\n" +
                ":link: **Bot Invite:** [Bot Invite]("+event.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR)+")");
    }
}
