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

import me.artuto.endless.core.entities.Poll;
import me.artuto.endless.utils.ChecksUtil;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Artuto
 */

public class PollHandler
{
    public static void sendResults(Poll poll, ShardManager shardManager)
    {
        TextChannel tc = shardManager.getTextChannelById(poll.getTextChannelId());
        if(tc==null)
            return;
        if(!(ChecksUtil.hasPermission(tc.getGuild().getSelfMember(), tc, Permission.MESSAGE_READ,
                Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS)))
            return;
        if(!(tc.canTalk()))
            return;
        tc.getMessageById(poll.getMessageId()).queue(msg -> {
            EmbedBuilder builder = new EmbedBuilder();
            MessageEmbed embed = msg.getEmbeds().get(0);
            StringBuilder sb = new StringBuilder();
            builder.setColor(embed.getColor());

            builder.setTitle(embed.getTitle());
            if(msg.getReactions().isEmpty())
                sb.append("I couldn't determine a winner option for this poll.");
            else
            {
                List<Integer> counts = new ArrayList<>();
                msg.getReactions().forEach(r -> counts.add(r.isSelf()?(r.getCount()-1):r.getCount()));
                int max = Collections.max(counts);
                int totalCount = 0;
                for(int re : counts)
                    totalCount += re;
                List<MessageReaction> winners = msg.getReactions().stream()
                        .filter(r -> (r.isSelf()?(r.getCount()-1)==max:r.getCount()==max)).collect(Collectors.toList());
                sb.append("Time is over! ");
                if(winners.size()>1)
                {
                    sb.append("\nThere was a tie between **").append(winners.size()).append("** options!\n_ _\n");
                    winners.forEach(re -> {
                        MessageReaction.ReactionEmote reactionEmote = re.getReactionEmote();
                        sb.append(reactionEmote.isEmote()?reactionEmote.getEmote().getAsMention():reactionEmote.getName()).append("  ");
                    });
                }
                else
                {
                    sb.append("A total of **").append(totalCount).append("** people voted!\n");
                    MessageReaction re = winners.get(0);
                    MessageReaction.ReactionEmote reactionEmote = re.getReactionEmote();
                    sb.append("The winner, with **").append(re.isSelf()?(re.getCount()-1):re.getCount()).append("** votes, is...\n_ _\n");
                    sb.append(reactionEmote.isEmote()?reactionEmote.getEmote().getAsMention():reactionEmote.getName());
                }
            }
            tc.sendMessage(builder.setDescription(sb).build()).queue();
        }, e -> {});
    }
}
