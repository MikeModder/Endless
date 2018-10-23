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

package me.artuto.endless.commands.tools;

import me.artuto.endless.Const;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.utils.ArgsUtils;
import me.artuto.endless.utils.FormatUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Artuto
 */

public class UserInfoCmd extends EndlessCommand
{
    public UserInfoCmd()
    {
        this.name = "user";
        this.aliases = new String[]{"member", "userinfo", "i", "info", "memberinfo", "whois"};
        this.help = "Shows info about the specified user";
        this.arguments = "<user>";
        this.category = Categories.TOOLS;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        StringBuilder sb = new StringBuilder();

        Member member;
        User user = event.getArgs().isEmpty()?event.getAuthor():ArgsUtils.findUser(false, event, event.getArgs());
        if(user==null)
            return;

        Game game = user.getMutualGuilds().get(0).getMember(user).getGame();
        String status = getStatus(event, user.getMutualGuilds().get(0).getMember(user).getOnlineStatus());
        String statusEmote = getStatusEmote(user.getMutualGuilds().get(0).getMember(user));
        String gameName;

        sb.append(Const.LINE_START).append(" ID: **").append(user.getId()).append("**\n");

        if(event.isFromType(ChannelType.TEXT))
        {
            member = event.getGuild().getMember(user);
            if(member==null)
                member = user.getMutualGuilds().get(0).getMember(user);
            game = member.getGame();
            status = getStatus(event, member.getOnlineStatus());
            statusEmote = getStatusEmote(member);
            sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.status")).append(": ").append(statusEmote)
                    .append(" **").append(status).append("**");
            if(!(game==null))
            {
                if(game.getUrl()==null)
                    gameName = "*"+game.getName()+"*";
                else
                    gameName = "*("+game.getUrl()+")["+game.getUrl()+"]*";
                
                sb.append(" (").append(getGame(event, game.getType().getKey())).append(" ").append(gameName).append(")");
            }
            sb.append("\n");

            if(!(event.getGuild().getMember(user)==null))
            {
                if(!(member.getNickname()==null))
                {
                    sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.nick")).append(": **")
                            .append(member.getNickname()).append("**\n");
                }
                String roles = member.getRoles().isEmpty()?"":member.getRoles().stream().map(IMentionable::getAsMention)
                        .collect(Collectors.joining(", "));
                if(!(roles.isEmpty()))
                    sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.roles")).append(": ").append(roles).append("\n");
                sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.joinDate")).append(": **")
                        .append(member.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("**\n");
                sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.creationDate")).append(": **")
                        .append(user.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("**\n");

                StringBuilder strjoins;
                List<Member> joins = new ArrayList<>(event.getGuild().getMembers());
                joins.sort(Comparator.comparing(Member::getJoinDate));
                int index = joins.indexOf(member);
                int joinnumber = index;
                index -= 3;
                if(index<0) index = 0;

                if(joins.get(index).equals(member))
                    strjoins = new StringBuilder("**"+joins.get(index).getUser().getName()+"**");
                else
                    strjoins = new StringBuilder(joins.get(index).getUser().getName());

                for(int i = index+1; i<index+7; i++)
                {
                    if(i>=joins.size()) break;

                    Member m = joins.get(i);
                    String name = m.getUser().getName();

                    if(m.equals(member)) name = "**"+name+"**";

                    strjoins.append(" > ").append(name);
                }
                sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.joinOrder")).append(": ").append("`(#")
                    .append(joinnumber+1).append(")` ").append(strjoins).append("\n");
                builder.setColor(member.getColor());
            }
            else
                sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.creationDate")).append(": **")
                        .append(user.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("**\n");
        }
        else
        {
            sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.status")).append(": ")
                    .append(statusEmote).append(" **").append(status).append("**");
            if(!(game==null))
            {
                if(game.getUrl()==null)
                    gameName = "*"+game.getName()+"*";
                else
                    gameName = "*("+game.getUrl()+")["+game.getUrl()+"]*";
                
                sb.append(" (").append(getGame(event, game.getType().getKey())).append(" ").append(gameName).append(")");
            }
            sb.append("\n");
            sb.append(Const.LINE_START).append(" ").append(event.localize("command.user.creationDate")).append(": **")
                    .append(user.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("**\n");
        }

        builder.setDescription(sb).setThumbnail(user.getEffectiveAvatarUrl());
        boolean nitro = !(user.getAvatarId()==null) && user.getAvatarId().startsWith("a_");
        String title = FormatUtil.sanitize(event.localize("command.user.title", user.isBot()?Const.BOT:Const.PEOPLE,
                user.getName()+"**#**"+user.getDiscriminator(), (nitro?Const.NITRO+":":":")));
        event.reply(mb.setContent(title).setEmbed(builder.build()).build());
    }

    private String getGame(EndlessCommandEvent event, int type)
    {
        switch(type)
        {
            case 0:
                return event.localize("misc.playing");
            case 1:
                return event.localize("misc.streaming");
            case 2:
                return event.localize("misc.listening");
            case 3:
                return event.localize("misc.watching");
            default:
                return event.localize("misc.playing");
        }
    }

    private String getStatusEmote(Member member)
    {
        if(!(member.getGame()==null) && member.getGame().getType()==Game.GameType.STREAMING)
            return Const.STREAMING;

        switch(member.getOnlineStatus())
        {
            case ONLINE:
                return Const.ONLINE;
            case IDLE:
                return Const.IDLE;
            case DO_NOT_DISTURB:
                return Const.DND;
            case OFFLINE:
                return Const.OFFLINE;
            default:
                return Const.INVISIBLE;
        }
    }

    private String getStatus(EndlessCommandEvent event, OnlineStatus status)
    {
        switch(status)
        {
            case ONLINE:
                return event.localize("misc.online");
            case IDLE:
                return event.localize("misc.idle");
            case DO_NOT_DISTURB:
                return event.localize("misc.dnd");
            case OFFLINE:
                return event.localize("misc.offline");
            default:
                return event.localize("misc.invisible");
        }
    }
}
