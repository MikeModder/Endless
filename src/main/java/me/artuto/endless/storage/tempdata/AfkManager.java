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

package me.artuto.endless.storage.tempdata;

import me.artuto.endless.Bot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;

public class AfkManager
{
    private static HashMap<Long, String> afk = new HashMap<>();

    public static void setAfk(long id, String message)
    {
        afk.put(id, message);
    }

    public static String getMessage(long id)
    {
        return afk.get(id);
    }

    private static void unsetAfk(long id)
    {
        afk.remove(id);
    }

    private static boolean isAfk(long id)
    {
        return afk.containsKey(id);
    }

    public static void checkAfk(GuildMessageReceivedEvent event)
    {
        User author = event.getAuthor();
        if(author.isBot())
            return;
        if(isAfk(author.getIdLong()))
        {
            author.openPrivateChannel().queue(pc -> pc.sendMessage(Bot.getInstance().config.getDoneEmote()+" I've removed your AFK status.")
                    .queue(null, (e) -> {}));
            unsetAfk(author.getIdLong());
        }
    }

    public static void checkPings(GuildMessageReceivedEvent event)
    {
        Message message = event.getMessage();
        User author = event.getAuthor();

        message.getMentionedUsers().forEach(user -> {
            if(!(isAfk(user.getIdLong())))
                return;
            if(author.isBot())
                return;

            EmbedBuilder builder = new EmbedBuilder();

            builder.setAuthor(author.getName()+"#"+author.getDiscriminator(), null, author.getEffectiveAvatarUrl());
            builder.setDescription(message.getContentDisplay());
            builder.setFooter("#"+message.getTextChannel().getName()+", "+event.getGuild().getName(), event.getGuild().getIconUrl());
            builder.setTimestamp(message.getCreationTime());
            builder.setColor(event.getMember().getColor());

            user.openPrivateChannel().queue(pc -> pc.sendMessage(builder.build()).queue((s) -> {
                builder.clear();
                continueSending(builder, event, user);
            }, (e) -> {
                builder.clear();
                continueSending(builder, event, user);
            }));
        });
    }

    private static void continueSending(EmbedBuilder builder, GuildMessageReceivedEvent event, User user)
    {
        if(!(event.getChannel().canTalk()))
            return;

        if(getMessage(user.getIdLong())==null)
            event.getChannel().sendMessage(":bed: "+Bot.getInstance().localize(event.getGuild(), "core.afk", user.getName())).queue();
        else
        {
            builder.setDescription(AfkManager.getMessage(user.getIdLong()));
            builder.setColor(event.getGuild().getMember(user).getColor());

            event.getChannel().sendMessage(new MessageBuilder().setContent(":bed: "+Bot.getInstance().localize(event.getGuild(), "core.afk", user.getName()))
                    .setEmbed(builder.build()).build()).queue();
        }
    }
}
