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

package me.artuto.endless.commands;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.Const;
import me.artuto.endless.Locale;
import me.artuto.endless.core.entities.GuildSettings;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.MissingResourceException;

/**
 * @author Artuto
 */

public class EndlessCommandEvent extends CommandEvent
{
    private CommandEvent event;

    private EndlessCommandEvent(MessageReceivedEvent event, String args, CommandClient client)
    {
        super(event, args, client);
    }

    public EndlessCommandEvent(CommandEvent event)
    {
        this(event.getEvent(), event.getArgs(), event.getClient());
        this.event = event;
    }

    public String localize(String s, Object... args)
    {
        if(event.isFromType(ChannelType.TEXT))
        {
            GuildSettings gs = getClient().getSettingsFor(getGuild());
            s = MessageFormat.format(gs.getLocale().getBundle().getString(s), args);
        }
        else
            s = MessageFormat.format(Locale.EN_US.getBundle().getString(s), args);

        return s;
    }

    // Reply methods. To localize strings.
    @Override
    public void reply(String s)
    {
        reply(true, s, Collections.EMPTY_LIST);
    }

    public void reply(String s, Object... args)
    {
        if(event.isFromType(ChannelType.TEXT))
        {
            GuildSettings gs = getClient().getSettingsFor(getGuild());
            try {s = MessageFormat.format(gs.getLocale().getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.reply(s);
                return;
            }
        }
        else
        {
            try {s = MessageFormat.format(Locale.EN_US.getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.reply(s);
                return;
            }
        }

        event.reply(s);
    }

    public void reply(boolean t, String s, Object... args)
    {
        if(t)
        {
            reply(s, args);
            return;
        }

        event.reply(s);
    }

    public void replyInfo(String s)
    {
        replyInfo(true, s, Collections.EMPTY_LIST);
    }

    public void replyInfo(String s, Object... args)
    {
        if(event.isFromType(ChannelType.TEXT))
        {
            GuildSettings gs = getClient().getSettingsFor(getGuild());
            try {s = MessageFormat.format(gs.getLocale().getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.reply(Const.INFO+" "+s);
                return;
            }
        }
        else
        {
            try {s = MessageFormat.format(Locale.EN_US.getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.reply(Const.INFO+" "+s);
                return;
            }
        }

        event.reply(Const.INFO+" "+s);
    }

    public void replyInfo(boolean t, String s, Object... args)
    {
        if(t)
        {
            replyInfo(s, args);
            return;
        }

        event.reply(Const.INFO+" "+s);
    }

    @Override
    public void replyError(String s)
    {
        replyError(true, s, Collections.EMPTY_LIST);
    }

    public void replyError(String s, Object... args)
    {
        if(event.isFromType(ChannelType.TEXT))
        {
            GuildSettings gs = getClient().getSettingsFor(getGuild());
            try {s = MessageFormat.format(gs.getLocale().getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.replyError(s);
                return;
            }
        }
        else
        {
            try {s = MessageFormat.format(Locale.EN_US.getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.replyError(s);
                return;
            }
        }

        event.replyError(s);
    }

    public void replyError(boolean t, String s, Object... args)
    {
        if(t)
        {
            replyError(s, args);
            return;
        }

        event.replyError(s);
    }

    @Override
    public void replySuccess(String s)
    {
        replySuccess(true, s, Collections.EMPTY_LIST);
    }

    public void replySuccess(String s, Object... args)
    {
        if(event.isFromType(ChannelType.TEXT))
        {
            GuildSettings gs = getClient().getSettingsFor(getGuild());
            try {s = MessageFormat.format(gs.getLocale().getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.replySuccess(s);
                return;
            }
        }
        else
        {
            try {s = MessageFormat.format(Locale.EN_US.getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.replySuccess(s);
                return;
            }
        }

        event.replySuccess(s);
    }

    public void replySuccess(boolean t, String s, Object... args)
    {
        if(t)
        {
            replySuccess(s, args);
            return;
        }

        event.replySuccess(s);
    }

    @Override
    public void replyWarning(String s)
    {
        replyWarning(true, s, Collections.EMPTY_LIST);
    }

    public void replyWarning(String s, Object... args)
    {
        if(event.isFromType(ChannelType.TEXT))
        {
            GuildSettings gs = getClient().getSettingsFor(getGuild());
            try {s = MessageFormat.format(gs.getLocale().getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.replyWarning(s);
                return;
            }
        }
        else
        {
            try {s = MessageFormat.format(Locale.EN_US.getBundle().getString(s), args);}
            catch(MissingResourceException ignored)
            {
                event.replyWarning(s);
                return;
            }
        }
        event.replyWarning(s);
    }

    public void replyWarning(boolean t, String s, Object... args)
    {
        if(t)
        {
            replyWarning(s, args);
            return;
        }

        event.replyWarning(s);
    }
}
