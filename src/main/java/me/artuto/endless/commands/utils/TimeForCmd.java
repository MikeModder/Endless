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

package me.artuto.endless.commands.utils;

import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.core.entities.Profile;
import me.artuto.endless.utils.ArgsUtils;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeForCmd extends EndlessCommand
{
    private final Bot bot;

    public TimeForCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "timefor";
        this.aliases = new String[]{"tf"};
        this.children = new EndlessCommand[]{new ChangeCmd(), new ListCmd()};
        this.help = "Shows the timezone for the specified user";
        this.arguments = "<user>";
        this.category = Categories.UTILS;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(!(bot.dataEnabled))
        {
            event.replyError("core.data.disabled");
            return;
        }

        Profile p;
        ZonedDateTime t;
        ZoneId zone;
        String time;
        String time24;
        String name;
        User user;

        if(event.getArgs().isEmpty())
        {
            user = event.getAuthor();
            p = bot.prdm.getProfile(user);
            name = user.getName()+"#"+user.getDiscriminator();

            if(p.getTimezone()==null)
                event.replyWarning("command.timefor.notConfigured.executor");
            else
            {
                try {zone = ZoneId.of(p.getTimezone());}
                catch(DateTimeException ignored)
                {
                    event.replyError("command.timefor.invalid", p.getTimezone());
                    return;
                }

                t = event.getMessage().getCreationTime().atZoneSameInstant(zone);
                time = t.format(DateTimeFormatter.ofPattern("h:mma"));
                time24 = t.format(DateTimeFormatter.ofPattern("HH:mm"));

                event.reply(false, ":clock1: "+event.localize("command.timefor", name, time, time24));
            }
        }
        else
        {
            user = ArgsUtils.findUser(false, event, event.getArgs());
            if(user==null)
                return;

            p = bot.prdm.getProfile(user);
            name = user.getName()+"#"+user.getDiscriminator();

            if(!(bot.prdm.hasProfile(user)))
                event.replyError("command.timefor.notConfigured.other", name);
            else
            {
                try {zone = ZoneId.of(p.getTimezone());}
                catch(DateTimeException ignored)
                {
                    event.replyError("command.timefor.invalid", p.getTimezone());
                    return;
                }

                t = event.getMessage().getCreationTime().atZoneSameInstant(zone);
                time = t.format(DateTimeFormatter.ofPattern("h:mma"));
                time24 = t.format(DateTimeFormatter.ofPattern("HH.mm"));

                event.reply(false, ":clock1: "+event.localize("command.timefor", name, time, time24));
            }
        }
    }

    private class ChangeCmd extends EndlessCommand
    {
        ChangeCmd()
        {
            this.name = "change";
            this.aliases = new String[]{"set"};
            this.help = "Changes your timezone";
            this.arguments = "<timezone>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.parent = TimeForCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            String args = event.getArgs();
            try {ZoneId.of(args);}
            catch(DateTimeException ignored)
            {
                event.replyError("command.timefor.change.invalid", event.getClient().getPrefix());
                return;
            }

            bot.prdm.setTimezone(event.getAuthor(), args);
            event.replySuccess("command.timefor.change.changed");
        }
    }

    private class ListCmd extends EndlessCommand
    {
        ListCmd()
        {
            this.name = "list";
            this.aliases = new String[]{"timezones"};
            this.help = "Shows the list with valid timezones";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.parent = TimeForCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            String url = "https://github.com/EndlessBot/Endless/blob/master/src/main/resources/timezones.txt";
            try {event.getChannel().sendFile(new URL(url).openStream(), "Timezones.txt", null).queue();}
            catch(IOException ignored) {event.replyError("command.timefor.list.error", url);}
        }
    }
}
