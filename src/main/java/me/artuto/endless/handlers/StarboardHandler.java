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

import me.artuto.endless.Bot;
import me.artuto.endless.storage.data.managers.StarboardDataManager;
import me.artuto.endless.core.entities.StarboardMessage;
import me.artuto.endless.utils.ChecksUtil;
import me.artuto.endless.utils.FinderUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger("Starboard");
    private static final Pattern MESSAGE = Pattern.compile(":(\\D+): \\*\\*(\\d+)\\*\\* <#(\\d{17,20})> ID: (\\d{17,20})");
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
                FinderUtil.getDefaultChannel(guild).sendMessage("I can't talk on the starboard!").queue(null, e -> guild.getOwner().getUser().openPrivateChannel().queue(s -> s.sendMessage("I can't talk on the starboard!").queue(null, null)));
                return;
            }

            if(event.getChannel().getIdLong()==starboard.getIdLong())
            {
                String content = starredMsg.getContentRaw();
                Matcher m = MESSAGE.matcher(content);
                if(m.matches() && starredMsg.getAuthor().getIdLong()==event.getJDA().getSelfUser().getIdLong())
                {
                    TextChannel originalTc = guild.getTextChannelById(m.group(3));
                    if(originalTc==null || !(ChecksUtil.hasPermission(guild.getSelfMember(), originalTc, Permission.MESSAGE_HISTORY)))
                        return;

                    Message originalMsg = originalTc.getMessageById(m.group(4)).complete();
                    MessageReaction reaction = originalMsg.getReactions().stream().filter(r -> {
                        MessageReaction.ReactionEmote reE = r.getReactionEmote();
                        return (reE.isEmote()?reE.getId().equals(emote):reE.getName().equals(emote));
                    }).findFirst().orElse(null);
                    if(reaction==null || reaction.getUsers().complete().contains(event.getUser()))
                        return;
                    if(isSameAuthor(originalMsg.getAuthor(), event.getUser()) && (re.isEmote()?re.getId().equals(emote):re.getName().equals(emote)))
                        return;
                    int count = getStarCount(originalMsg)+getStarCount(starredMsg);
                    System.out.println(count);

                    if(!(sdm.updateCount(originalMsg.getIdLong(), count)))
                        LOG.warn("Error when updating star count. Message ID: "+originalMsg.getId()+" TC ID: "+originalMsg.getTextChannel().getId());
                    else
                        updateCount(starredMsg, sdm.getStarboardMessage(originalMsg.getIdLong()).getStarboardMessageIdLong(), count);
                }
                else
                    addMessage(starredMsg, starboard);
            }
            else
            {
                if(existsOnStarboard(starredMsg.getIdLong()))
                {
                    if(!(sdm.updateCount(starredMsg.getIdLong(), getStarCount(starredMsg))))
                        LOG.warn("Error when updating star count. Message ID: "+starredMsg.getId()+" TC ID: "+starredMsg.getTextChannel().getId());
                    else
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
                String content = starredMsg.getContentRaw();
                Matcher m = MESSAGE.matcher(content);
                if(m.matches() && starredMsg.getAuthor().getIdLong()==event.getJDA().getSelfUser().getIdLong())
                {
                    TextChannel originalTc = guild.getTextChannelById(m.group(3));
                    if(originalTc==null || !(ChecksUtil.hasPermission(guild.getSelfMember(), originalTc, Permission.MESSAGE_HISTORY)))
                        return;

                    Message originalMsg = originalTc.getMessageById(m.group(4)).complete();
                    starboardMsg = sdm.getStarboardMessage(originalMsg.getIdLong());
                    if(isSameAuthor(originalMsg.getAuthor(), event.getUser()) && (re.isEmote()?re.getId().equals(emote):re.getName().equals(emote)))
                        return;
                    int count = getStarCount(originalMsg)+getStarCount(starredMsg);

                    if(!(count>=GuildUtils.getStarboardCount(guild)))
                    {
                        delete(starboard, starboardMsg);
                        return;
                    }

                    if(!(sdm.updateCount(originalMsg.getIdLong(), count)))
                        LOG.warn("Error when updating star count. Message ID: "+originalMsg.getId()+" TC ID: "+originalMsg.getTextChannel().getId());
                    else
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

                if(!(sdm.updateCount(starredMsg.getIdLong(), getStarCount(starredMsg))))
                    LOG.warn("Error when updating star count. Message ID: "+starredMsg.getId()+" TC ID: "+starredMsg.getTextChannel().getId());
                else
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

                String content = starredMsg.getContentRaw();
                Matcher m = MESSAGE.matcher(content);
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

    private static void updateCount(Message msg, Long starboardMsg, Integer amount)
    {
        if(!(Bot.getInstance().dataEnabled))
            return;

        String emote = Bot.getInstance().endless.getGuildSettings(msg.getGuild()).getStarboardEmote();
        TextChannel tc = GuildUtils.getStarboardChannel(msg.getGuild());
        tc.getMessageById(starboardMsg).queue(s -> s.editMessage(s.getContentRaw().replaceAll(":(\\D+):", getEmote(amount, emote))
                .replaceAll("\\*\\*(\\d+)\\*\\*", "**"+amount+"**")).queue(),null);
    }

    private static void addMessage(Message starredMsg, TextChannel starboard)
    {
        EmbedBuilder eb = new EmbedBuilder();
        MessageBuilder msgB = new MessageBuilder();
        StringBuilder sb = new StringBuilder();
        String emote = Bot.getInstance().endless.getGuildSettings(starboard.getGuild()).getStarboardEmote();

        List<Message.Attachment> attachments = starredMsg.getAttachments().stream().filter(a -> !(a.isImage())).collect(Collectors.toList());
        List<Message.Attachment> images = starredMsg.getAttachments().stream().filter(Message.Attachment::isImage).collect(Collectors.toList());

        sb.append(starredMsg.getContentRaw());
        eb.setAuthor(starredMsg.getAuthor().getName(), null, starredMsg.getAuthor().getEffectiveAvatarUrl());
        if(!(attachments.isEmpty())) for(Message.Attachment att : attachments)
            sb.append("\n").append(att.getUrl());
        if(!(images.isEmpty())) if(images.size()>1) for(Message.Attachment img : images)
            sb.append("\n").append(img.getUrl());
        else eb.setImage(images.get(0).getUrl());
        eb.setDescription(sb.toString());
        eb.setColor(Color.ORANGE);

        msgB.setContent(getEmote(getStarCount(starredMsg), emote)+" **"+getStarCount(starredMsg)+"** "+starredMsg.getTextChannel().getAsMention()+" ID: "+starredMsg.getId());
        msgB.setEmbed(eb.build());

        if(!(sdm.addMessage(starredMsg, getStarCount(starredMsg))))
            LOG.warn("Error when adding message to starboard. Message ID: "+starredMsg.getId()+" TC ID: "+starredMsg.getTextChannel().getId());
        starboard.sendMessage(msgB.build()).queue(s -> sdm.setStarboardMessageId(starredMsg, s.getIdLong()));
    }

    private static String getEmote(Integer count, String emote)
    {
        if(!(emote.equals("\u2B50")))
            return emote;

        if(count<5) return ":star:";
        else if(count>5 || count<=10) return ":star2:";
        else if(count>15) return ":dizzy:";
        else return ":star:";
    }

    private static void delete(TextChannel starboard, StarboardMessage starboardMsg)
    {
        if(starboard==null)
            return;

        starboard.getMessageById(starboardMsg.getStarboardMessageId()).queue(s -> {
            s.delete().queue();
            sdm.deleteMessage(starboardMsg.getMessageIdLong(), starboardMsg.getStarboardMessageIdLong());
        }, e -> sdm.deleteMessage(starboardMsg.getMessageIdLong(), starboardMsg.getStarboardMessageIdLong()));
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
}
