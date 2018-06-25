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

package me.artuto.endless.data.managers;

import me.artuto.endless.Const;
import me.artuto.endless.data.Database;
import me.artuto.endless.entities.Blacklist;
import me.artuto.endless.entities.impl.BlacklistImpl;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.*;

public class BlacklistDataManager
{
    private Connection connection;

    public BlacklistDataManager(Database db)
    {
        connection = db.getConnection();
    }

    public Blacklist getBlacklist(long id)
    {
        try
        {
            Statement statement = connection.createStatement();
            statement.closeOnCompletion();

            try(ResultSet results = statement.executeQuery(String.format("SELECT * FROM BLACKLISTED_ENTITIES WHERE id = %s", id)))
            {
                if(results.next())
                {
                    Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    gmt.setTimeInMillis(results.getLong("time"));
                    return new BlacklistImpl(Const.BlacklistType.valueOf(results.getString("type")), results.getLong("id"),
                            OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId()), results.getString("reason"));
                }
                else return null;
            }
        }
        catch(SQLException e)
        {
            Database.LOG.error("Error while checking if the specified ID is blacklisted. ID: "+id, e);
            return null;
        }
    }

    public void addBlacklist(Const.BlacklistType type, long id, long time, String reason)
    {
        try
        {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.closeOnCompletion();

            try(ResultSet results = statement.executeQuery(String.format("SELECT * FROM BLACKLISTED_ENTITIES WHERE id = %s", id)))
            {
                if(results.next())
                {
                    results.updateLong("id", id);
                    results.updateString("reason", reason.equals("[no reason provided]")?null:reason);
                    results.updateLong("time", time);
                    results.updateString("type", type.name());
                    results.updateRow();
                }
                else
                {
                    results.moveToInsertRow();
                    results.updateLong("id", id);
                    results.updateString("reason", reason);
                    results.updateLong("time", time);
                    results.updateString("type", type.name());
                    results.insertRow();
                }
            }
        }
        catch(SQLException e)
        {
            Database.LOG.error("Error while adding the specified ID to the blacklisted entities list. ID: "+id, e);
        }
    }

    public void removeBlacklist(long id)
    {
        try
        {
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            statement.closeOnCompletion();

            try(ResultSet results = statement.executeQuery(String.format("SELECT * FROM BLACKLISTED_ENTITIES WHERE id = %s", id)))
            {
                if(results.next())
                    results.deleteRow();
            }
        }
        catch(SQLException e)
        {
            Database.LOG.error("Error while removing the specified ID from the blacklisted entities list. ID: "+id, e);
        }
    }

    public Map<Blacklist, Guild> getBlacklistedGuilds(JDA jda)
    {
        try
        {
            Statement statement = connection.createStatement();
            statement.closeOnCompletion();
            Map<Blacklist, Guild> map;

            try(ResultSet results = statement.executeQuery(String.format("SELECT * FROM BLACKLISTED_ENTITIES WHERE type = %s", Const.BlacklistType.GUILD.name())))
            {
                map = new HashMap<>();
                while(results.next())
                {
                    long id = results.getLong("id");
                    Guild guild = jda.getGuildCache().getElementById(id);

                    if(!(guild==null))
                        map.put(getBlacklist(id), guild);
                }
            }
            return map;
        }
        catch(SQLException e)
        {
            Database.LOG.error("Error whiile getting the blacklisted guilds map.", e);
            return null;
        }
    }

    public Map<Blacklist, User> getBlacklistedUsers(JDA jda)
    {
        try
        {
            Statement statement = connection.createStatement();
            statement.closeOnCompletion();
            Map<Blacklist, User> map;

            try(ResultSet results = statement.executeQuery(String.format("SELECT * FROM BLACKLISTED_ENTITIES WHERE type = \"%s\"", Const.BlacklistType.USER.name())))
            {
                map = new HashMap<>();
                while(results.next())
                {
                    long id = results.getLong("id");
                    jda.retrieveUserById(id).queue(user -> map.put(getBlacklist(id), user), e -> removeBlacklist(id));
                }
            }
            return map;
        }
        catch(SQLException e)
        {
            Database.LOG.error("Error while getting the blacklisted users map.", e);
            return null;
        }
    }

    public Map<Blacklist, Long> getBlacklistedGuildsRaw()
    {
        try
        {
            Statement statement = connection.createStatement();
            statement.closeOnCompletion();
            Map<Blacklist, Long> map;

            try(ResultSet results = statement.executeQuery(String.format("SELECT * FROM BLACKLISTED_ENTITIES WHERE type = %s", Const.BlacklistType.GUILD.name())))
            {
                map = new HashMap<>();
                while(results.next())
                {
                    long id = results.getLong("id");
                        map.put(getBlacklist(id), id);
                }
            }
            return map;
        }
        catch(SQLException e)
        {
            Database.LOG.error("Error whiile getting the raw blacklisted guilds map.", e);
            return null;
        }
    }

    public Map<Blacklist, Long> getBlacklistedUsersRaw()
    {
        try
        {
            Statement statement = connection.createStatement();
            statement.closeOnCompletion();
            Map<Blacklist, Long> map;

            try(ResultSet results = statement.executeQuery(String.format("SELECT * FROM BLACKLISTED_ENTITIES WHERE type = \"%s\"", Const.BlacklistType.USER.name())))
            {
                map = new HashMap<>();
                while(results.next())
                {
                    long id = results.getLong("id");
                    map.put(getBlacklist(id), id);
                }
            }
            return map;
        }
        catch(SQLException e)
        {
            Database.LOG.error("Error while getting the raw blacklisted users map.", e);
            return null;
        }
    }
}
