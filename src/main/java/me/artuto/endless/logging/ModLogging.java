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

import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.Action;
import me.artuto.endless.Bot;
import me.artuto.endless.Sender;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.core.entities.ParsedAuditLog;
import me.artuto.endless.utils.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audit.ActionType;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Artuto
 * Class used for ModLog actions.
 */

public class ModLogging
{
    private final Bot bot;
    private final HashMap<String, Message> banLogs = new HashMap<>();
    private final HashMap<Long,Integer> caseNum = new HashMap<>();

    // Parts
    private final String TIME = "`[%s]`";
    private final String CASE = " `[%d]`";
    private final String EMOTE = " %s";
    private final String AUTHOR = " **%s**#%s";
    private final String ACTION = " %s";
    private final String TARGET = " **%s**#%s (ID: %d)";
    private final String REASON = " \n`[ Reason ]` %s";
    private final String CRITERIA = " \n`[Criteria]` %s";
    private final String EXPIRY_TIME = " \n`[Duration]` %s";

    // Formats
    private final String GENERAL_FORMAT = TIME+CASE+EMOTE+AUTHOR+ACTION+TARGET+REASON;
    private final String CLEAN_FORMAT = TIME+CASE+EMOTE+AUTHOR+ACTION+" `%d` messages in <#%d>"+CRITERIA+REASON;
    private final String TEMP_FORMAT = TIME+CASE+EMOTE+AUTHOR+ACTION+TARGET+REASON+EXPIRY_TIME;

    public ModLogging(Bot bot)
    {
        this.bot = bot;
    }

    public void logClear(Action action, CommandEvent event, List<Message> messages, OffsetDateTime time, String crit, String reason)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = event.getClient().getSettingsFor(guild);
        TextChannel cleanLog = event.getJDA().asBot().getShardManager().getTextChannelById(470068055322525708L);
        TextChannel modlog = guild.getTextChannelById(gs.getModlog());
        User author = event.getAuthor();
        if(modlog==null || !(modlog.canTalk()) || LogUtils.isTypeIgnored(action.getInternalAction(), event.getTextChannel()) ||
                LogUtils.isIgnored(author.getIdLong(), modlog))
            return;

        getCaseNumberAsync(modlog, id -> {
            EmbedBuilder fileEmbed = new EmbedBuilder().setColor(guild.getSelfMember().getColor());
            MessageBuilder mb = new MessageBuilder();
            mb.setContent(FormatUtil.formatLogClean(CLEAN_FORMAT, time, gs.getTimezone(), id, action.getEmote(), author.getName(),
                    author.getDiscriminator(), action.getVerb(), messages.size(), event.getTextChannel().getIdLong(), crit, reason));
            Sender.sendMessage(modlog, mb.build(), m -> bot.endlessPool.submit(() -> {
                File file = LogUtils.createMessagesTextFile(messages, "Messages"+event.getTextChannel().getId()+".txt");
                if(!(file==null) && ChecksUtil.hasPermission(guild.getSelfMember(), modlog, Permission.MESSAGE_EMBED_LINKS))
                {
                    Message.Attachment att = cleanLog.sendFile(file).complete().getAttachments().get(0);
                    LogUtils.UploadedText text = new LogUtils.UploadedText(att.getId(), event.getTextChannel().getId());
                    fileEmbed.setDescription("[`\uD83D\uDCC4 View`]("+text.getViewUrl()+") | [`\uD83D\uDCE9 Download`]("+text.getCDNUrl()+")");
                    m.editMessage(mb.setEmbed(fileEmbed.build()).build()).queue(s -> file.delete(), e -> file.delete());
                }
            }));
        });
    }

    public void logGeneral(Action action, CommandEvent event, OffsetDateTime time, String reason, User target)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = event.getClient().getSettingsFor(guild);
        TextChannel modlog = guild.getTextChannelById(gs.getModlog());
        User author = event.getAuthor();
        if(modlog==null || !(modlog.canTalk()) || LogUtils.isTypeIgnored(action.getInternalAction(), modlog) || LogUtils.isIgnored(author.getIdLong(), modlog) ||
                LogUtils.isIgnored(target.getIdLong(), modlog))
            return;

        getCaseNumberAsync(modlog, id -> Sender.sendMessage(modlog, FormatUtil.formatLogGeneral(GENERAL_FORMAT, time, gs.getTimezone(),
                id, action.getEmote(), author.getName(), author.getDiscriminator(), action.getVerb(), target.getName(),
                target.getDiscriminator(), target.getIdLong(), reason), m -> {
            if(action==Action.BAN)
            {
                guild.getAuditLogs().type(ActionType.BAN).limit(3).queue(preEntries -> {
                    List<AuditLogEntry> entries = preEntries.stream().filter(ale -> ale.getTargetIdLong() == target.getIdLong()).collect(Collectors.toList());
                    if(entries.isEmpty())
                        return;

                    if(LogUtils.isIgnored(author.getIdLong(), modlog))
                        return;

                    banLogs.put(banCacheKey(entries.get(0), target), m);
                });
            }
        }));
    }

    public void logManual(Action action, Guild guild, OffsetDateTime time, String reason, User author, User target)
    {
        logManual(action, guild, time, reason, author, target, m -> {});
    }

    private void logManual(Action action, Guild guild, OffsetDateTime time, String reason, User author, User target, Consumer<Message> m)
    {
        if(!(bot.dataEnabled))
            return;

        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel modlog = guild.getTextChannelById(gs.getModlog());
        if(modlog==null || !(modlog.canTalk()) || LogUtils.isTypeIgnored(action.getInternalAction(), modlog) || LogUtils.isIgnored(author.getIdLong(), modlog) ||
                LogUtils.isIgnored(target.getIdLong(), modlog))
            return;

        getCaseNumberAsync(modlog, id -> Sender.sendMessage(modlog, FormatUtil.formatLogGeneral(GENERAL_FORMAT, time, gs.getTimezone(),
                id, action.getEmote(), author.getName(), author.getDiscriminator(), action.getVerb(), target.getName(),
                target.getDiscriminator(), target.getIdLong(), reason), m));
    }

    public void logTemp(Action action, CommandEvent event, int mins, OffsetDateTime time, String reason, User target)
    {
        if(!(bot.dataEnabled))
            return;

        Guild guild = event.getGuild();
        GuildSettings gs = event.getClient().getSettingsFor(guild);
        TextChannel modlog = guild.getTextChannelById(gs.getModlog());
        User author = event.getAuthor();
        if(modlog==null || !(modlog.canTalk()) || LogUtils.isTypeIgnored(action.getInternalAction(), modlog) || LogUtils.isIgnored(author.getIdLong(), modlog) ||
                LogUtils.isIgnored(target.getIdLong(), modlog))
            return;

        getCaseNumberAsync(modlog, id -> Sender.sendMessage(modlog, FormatUtil.formatLogTemp(TEMP_FORMAT, time, gs.getTimezone(),
                id, FormatUtil.formatTimeFromSeconds(mins*60), action.getEmote(), author.getName(), author.getDiscriminator(), action.getVerb(),
                target.getName(), target.getDiscriminator(), target.getIdLong(), reason)));
    }

    public void onGuildBan(GuildBanEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Action action = Action.MANUAL_BAN;
        Guild guild = event.getGuild();
        User target = event.getUser();

        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel modlog = guild.getTextChannelById(gs.getModlog());
        if(modlog==null || !(modlog.canTalk()) || LogUtils.isTypeIgnored(action.getInternalAction(), modlog) || LogUtils.isIgnored(target.getIdLong(), modlog))
            return;
        if(!(ChecksUtil.hasPermission(guild.getSelfMember(), null, Permission.VIEW_AUDIT_LOGS)))
            return;

        guild.getAuditLogs().type(ActionType.BAN).limit(3).queue(preEntries -> {
            List<AuditLogEntry> entries = preEntries.stream().filter(ale -> ale.getTargetIdLong()==target.getIdLong()).collect(Collectors.toList());
            if(entries.isEmpty())
                return;

            ParsedAuditLog parsedAuditLog = GuildUtils.getAuditLog(entries.get(0), null);
            if(parsedAuditLog==null)
                return;

            String reason = parsedAuditLog.getReason();
            User author = parsedAuditLog.getAuthor();
            if(author.isBot() ||  LogUtils.isIgnored(author.getIdLong(), modlog))
                return;

            logManual(action, guild, OffsetDateTime.now(), reason, author, target, m -> banLogs.put(banCacheKey(entries.get(0), target), m));
        });
    }

    public void onGuildMemberLeave(GuildMemberLeaveEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Action action = Action.MANUAL_KICK;
        Guild guild = event.getGuild();
        User target = event.getUser();

        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel modlog = guild.getTextChannelById(gs.getModlog());
        if(modlog==null || !(modlog.canTalk()) || LogUtils.isTypeIgnored(action.getInternalAction(), modlog) || LogUtils.isIgnored(target.getIdLong(), modlog))
            return;
        if(!(ChecksUtil.hasPermission(guild.getSelfMember(), null, Permission.VIEW_AUDIT_LOGS)))
            return;

        guild.getAuditLogs().type(ActionType.KICK).limit(3).queue(preEntries -> {
            List<AuditLogEntry> entries = preEntries.stream().filter(ale -> ale.getTargetIdLong()==target.getIdLong()).collect(Collectors.toList());
            if(entries.isEmpty())
                return;
            ParsedAuditLog parsedAuditLog = GuildUtils.getAuditLog(entries.get(0), null);
            if(parsedAuditLog==null)
                return;
            if(entries.get(0).getCreationTime().plusSeconds(5).isBefore(OffsetDateTime.now()))
                return;

            String reason = parsedAuditLog.getReason();
            User author = parsedAuditLog.getAuthor();
            if(author.isBot() ||  LogUtils.isIgnored(author.getIdLong(), modlog))
                return;

            logManual(action, guild, OffsetDateTime.now(), reason, author, target);
        });
    }

    public void onGuildUnban(GuildUnbanEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        Action action = Action.MANUAL_UNBAN;
        Guild guild = event.getGuild();
        User target = event.getUser();

        GuildSettings gs = bot.endless.getGuildSettings(guild);
        TextChannel modlog = guild.getTextChannelById(gs.getModlog());
        if(modlog==null || !(modlog.canTalk()) || LogUtils.isTypeIgnored(action.getInternalAction(), modlog) || LogUtils.isIgnored(target.getIdLong(), modlog))
            return;
        if(!(ChecksUtil.hasPermission(guild.getSelfMember(), null, Permission.VIEW_AUDIT_LOGS)))
            return;

        guild.getAuditLogs().type(ActionType.UNBAN).limit(3).queue(preEntries -> {
            List<AuditLogEntry> entries = preEntries.stream().filter(ale -> ale.getTargetIdLong()==target.getIdLong()).collect(Collectors.toList());
            if(entries.isEmpty())
                return;

            ParsedAuditLog parsedAuditLog = GuildUtils.getAuditLog(entries.get(0), null);
            if(parsedAuditLog==null)
                return;

            String reason = parsedAuditLog.getReason();
            User author = parsedAuditLog.getAuthor();
            if(author.isBot() ||  LogUtils.isIgnored(author.getIdLong(), modlog))
                return;

            guild.getAuditLogs().type(ActionType.BAN).limit(3).queue(preEntries2 -> {
                List<AuditLogEntry> ent2 = preEntries2.stream().filter(ale -> ale.getTargetIdLong()==target.getIdLong()).collect(Collectors.toList());
                if(ent2.isEmpty())
                {
                    logManual(action, guild, OffsetDateTime.now(), reason, author, target);
                    return;
                }

                ParsedAuditLog parsedAuditLog2 = GuildUtils.getAuditLog(ent2.get(0), null);
                if(parsedAuditLog2==null)
                    return;

                String reason2 = parsedAuditLog2.getReason();
                if(!(reason2.endsWith("Softban Unban")))
                {
                    String key = banCacheKey(entries.get(0), target);
                    Message banMsg = banLogs.get(key);
                    if(!(banMsg==null) && banMsg.getCreationTime().plusMinutes(2).isAfter(ent2.get(0).getCreationTime()))
                    {
                        banMsg.editMessage(banMsg.getContentRaw()
                                .replaceFirst(Action.BAN.getEmote(), Action.SOFTBAN.getEmote())
                                .replaceFirst(Action.BAN.getVerb(), Action.SOFTBAN.getVerb())).queue();
                    }
                }
            });
        });
    }

    public int updateCase(Guild guild, int num, String reason)
    {
        if(!(bot.dataEnabled))
            return -1;

        TextChannel modlog = guild.getTextChannelById(bot.endless.getGuildSettings(guild).getModlog());
        if(modlog==null)
            return -1;
        else if(!(modlog.canTalk()) || !(ChecksUtil.hasPermission(guild.getSelfMember(), modlog, Permission.MESSAGE_HISTORY)))
            return -2;
        List<Message> list = modlog.getHistory().retrievePast(100).complete();
        Message m = null;
        int thisCase = 0;
        for(Message msg: list)
        {
            thisCase = MiscUtils.isCase(msg, num);
            if(!(thisCase==0))
            {
                m = msg;
                break;
            }
        }
        if(m==null)
            return num==-1?-4:-3;
        m.editMessage(m.getContentRaw().replaceAll("(?is)\n`\\[ Reason \\]` .+", "\n`[ Reason ]` "+reason)).queue();
        return thisCase;
    }

    private int getCaseNumber(TextChannel tc) // not async
    {
        if(caseNum.containsKey(tc.getGuild().getIdLong()))
        {
            int num = caseNum.get(tc.getGuild().getIdLong());
            caseNum.put(tc.getGuild().getIdLong(), num+1);
            return num;
        }
        else
        {
            int num;
            for(Message m: tc.getHistory().retrievePast(100).complete())
            {
                num = getCaseNumber(m);
                if(num!=-1)
                {
                    caseNum.put(tc.getGuild().getIdLong(), num+2);
                    return num+1;
                }
            }
            caseNum.put(tc.getGuild().getIdLong(), 2);
            return 1;
        }
    }

    private void getCaseNumberAsync(TextChannel tc, Consumer<Integer> result)
    {
        if(caseNum.containsKey(tc.getGuild().getIdLong()))
        {
            int num = caseNum.get(tc.getGuild().getIdLong());
            caseNum.put(tc.getGuild().getIdLong(), num+1);
            result.accept(num);
        }
        else
        {
            tc.getHistory().retrievePast(100).queue(list ->
            {
                int num;
                for(Message m: list)
                {
                    num = getCaseNumber(m);
                    if(num!=-1)
                    {
                        caseNum.put(tc.getGuild().getIdLong(), num+2);
                        result.accept(num+1);
                        return;
                    }
                }
                caseNum.put(tc.getGuild().getIdLong(), 2);
                result.accept(1);
            });
        }
    }

    private int getCaseNumber(Message m)
    {
        if(!(m.getAuthor().getIdLong()==m.getJDA().getSelfUser().getIdLong()))
            return -1;
        if(!(m.getContentRaw().startsWith("`[")))
            return -1;
        try
        {
            return Integer.parseInt(m.getContentRaw().substring(15, m.getContentRaw().indexOf("]` ",15)));
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    private String banCacheKey(AuditLogEntry ale, User mod)
    {
        return ale.getGuild().getId()+"|"+ale.getTargetId()+"|"+mod.getId();
    }
}
