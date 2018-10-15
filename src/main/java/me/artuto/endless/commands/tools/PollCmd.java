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

import com.vdurmont.emoji.EmojiParser;
import me.artuto.endless.Bot;
import me.artuto.endless.Sender;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.utils.ArgsUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Artuto
 */

public class PollCmd extends EndlessCommand
{
    private final Bot bot;
    private final String[] FLAGS = {"c", "d", "e", "t"};

    public PollCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "poll";
        this.help = "Run a poll with options. Execute `e!"+
                "poll flags` to get a list of all the supported flags.";
        this.category = Categories.TOOLS;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION};
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(!(bot.dataEnabled))
        {
            event.replyError("core.data.disabled");
            return;
        }

        ParsedArgs pa = new ParsedArgs(event);
        EmbedBuilder builder = new EmbedBuilder();

        if(event.getArgs().equalsIgnoreCase("flags"))
        {
            builder.setTitle("Available flags:");
            builder.setDescription("`-t` - Sets how long the poll will be.\n" +
                    "`-e` - Emotes to apply to the poll.\n" +
                    "`-d` - Sets the description of the poll.\n" +
                    "`-c` - The color of the poll.\n\n" +
                    ":warning: REMEMBER TO PUT A SPACE AFTER AN OPTION!!! :warning:\n" +
                    "For example `-t 24h`");
            builder.setColor(Color.YELLOW);
            Sender.sendHelp(event, builder.build());
            return;
        }

        String title = pa.title;
        String description = pa.description;
        Color color = pa.color;
        OffsetDateTime endTime = pa.time;
        if(title==null)
        {
            event.replyError("command.poll.noQuestion");
            return;
        }

        builder.setTitle(title);
        builder.setColor(color==null?event.getMember().getColor():color);
        builder.setDescription(description==null?"\n":description);
        builder.setFooter(event.localize("command.poll.expireIn"), event.getAuthor().getEffectiveAvatarUrl());
        builder.setTimestamp(endTime);
        event.getTextChannel().sendMessage(builder.build()).queue(msg -> {
            bot.pldm.createPoll(endTime.toInstant().toEpochMilli(), event.getGuild().getIdLong(), msg.getIdLong(), event.getTextChannel().getIdLong());
            pa.emotes.forEach(str -> {
                long id = 0L;
                try {id = Long.parseLong(str);}
                catch(NumberFormatException ignored) {}
                Emote e = event.getJDA().asBot().getShardManager().getEmoteById(id);
                if(e==null)
                    msg.addReaction(str).queue();
                else
                    msg.addReaction(e).queue();
            });
        });
    }

    private class ParsedArgs
    {
        private String title;
        private String description;
        private String rawColor;
        private String rawTime;
        private String rawEmotes;

        private Color color = null;
        private OffsetDateTime time;
        private List<String> emotes = new ArrayList<>();

        ParsedArgs(EndlessCommandEvent event)
        {
            Map<String, String> map = ArgsUtils.parseArgs(FLAGS, event.getArgs().split("\\s+"));
            this.title = map.get("title");
            this.description = map.get("d");
            this.rawColor = map.getOrDefault("c", "#"+event.hashCode()); // i need a random color so...
            this.rawTime = map.get("t");
            this.rawEmotes = map.getOrDefault("e", "");

            // parse color
            try {this.color = Color.decode(rawColor.startsWith("#")?rawColor:"#"+rawColor);}
            catch(NumberFormatException ignored) {}

            // parse emotes/emojis
            emotes.addAll(EmojiParser.extractEmojis(rawEmotes));
            Pattern EMOTE = Message.MentionType.EMOTE.getPattern();
            for(String e : rawEmotes.split("\\s+"))
            {
                Matcher m = EMOTE.matcher(e);
                while(m.find())
                {
                    Emote em = event.getJDA().asBot().getShardManager().getEmoteById(m.group(2));
                    if(!(em==null))
                        emotes.add(em.getId());
                }
            }
            if(emotes.isEmpty())
            {
                emotes.add("\uD83D\uDC4D");
                emotes.add("\uD83D\uDC4E");
            }

            // parse time
            long t = ArgsUtils.parseTime(rawTime==null?"60s":rawTime);
            this.time = OffsetDateTime.now().plusSeconds(t==0?60:t);
        }
    }
}
