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

package me.artuto.endless.storage.data.managers;

import ch.qos.logback.classic.Logger;
import me.artuto.endless.Bot;
import me.artuto.endless.Endless;
import me.artuto.endless.core.entities.Reminder;
import me.artuto.endless.utils.FormatUtil;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Artuto
 */

public class RemindersDataManager
{
    private final Bot bot;
    private final Connection connection;
    private final Logger LOG = Endless.getLog(RemindersDataManager.class);

    public RemindersDataManager(Bot bot)
    {
        this.bot = bot;
        this.connection = bot.db.getConnection();
    }

    public void createReminder(long channelId, long expiryTime, long userId, String msg)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM REMINDERS",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.closeOnCompletion();

            try(ResultSet results = statement.executeQuery())
            {
                results.moveToInsertRow();
                results.updateLong("user_id", userId);
                results.updateLong("channel_id", channelId);
                results.updateLong("expiry_time", expiryTime);
                results.updateString("msg", msg);
                results.insertRow();
            }
        }
        catch(SQLException e)
        {
            LOG.error("Error while creating a reminder. User ID: {}", userId, e);
        }
    }

    public void deleteReminder(long id, long userId)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM REMINDERS WHERE user_id = ? AND id = ?",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.setLong(1, userId);
            statement.setLong(2, id);
            statement.closeOnCompletion();

            try(ResultSet results = statement.executeQuery())
            {
                if(results.next())
                    results.deleteRow();
            }
        }
        catch(SQLException e)
        {
            LOG.error("Error while deleting a reminder. User ID: [}", userId, e);
        }
    }

    public List<Reminder> getRemindersByUser(long userId)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM REMINDERS WHERE user_id = ?",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.setLong(1, userId);
            statement.closeOnCompletion();
            List<Reminder> list;

            try(ResultSet results = statement.executeQuery())
            {
                list = new LinkedList<>();
                while(results.next())
                    list.add(bot.endlessBuilder.entityBuilder.createReminder(results));
                return list;
            }
        }
        catch(SQLException e)
        {
            LOG.error("Error while adding a list of reminders of a specified user. User ID: {}", userId, e);
            return Collections.emptyList();
        }
    }

    public void updateReminders(ShardManager shardManager)
    {
        OffsetDateTime now = OffsetDateTime.now();
        List<Reminder> expired = new LinkedList<>();
        List<Reminder> reminders = getReminders();

        // Check for expired
        for(Reminder reminder : reminders)
        {
            if(now.toInstant().toEpochMilli()>reminder.getExpiryTime().toInstant().toEpochMilli())
                expired.add(reminder);
        }

        // Tell the user and delete them
        for(Reminder reminder : expired)
        {
            deleteReminder(reminder.getId(), reminder.getUserId());
            User user = shardManager.getUserById(reminder.getUserId());
            if(user==null)
                return;
            MessageChannel channel = shardManager.getTextChannelById(reminder.getChannelId());
            if(channel==null)
                channel = user.openPrivateChannel().complete();
            String toSend;
            long until = ChronoUnit.SECONDS.between(now,  reminder.getExpiryTime());
            String formattedTime = FormatUtil.formatTimeFromSeconds(until);
            if(channel instanceof PrivateChannel)
                toSend = ":alarm_clock: "+reminder.getMessage()+" ~set "+formattedTime+" ago";
            else
                toSend = user.getAsMention()+" :alarm_clock: "+reminder.getMessage()+" ~set "+formattedTime+" ago";

            channel.sendMessage(FormatUtil.sanitize(toSend)).queue(null, e -> {});
        }
    }

    private List<Reminder> getReminders()
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM REMINDERS",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.closeOnCompletion();
            List<Reminder> list;

            try(ResultSet results = statement.executeQuery())
            {
                list = new LinkedList<>();
                while(results.next())
                    list.add(bot.endlessBuilder.entityBuilder.createReminder(results));
                return list;
            }
        }
        catch(SQLException e)
        {
            LOG.error("Error while adding a list of reminders", e);
            return Collections.emptyList();
        }
    }
}
