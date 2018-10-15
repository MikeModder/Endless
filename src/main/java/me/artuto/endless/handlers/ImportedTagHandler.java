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

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.core.entities.Tag;
import me.artuto.endless.utils.MiscUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Artuto
 */

public class ImportedTagHandler
{
    public static void runTag(Bot bot, MessageReceivedEvent event)
    {
        if(!(bot.dataEnabled))
            return;

        CommandClient client = bot.client;
        EndlessCommand tagCommand = (EndlessCommand)client.getCommands().stream()
                .filter(c -> c.getName().equals("tag")).findFirst().orElse(null);
        if(tagCommand==null)
            return;
        if(event.getGuild()==null)
            return;

        GuildSettings settings = client.getSettingsFor(event.getGuild());
        String contentRaw = event.getMessage().getContentRaw();
        String[] parts = null;

        if(!(event.getAuthor().isBot()))
        {
            if(client.getAltPrefix().equals("@mention") && (contentRaw.startsWith("<@" + event.getJDA().getSelfUser().getId() + ">")
                    || contentRaw.startsWith("<@!" + event.getJDA().getSelfUser().getId() + ">")))
                parts = splitOnPrefixLength(contentRaw, contentRaw.indexOf(">")+1);

            if(parts==null && contentRaw.toLowerCase().startsWith(client.getPrefix().toLowerCase()))
                parts = splitOnPrefixLength(contentRaw, client.getPrefix().length());

            if(parts==null && !(client.getAltPrefix()==null) && contentRaw.toLowerCase().startsWith(client.getAltPrefix().toLowerCase()))
                parts = splitOnPrefixLength(contentRaw, client.getAltPrefix().length());

            if(parts==null & !(settings==null))
            {
                Collection<String> prefixes = settings.getPrefixes();
                if(!(prefixes==null))
                {
                    for(String prefix : prefixes)
                    {
                        if(contentRaw.toLowerCase().startsWith(prefix.toLowerCase()))
                            parts = splitOnPrefixLength(contentRaw, prefix.length());
                    }
                }
            }

            if(!(parts==null))
            {
                String name = parts[0];
                EndlessCommand command = (EndlessCommand)client.getCommands().stream().filter(c -> c.isCommandFor(name)).findFirst().orElse(null);
                if(!(command==null))
                    return;

                String commandArgs = String.join(" ", parts[0], parts[1]==null?"":parts[1]);
                String[] tagParts = splitTagNameAndArgs(commandArgs);
                Tag tag = bot.endless.getGuildSettings(event.getGuild()).getImportedTags()
                        .stream().filter(t -> t.getName().equals(tagParts[0])).findFirst().orElse(null);
                if(tag==null)
                    return;

                CommandEvent cevent = new CommandEvent(event, commandArgs, client);
                if(!(client.getListener()==null))
                    client.getListener().onCommand(cevent, tagCommand);
                tagCommand.run(cevent);
            }
        }
    }

    private static String[] splitOnPrefixLength(String contentRaw, int length)
    {
        return Arrays.copyOf(contentRaw.substring(length).trim().split("\\s+", 2), 2);
    }

    private static String[] splitTagNameAndArgs(String content)
    {
        try
        {
            String[] parts = content.split("\\s+", 2);
            String name = parts[0];
            String args = parts[1];

            return new String[]{name, args};
        }
        catch(IndexOutOfBoundsException e)
        {
            return new String[]{content, ""};
        }
    }
}
