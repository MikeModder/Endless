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

import com.jagrosh.jdautilities.command.Command;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.core.entities.Reminder;
import me.artuto.endless.utils.ArgsUtils;
import me.artuto.endless.utils.FormatUtil;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Artuto
 */

public class ReminderCmd extends EndlessCommand
{
    private final Bot bot;

    public ReminderCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "reminder";
        this.aliases = new String[]{"remindme", "remind"};
        this.children = new Command[]{new CreateCmd(), new DeleteCmd()};
        this.help = "Shows the list of reminders.";
        this.category = Categories.UTILS;
        this.needsArguments = false;
        this.guildOnly = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(!(bot.dataEnabled))
        {
            event.replyError("core.data.disabled");
            return;
        }

        User author = event.getAuthor();
        List<Reminder> reminders = bot.rdm.getRemindersByUser(author.getIdLong());
        if(reminders.isEmpty())
        {
            event.replyWarning("command.reminder.empty", event.getClient().getPrefix());
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(event.localize("command.reminder.list", reminders.size())).append(":");
        for(Reminder r : reminders)
        {
            TextChannel tc = event.getJDA().asBot().getShardManager().getTextChannelById(r.getChannelId());
            sb.append("\n`").append(reminders.indexOf(r)).append(".` ").append(tc==null?event.localize("misc.dm"):tc.getAsMention());
            sb.append(" - \"").append(r.getMessage().length()>20?r.getMessage().substring(0, 20)+"...":r.getMessage()).append("\" ")
                    .append(event.localize("misc.in")).append(" ").append(FormatUtil.formatTimeFromSeconds(OffsetDateTime.now()
                    .until(r.getExpiryTime(), ChronoUnit.SECONDS)));
        }
        event.replySuccess(false, sb.toString());
    }

    private class CreateCmd extends EndlessCommand
    {
        CreateCmd()
        {
            this.name = "create";
            this.aliases = new String[]{"add"};
            this.help = "Creates a reminder.";
            this.arguments = "<time> <message>";
            this.guildOnly = false;
            this.category = Categories.UTILS;
            this.parent = ReminderCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            User author = event.getAuthor();
            List<Reminder> reminders = bot.rdm.getRemindersByUser(author.getIdLong());
            if(reminders.size()>100)
            {
                event.replyError("command.reminder.create.limit");
                return;
            }

            String[] args = ArgsUtils.split(2, event.getArgs());
            long time = ArgsUtils.parseTime(args[0]);
            String message = args[1];
            if(time==0)
            {
                event.replyError("command.reminder.create.invalidTime");
                return;
            }
            if(time<60)
            {
                event.replyError("command.reminder.create.tooShort"); // like apfel's dick
                return;
            }
            if(message.isEmpty())
            {
                event.replyError("command.reminder.create.noMessage");
                return;
            }

            OffsetDateTime expiry = OffsetDateTime.now().plusSeconds(time);
            String formattedTime = FormatUtil.formatTimeFromSeconds(time);
            bot.rdm.createReminder(event.getChannel().getIdLong(), expiry.toInstant().toEpochMilli(), event.getAuthor().getIdLong(), message);
            event.replySuccess("command.reminder.create.created", formattedTime);
        }
    }

    private class DeleteCmd extends EndlessCommand
    {
        DeleteCmd()
        {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.arguments = "<reminder id>";
            this.help = "Deletes a reminder.";
            this.guildOnly = false;
            this.parent = ReminderCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            User author = event.getAuthor();
            List<Reminder> reminders = bot.rdm.getRemindersByUser(author.getIdLong());
            if(reminders.isEmpty())
            {
                event.replyWarning("command.reminder.delete.empty");
                return;
            }

            long id;
            try {id = Long.parseLong(event.getArgs());}
            catch(NumberFormatException e)
            {
                event.replyError("command.reminder.delete.invalidId");
                return;
            }

            if(reminders.size()>id+1 || reminders.size()<id+1)
            {
                event.replyError("command.reminder.delete.notFound");
                return;
            }

            Reminder reminder = reminders.get((int)id);
            bot.rdm.deleteReminder(reminder.getId(), author.getIdLong());
            event.replySuccess("command.reminder.delete.deleted", id);
        }
    }
}
