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

package me.artuto.endless.handlers;

import com.vdurmont.emoji.EmojiParser;
import me.artuto.endless.Bot;
import me.artuto.endless.core.entities.StarboardMessage;
import me.artuto.endless.storage.data.managers.StarboardDataManager;
import me.artuto.endless.utils.ChecksUtil;
import me.artuto.endless.utils.FinderUtil;
import me.artuto.endless.utils.FormatUtil;
import me.artuto.endless.utils.GuildUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Artuto
 */

public class StarboardHandler
{
    private static final String GENERAL = " \\*\\*(\\d+)\\*\\* <#(\\d{17,20})> ID: (\\d{17,20})";
    private static final Pattern EMOTE_PATTERN = Pattern.compile(Message.MentionType.EMOTE.getPattern()+GENERAL);
    private static final Pattern EMOJI_PATTERN = Pattern.compile("(:\\w+:|.*u\\w+|.*u\\w+.*u\\w+)"+GENERAL);
    private static final ScheduledExecutorService thread = Bot.getInstance().starboardThread;
    private static final StarboardDataManager sdm = Bot.getInstance().sdm;

    public static void checkAddReaction(GuildMessageReactionAddEvent event)
    {
        if(!(Bot.getInstance().dataEnabled))
            return;

        thread.submit(() -> {
            Guild guild = event.getGuild();

            if(!(event.getChannel().getTopic() == null) && event.getChannel().getTopic().toLowerCase().contains("{ignore:starboard}"))
                return;

            if(!(isConfigured(guild))) return;

            TextChannel starboard = GuildUtils.getStarboardChannel(event.getGuild());
            Message starredMsg = getMessage(event.getMessageIdLong(), event.getChannel());
            if(starredMsg==null)
                return;

            String emote = Bot.getInstance().endless.getGuildSettings(guild).getStarboardEmote();
            MessageReaction.ReactionEmote re = event.getReactionEmote();
            if(isSameAuthor(starredMsg.getAuthor(), event.getUser()) && (re.isEmote()?re.getEmote().getId().equals(emote):re.getName().equals(emote)))
                return;

            if(!(amountPassed(starredMsg))) return;

            if(!(starboard.canTalk()))
            {
                FinderUtil.getDefaultChannel(guild).sendMessage("I can't talk on the starboard!").queue(null,
                        e -> guild.getOwner().getUser().openPrivateChannel().queue(s -> s.sendMessage("I can't talk on the starboard!")
                                .queue(null, null)));
                return;
            }

            if(event.getChannel().getIdLong()==starboard.getIdLong())
            {
                boolean isUnicode = false;
                String content = starredMsg.getContentRaw();
                String unicode;
                Pattern REGEX;
                if(starredMsg.getEmotes().isEmpty())
                {
                    isUnicode = true;
                    REGEX = EMOJI_PATTERN;
                    unicode = getUnicode(emote);
                    content = content.replace(emote, unicode);
                }
                else
                    REGEX = EMOTE_PATTERN;
                Matcher m = REGEX.matcher(content);
                if(m.matches() && starredMsg.getAuthor().getIdLong()==event.getJDA().getSelfUser().getIdLong())
                {
                    String channelId;
                    String msgId;
                    if(isUnicode)
                    {
                        channelId = m.group(3);
                        msgId = m.group(4);
                    }
                    else
                    {
                        channelId = m.group(4);
                        msgId = m.group(5);
                    }
                    TextChannel originalTc = guild.getTextChannelById(channelId);
                    if(originalTc==null || !(ChecksUtil.hasPermission(guild.getSelfMember(), originalTc, Permission.MESSAGE_HISTORY)))
                        return;

                    Message originalMsg = originalTc.getMessageById(msgId).complete();
                    MessageReaction reaction = originalMsg.getReactions().stream().filter(r -> {
                        MessageReaction.ReactionEmote reE = r.getReactionEmote();
                        return (reE.isEmote()?reE.getId().equals(emote):reE.getName().equals(emote));
                    }).findFirst().orElse(null);
                    if(reaction==null || reaction.getUsers().complete().contains(event.getUser()))
                        return;
                    if(isSameAuthor(originalMsg.getAuthor(), event.getUser()) && (re.isEmote()?re.getId().equals(emote):re.getName().equals(emote)))
                        return;
                    int count = getStarCount(originalMsg)+getStarCount(starredMsg);

                    sdm.updateCount(originalMsg.getIdLong(), count);
                    updateCount(starredMsg, sdm.getStarboardMessage(originalMsg.getIdLong()).getStarboardMessageIdLong(), count);
                }
                else
                {
                    if(existsOnStarboard(starredMsg.getIdLong()))
                    {
                        sdm.updateCount(starredMsg.getIdLong(), getStarCount(starredMsg));
                        updateCount(starredMsg, sdm.getStarboardMessage(starredMsg.getIdLong()).getStarboardMessageIdLong(), getStarCount(starredMsg));
                    }
                   else
                       addMessage(starredMsg, starboard);
                }
            }
            else
            {
                if(existsOnStarboard(starredMsg.getIdLong()))
                {
                    sdm.updateCount(starredMsg.getIdLong(), getStarCount(starredMsg));
                    updateCount(starredMsg, sdm.getStarboardMessage(starredMsg.getIdLong()).getStarboardMessageIdLong(), getStarCount(starredMsg));
                }
                else
                    addMessage(starredMsg, starboard);
            }
        });
    }

    public static void checkRemoveReaction(GuildMessageReactionRemoveEvent event)
    {
        if(!(Bot.getInstance().dataEnabled))
            return;

        Guild guild = event.getGuild();

        thread.submit(() -> {
            if(!(isConfigured(event.getGuild()))) return;

            Message starredMsg = getMessage(event.getMessageIdLong(), event.getChannel());
            if(starredMsg==null)
                return;

            String emote = Bot.getInstance().endless.getGuildSettings(guild).getStarboardEmote();
            MessageReaction.ReactionEmote re = event.getReactionEmote();
            StarboardMessage starboardMsg;
            TextChannel starboard = GuildUtils.getStarboardChannel(event.getGuild());

            if(event.getChannel().getIdLong()==starboard.getIdLong())
            {
                boolean isUnicode = false;
                String content = starredMsg.getContentRaw();
                String unicode;
                Pattern REGEX;
                if(starredMsg.getEmotes().isEmpty())
                {
                    isUnicode = true;
                    REGEX = EMOJI_PATTERN;
                    unicode = getUnicode(emote);
                    content = content.replace(emote, unicode);
                }
                else
                    REGEX = EMOTE_PATTERN;
                Matcher m = REGEX.matcher(content);
                if(m.matches() && starredMsg.getAuthor().getIdLong()==event.getJDA().getSelfUser().getIdLong())
                {
                    String channelId;
                    String msgId;
                    if(isUnicode)
                    {
                        channelId = m.group(3);
                        msgId = m.group(4);
                    }
                    else
                    {
                        channelId = m.group(4);
                        msgId = m.group(5);
                    }
                    TextChannel originalTc = guild.getTextChannelById(channelId);
                    if(originalTc==null || !(ChecksUtil.hasPermission(guild.getSelfMember(), originalTc, Permission.MESSAGE_HISTORY)))
                        return;

                    Message originalMsg = originalTc.getMessageById(msgId).complete();
                    starboardMsg = sdm.getStarboardMessage(originalMsg.getIdLong());
                    if(isSameAuthor(originalMsg.getAuthor(), event.getUser()) && (re.isEmote()?re.getId().equals(emote):re.getName().equals(emote)))
                        return;
                    int count = getStarCount(originalMsg)+getStarCount(starredMsg);

                    if(!(count>=GuildUtils.getStarboardCount(guild)))
                    {
                        delete(starboard, starboardMsg);
                        return;
                    }

                    sdm.updateCount(originalMsg.getIdLong(), count);
                    updateCount(starredMsg, sdm.getStarboardMessage(originalMsg.getIdLong()).getStarboardMessageIdLong(), count);
                }
            }

            if(existsOnStarboard(starredMsg.getIdLong()))
            {
                starboardMsg = sdm.getStarboardMessage(starredMsg.getIdLong());
                if(!(amountPassed(starredMsg)))
                {
                    delete(starboard, starboardMsg);
                    return;
                }

                sdm.updateCount(starredMsg.getIdLong(), getStarCount(starredMsg));
                updateCount(starredMsg, sdm.getStarboardMessage(starredMsg.getIdLong()).getStarboardMessageIdLong(), getStarCount(starredMsg));
            }
        });
    }

    public static void checkRemoveAllReactions(GuildMessageReactionRemoveAllEvent event)
    {
        if(!(Bot.getInstance().dataEnabled))
            return;

        Guild guild = event.getGuild();
        TextChannel starboard = GuildUtils.getStarboardChannel(guild);
        thread.submit(() -> {
            if(event.getChannel().getIdLong()==starboard.getIdLong())
            {
                Message starredMsg = getMessage(event.getMessageIdLong(), event.getChannel());
                if(starredMsg==null)
                    return;

                Pattern REGEX;
                if(starredMsg.getEmotes().isEmpty())
                    REGEX = EMOJI_PATTERN;
                else
                    REGEX = EMOTE_PATTERN;
                String content = starredMsg.getContentRaw();
                Matcher m = REGEX.matcher(content);
                if(m.matches() && starredMsg.getAuthor().getIdLong()==event.getJDA().getSelfUser().getIdLong())
                {
                    TextChannel originalTc = guild.getTextChannelById(m.group(3));
                    if(originalTc == null || !(ChecksUtil.hasPermission(guild.getSelfMember(), originalTc, Permission.MESSAGE_HISTORY)))
                        return;

                    Message originalMsg = originalTc.getMessageById(m.group(4)).complete();
                    StarboardMessage starboardMsg = sdm.getStarboardMessage(originalMsg.getIdLong());
                    delete(starboard, starboardMsg);
                }
            }
            else
                thread.submit(() -> check(event.getGuild(), event.getMessageIdLong()));
        });
    }

    public static void checkDeleteMessage(GuildMessageDeleteEvent event)
    {
        if(!(Bot.getInstance().dataEnabled))
            return;

        thread.submit(() -> check(event.getGuild(), event.getMessageIdLong()));
    }

    private static boolean isSameAuthor(User msgAuthor, User user)
    {
        return msgAuthor.equals(user);
    }

    private static boolean isConfigured(Guild guild)
    {
        if(!(Bot.getInstance().dataEnabled))
            return false;

        return !(GuildUtils.getStarboardChannel(guild) == null) && !(GuildUtils.getStarboardCount(guild) == 0);
    }

    private static boolean amountPassed(Message msg)
    {
        return getStarCount(msg) >= GuildUtils.getStarboardCount(msg.getGuild());
    }

    private static int getStarCount(Message msg)
    {
        if(!(Bot.getInstance().dataEnabled))
            return 0;

        List<MessageReaction> reactions = msg.getReactions().stream().filter(r -> {
            String emote = Bot.getInstance().endless.getGuildSettings(r.getGuild()).getStarboardEmote();
            MessageReaction.ReactionEmote re = r.getReactionEmote();
            return (re.isEmote()?re.getId().equals(emote):re.getName().equals(emote));
        }).collect(Collectors.toList());
        if(reactions.isEmpty())
            return 0;

        List<User> users = reactions.get(0).getUsers().complete();

        if(users.contains(msg.getAuthor())) return users.size()-1;
        else return users.size();
    }

    private static boolean existsOnStarboard(Long id)
    {
        if(!(Bot.getInstance().dataEnabled))
            return false;

        return !(sdm.getStarboardMessage(id)==null);
    }

    private static void updateCount(Message msg, long starboardMsg, int amount)
    {
        if(!(Bot.getInstance().dataEnabled))
            return;

        String sbEmote = Bot.getInstance().endless.getGuildSettings(msg.getGuild()).getStarboardEmote();
        TextChannel tc = GuildUtils.getStarboardChannel(msg.getGuild());
        Message s = tc.getMessageById(starboardMsg).complete();
        String emote;
        if(!(s.getEmotes().isEmpty()))
        {
            emote = sbEmote;
            s.editMessage(s.getContentRaw().replaceAll(Message.MentionType.EMOTE.getPattern().pattern(), getEmote(amount, s, emote))
                    .replaceAll("\\*\\*(\\d+)\\*\\*", "**"+amount+"**")).complete();
        }
        else
        {
            List<String> emojis = EmojiParser.extractEmojis(sbEmote);
            if(!(emojis.isEmpty()))
            {
                emote = emojis.get(0);
                s.editMessage(s.getContentRaw().replaceAll("(\\\\u\\w+|\\\\u\\w+\\\\u\\w+)", getEmote(amount, s, emote))
                        .replaceAll("\\*\\*(\\d+)\\*\\*", "**"+amount+"**")).complete();
            }
        }
    }

    private static void addMessage(Message starredMsg, TextChannel starboard)
    {
        if(existsOnStarboard(starredMsg.getIdLong()))
            return;

        EmbedBuilder eb = new EmbedBuilder();
        MessageBuilder msgB = new MessageBuilder();
        StringBuilder sb = new StringBuilder();
        String emote = Bot.getInstance().endless.getGuildSettings(starboard.getGuild()).getStarboardEmote();

        List<Message.Attachment> attachments = starredMsg.getAttachments().stream().filter(a -> !(a.isImage())).collect(Collectors.toList());
        List<Message.Attachment> images = starredMsg.getAttachments().stream().filter(Message.Attachment::isImage).collect(Collectors.toList());

        sb.append(starredMsg.getContentRaw());
        eb.setAuthor(starredMsg.getAuthor().getName(), starredMsg.getJumpUrl(), starredMsg.getAuthor().getEffectiveAvatarUrl());
        if(!(attachments.isEmpty())) for(Message.Attachment att : attachments)
            sb.append("\n").append(att.getUrl());
        if(!(images.isEmpty())) if(images.size()>1) for(Message.Attachment img : images)
            sb.append("\n").append(img.getUrl());
        else eb.setImage(images.get(0).getUrl());
        eb.setDescription(sb.toString());
        eb.setColor(Color.ORANGE);

        msgB.setContent(FormatUtil.sanitize(getEmote(getStarCount(starredMsg), starredMsg, emote)+" **"+getStarCount(starredMsg)+"** "+
                starredMsg.getTextChannel().getAsMention()+" ID: "+starredMsg.getId()));
        msgB.setEmbed(eb.build());

        sdm.addMessage(starredMsg, getStarCount(starredMsg));
        starboard.sendMessage(msgB.build()).queue(s -> sdm.setStarboardMessageId(starredMsg, s.getIdLong()));
    }

    private static String getEmote(int count, Message msg, String emote)
    {
        try
        {
            return msg.getGuild().getEmoteById(emote).getAsMention();
        }
        catch(NumberFormatException e)
        {
            if(!(emote.equals("\u2B50")))
                return emote;
            else
            {
                if(count<5)
                    return ":star:";
                else if(count>5)
                    return ":star2:";
                else if(count>=15)
                    return ":dizzy:";
                else
                    return ":star:";
            }
        }
    }

    private static void delete(TextChannel starboard, StarboardMessage starboardMsg)
    {
        if(starboard==null)
            return;

        Message s = starboard.getMessageById(starboardMsg.getStarboardMessageId()).complete();
        s.delete().queue();
        sdm.deleteMessage(starboardMsg.getMessageIdLong(), starboardMsg.getStarboardMessageIdLong());
    }

    private static void check(Guild guild, long msg)
    {
        if(!(Bot.getInstance().dataEnabled))
            return;

        TextChannel starboard = GuildUtils.getStarboardChannel(guild);
        StarboardMessage starboardMsg = sdm.getStarboardMessage(msg);

        if(existsOnStarboard(msg))
            delete(starboard, starboardMsg);
    }

    private static Message getMessage(long id, TextChannel tc)
    {
        try
        {
            return tc.getMessageById(id).complete();
        }
        catch(ErrorResponseException e)
        {
            delete(GuildUtils.getStarboardChannel(tc.getGuild()), sdm.getStarboardMessage(id));
            return null;
        }
    }

    private static String getUnicode(String emoji)
    {
        StringBuilder sb = new StringBuilder();
        emoji.codePoints().forEachOrdered(code -> {
            char[] chars = Character.toChars(code);
            if(chars.length>1)
            {
                String hex0 = Integer.toHexString(chars[0]).toUpperCase();
                String hex1 = Integer.toHexString(chars[1]).toUpperCase();
                while(hex0.length()<4)
                    hex0 = "0"+hex0;
                while(hex1.length()<4)
                    hex1 = "0"+hex1;
                sb.append("\\u").append(hex0).append("\\u").append(hex1);
            }
            else
            {
                String hex = Integer.toHexString(code).toUpperCase();
                while(hex.length()<4)
                    hex = "0"+hex;
                sb.append("\\u").append(hex);
            }
        });
        return sb.toString();
    }
}
