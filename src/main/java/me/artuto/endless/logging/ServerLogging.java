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

package me.artuto.endless.logging;

import com.jagrosh.jagtag.Parser;
import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.Sender;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.storage.tempdata.MessagesLogging;
import me.artuto.endless.utils.ChecksUtil;
import me.artuto.endless.utils.FormatUtil;
import me.artuto.endless.utils.LogUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;

import java.awt.*;
import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerLogging
{
    private final Bot bot;
    private final Parser parser;

    // Parts
    private final String TIME = "`[%s]`";
    private final String EMOTE = " %s";
    private final String USER = " **%s**#%s (ID: %d)";

    // Formats
    private final String GENERAL = TIME+EMOTE+USER;

    public ServerLogging(Bot bot)
    {
        this.bot = bot;
        this.parser = Bot.tagParser;
    }

    // Member events
    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        String toSend;
        String welcomeMsg = gs.getWelcomeMsg();
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
        TextChannel welcomeChan = guild.getTextChannelById(gs.getWelcomeChannel());
        User user = event.getUser();

        if(!(serverlog==null) && serverlog.canTalk() && !(LogUtils.isTypeIgnored("memberjoin", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog)))
        {
            OffsetDateTime now = OffsetDateTime.now();
            long seconds = user.getCreationTime().until(now, ChronoUnit.SECONDS);
            String MEMBER_JOIN = GENERAL+" joined the server. Member count: **%d**\nCreation: %s (%s ago)";
            toSend = String.format(MEMBER_JOIN, FormatUtil.timeF(now, gs.getTimezone()), ":inbox_tray:", user.getName(), user.getDiscriminator(),
                    user.getIdLong(), guild.getMemberCache().size(), user.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), FormatUtil.formatTimeFromSeconds(seconds));
            Sender.sendMessage(serverlog, toSend);
        }
        if(!(welcomeChan==null) && welcomeChan.canTalk() && !(welcomeMsg==null))
        {
            EmbedBuilder tagEmbed = new EmbedBuilder();
            parser.clear().put("user", user).put("guild", guild).put("channel", welcomeChan).put("builder", tagEmbed);
            toSend = parser.parse(welcomeMsg);
            if(!(tagEmbed.isEmpty()))
                Sender.sendMessage(welcomeChan, new MessageBuilder().setContent(FormatUtil.sanitize(toSend))
                        .setEmbed(tagEmbed.build()).build());
            else
                Sender.sendMessage(welcomeChan, toSend);
            parser.clear();
        }
    }

    public void onGuildMemberLeave(GuildMemberLeaveEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        Member member = event.getMember();
        String toSend;
        String leaveMsg = gs.getLeaveMsg();
        TextChannel leaveChan = guild.getTextChannelById(gs.getLeaveChannel());
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
        User user = event.getUser();

        if(!(serverlog==null) && serverlog.canTalk() && !(LogUtils.isTypeIgnored("memberleave", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog)))
        {
            OffsetDateTime now = OffsetDateTime.now();
            long seconds = member.getJoinDate().until(now, ChronoUnit.SECONDS);
            String MEMBER_LEAVE = GENERAL+" left or was kicked from the server. Member count: **%d**\nJoined: %s (%s ago)";
            toSend = String.format(MEMBER_LEAVE, FormatUtil.timeF(now, gs.getTimezone()), ":outbox_tray:", user.getName(), user.getDiscriminator(),
                    user.getIdLong(), guild.getMemberCache().size(), member.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), FormatUtil.formatTimeFromSeconds(seconds));
            Sender.sendMessage(serverlog, toSend);
        }
        if(!(leaveChan==null) && leaveChan.canTalk() && !(leaveMsg==null))
        {
            EmbedBuilder tagEmbed = new EmbedBuilder();
            parser.clear().put("user", user).put("guild", guild).put("channel", leaveChan).put("builder", tagEmbed);
            toSend = parser.parse(leaveMsg);
            if(!(tagEmbed.isEmpty()))
                Sender.sendMessage(leaveChan, new MessageBuilder().setContent(FormatUtil.sanitize(toSend))
                        .setEmbed(tagEmbed.build()).build());
            else
                Sender.sendMessage(leaveChan, toSend);
            parser.clear();
        }
    }

    // Message Events
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        TextChannel serverlog = event.getGuild().getTextChannelById(bot.endless.getGuildSettings(event.getGuild()).getServerlog());
        if(!(serverlog==null) && !(event.getAuthor().isBot()))
        {
            if(LogUtils.isIgnored(event.getAuthor().getIdLong(), serverlog) || LogUtils.isIgnored(event.getChannel().getIdLong(), serverlog))
                return;

            MessagesLogging.addMessage(event.getMessage().getIdLong(), event.getMessage());
        }
    }

    public void onGuildMessageUpdate(GuildMessageUpdateEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        Message newMsg = event.getMessage();
        Message oldMsg = MessagesLogging.getMsg(event.getMessageIdLong());
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
        User user = event.getAuthor();

        if(oldMsg==null || user.isBot())
            return;
        if(serverlog==null || !(serverlog.canTalk()) || LogUtils.isTypeIgnored("msgedit", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog))
            return;
        if(LogUtils.isIgnored(event.getChannel().getIdLong(), serverlog))
            return;
        if(!(ChecksUtil.hasPermission(guild.getSelfMember(), serverlog, Permission.MESSAGE_EMBED_LINKS)))
            return;
        if(newMsg.getContentRaw().equals(oldMsg.getContentRaw()))
            return;

        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        OffsetDateTime now = OffsetDateTime.now();
        StringBuilder attachments = new StringBuilder();
        String newC = newMsg.getContentRaw().length()>1024?newMsg.getContentRaw().substring(0, 1016)+" (...)":newMsg.getContentRaw();
        String oldC = oldMsg.getContentRaw().length()>1024?oldMsg.getContentRaw().substring(0, 1016)+" (...)":oldMsg.getContentRaw();
        String toSend;
        if(!(newMsg.getAttachments().isEmpty()))
        {
            for(Message.Attachment att : newMsg.getAttachments())
                attachments.append("\n").append(att.getUrl());
        }

        if(!(oldC.isEmpty()))
            builder.addField("From:", oldC, false);
        if(!(newC.isEmpty()))
            builder.addField("To:", newC, false);
        if(!(newMsg.getAttachments().isEmpty()))
            builder.addField("Attachments:", attachments.toString(), false);
        builder.setColor(Color.YELLOW).setFooter("Message ID: "+newMsg.getId(), null);

        String MESSAGE_EDITED = GENERAL+" edited a message in %s:";
        toSend = String.format(MESSAGE_EDITED, FormatUtil.timeF(now, gs.getTimezone()), ":pencil2:", user.getName(), user.getDiscriminator(), user.getIdLong(),
                event.getChannel().getAsMention());
        mb.setContent(FormatUtil.sanitize(toSend)).setEmbed(builder.build());
        Sender.sendMessage(serverlog, mb.build(), s -> MessagesLogging.addMessage(newMsg.getIdLong(), newMsg));
    }

    public void onGuildMessageDelete(GuildMessageDeleteEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        Message msg = MessagesLogging.getMsg(event.getMessageIdLong());
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());

        if(msg==null)
            return;
        User user = msg.getAuthor();
        if(serverlog==null || !(serverlog.canTalk()) || LogUtils.isTypeIgnored("msgdelete", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog))
            return;
        if(LogUtils.isIgnored(event.getChannel().getIdLong(), serverlog))
            return;
        if(!(ChecksUtil.hasPermission(guild.getSelfMember(), serverlog, Permission.MESSAGE_EMBED_LINKS)))
            return;

        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        OffsetDateTime now = OffsetDateTime.now();
        StringBuilder attachments = new StringBuilder();
        String msgC = msg.getContentRaw();
        String toSend;
        if(!(msg.getAttachments().isEmpty()))
        {
            for(Message.Attachment att : msg.getAttachments())
                attachments.append("\n").append(att.getUrl());
        }

        builder.setDescription(msgC);
        if(!(msg.getAttachments().isEmpty()))
            builder.addField("Attachments:", attachments.toString(), false);
        builder.setColor(Color.RED).setFooter("Message ID: "+msg.getId(), null);

        String MESSAGE_DELETED = GENERAL+"'s message was deleted in %s:";
        toSend = String.format(MESSAGE_DELETED, FormatUtil.timeF(now, gs.getTimezone()), ":wastebasket:", user.getName(), user.getDiscriminator(), user.getIdLong(),
                event.getChannel().getAsMention());
        mb.setContent(FormatUtil.sanitize(toSend)).setEmbed(builder.build());
        Sender.sendMessage(serverlog, mb.build(), s -> MessagesLogging.removeMessage(msg.getIdLong()));
    }

    public void onMessageBulkDeleteEvent(MessageBulkDeleteEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel channel = event.getChannel();
        TextChannel cleanLog = event.getJDA().asBot().getShardManager().getTextChannelById(470068055322525708L);
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
        List<Message> messages = new LinkedList<>();
        event.getMessageIds().stream().filter(id -> !(MessagesLogging.getMsg(Long.valueOf(id))==null)).
                forEach(id -> messages.add(MessagesLogging.getMsg(Long.valueOf(id))));

        if(serverlog==null || !(serverlog.canTalk()) || LogUtils.isTypeIgnored("bulkdelete", serverlog))
            return;
        if(LogUtils.isIgnored(event.getChannel().getIdLong(), serverlog))
            return;
        if(messages.isEmpty())
            return;

        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        OffsetDateTime now = OffsetDateTime.now();
        String BULK_DELETE = TIME+EMOTE+" **%d** messages have been deleted in %s (%d cached):";
        String toSend;
        toSend = String.format(BULK_DELETE, FormatUtil.timeF(now, gs.getTimezone()), "", event.getMessageIds().size(), channel.getAsMention(), messages.size());

        Sender.sendMessage(serverlog, toSend, m -> bot.endlessPool.submit(() -> {
            File f = LogUtils.createMessagesTextFile(messages, "Messages"+channel.getId()+".txt");
            if(!(f==null) && ChecksUtil.hasPermission(guild.getSelfMember(), serverlog, Permission.MESSAGE_EMBED_LINKS))
            {
                Message.Attachment att = cleanLog.sendFile(f).complete().getAttachments().get(0);
                LogUtils.UploadedText text = new LogUtils.UploadedText(att.getId(), channel.getId());
                builder.setDescription("[`\uD83D\uDCC4 View`]("+text.getViewUrl()+") | [`\uD83D\uDCE9 Download`]("+text.getCDNUrl()+")");
                m.editMessage(mb.setEmbed(builder.build()).build()).queue(s -> f.delete(), e -> f.delete());
            }
        }));
    }

    // User Event
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        List<Guild> guilds = event.getUser().getMutualGuilds().stream().filter(g -> !(bot.endless.getGuildSettings(g).getServerlog()==0L)).collect(Collectors.toList());
        if(guilds.isEmpty())
            return;
        User user = event.getUser();
        if(user.isBot())
            return;
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        OffsetDateTime now = OffsetDateTime.now();

        for(Guild guild : guilds)
        {
            GuildSettings gs = bot.endless.getGuildSettings(guild);
            String toSend;
            TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
            if(serverlog==null || !(serverlog.canTalk()) || LogUtils.isTypeIgnored("avatarchange", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog))
                return;
            if(!(ChecksUtil.hasPermission(guild.getSelfMember(), serverlog, Permission.MESSAGE_EMBED_LINKS)))
                return;
            builder.setImage("attachment://avatarchange"+user.getId()+".png");
            builder.setColor(guild.getSelfMember().getColor());

            File f = LogUtils.getAvatarUpdateImage(event);
            if(f==null)
                return;

            String AVATAR_CHANGED = GENERAL+"'s avatar has changed:";
            toSend = String.format(AVATAR_CHANGED, FormatUtil.timeF(now, gs.getTimezone()), ":frame_photo:", user.getName(), user.getDiscriminator(), user.getIdLong());
            mb.setContent(FormatUtil.sanitize(toSend)).setEmbed(builder.build());
            Sender.sendFile(serverlog, f, mb.build(), s -> {
                if(guild.equals(guilds.get(guilds.size()-1)))
                    f.delete();
            });
        }
    }

    // Voice Events
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
        User user = event.getMember().getUser();
        VoiceChannel joined = event.getChannelJoined();

        if(user.isBot())
            return;
        if(serverlog==null || !(serverlog.canTalk()) || LogUtils.isTypeIgnored("voicejoin", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog))
            return;
        if(LogUtils.isIgnored(joined.getIdLong(), serverlog))
            return;

        OffsetDateTime now = OffsetDateTime.now();
        String toSend;

        String VOICE_JOINED = GENERAL+" connected to the voice channel **%s**";
        toSend = String.format(VOICE_JOINED, FormatUtil.timeF(now, gs.getTimezone()), Const.VOICE_JOIN, user.getName(), user.getDiscriminator(), user.getIdLong(),
                joined.getName());
        Sender.sendMessage(serverlog, FormatUtil.sanitize(toSend));
    }

    public void onGuildVoiceMove(GuildVoiceMoveEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
        User user = event.getMember().getUser();
        VoiceChannel left = event.getChannelLeft();
        VoiceChannel joined = event.getChannelJoined();

        if(user.isBot())
            return;
        if(serverlog==null || !(serverlog.canTalk()) || LogUtils.isTypeIgnored("voicemove", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog))
            return;
        if(LogUtils.isIgnored(joined.getIdLong(), serverlog) || LogUtils.isIgnored(left.getIdLong(), serverlog))
            return;

        OffsetDateTime now = OffsetDateTime.now();
        String toSend;

        String VOICE_MOVED = GENERAL+" moved from **%s** to **%s**";
        toSend = String.format(VOICE_MOVED, FormatUtil.timeF(now, gs.getTimezone()), Const.VOICE_MOVE, user.getName(), user.getDiscriminator(), user.getIdLong(),
                left.getName(), joined.getName());
        Sender.sendMessage(serverlog, FormatUtil.sanitize(toSend));
    }

    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel serverlog = guild.getTextChannelById(gs.getServerlog());
        User user = event.getMember().getUser();
        VoiceChannel left = event.getChannelLeft();

        if(user.isBot())
            return;
        if(serverlog==null || !(serverlog.canTalk()) || LogUtils.isTypeIgnored("voiceleave", serverlog) || LogUtils.isIgnored(user.getIdLong(), serverlog))
            return;
        if(LogUtils.isIgnored(left.getIdLong(), serverlog))
            return;

        OffsetDateTime now = OffsetDateTime.now();
        String toSend;

        String VOICE_LEAVE = GENERAL+" disconnected from the voice channel **%s**";
        toSend = String.format(VOICE_LEAVE, FormatUtil.timeF(now, gs.getTimezone()), Const.VOICE_LEAVE, user.getName(), user.getDiscriminator(), user.getIdLong(),
                left.getName());
        Sender.sendMessage(serverlog, FormatUtil.sanitize(toSend));
    }
}
