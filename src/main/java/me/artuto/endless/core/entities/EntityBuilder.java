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

package me.artuto.endless.core.entities;

import me.artuto.endless.Bot;
import me.artuto.endless.Locale;
import me.artuto.endless.core.entities.impl.*;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audit.AuditLogChange;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.audit.AuditLogKey;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Artuto
 */

public class EntityBuilder
{
    public static final ZoneId DEFAULT_TZ = ZoneId.of("GMT-5");
    private final Bot bot;

    public EntityBuilder(Bot bot)
    {
        this.bot = bot;
    }

    public Blacklist createBlacklist(ResultSet results) throws SQLException
    {
        Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmt.setTimeInMillis(results.getLong("time"));
        return new BlacklistImpl(BlacklistType.valueOf(results.getString("type")),
                results.getLong("id"),
                OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId()),
                results.getString("reason"));
    }

    public GuildSettings createGuildSettings(Guild guild, ResultSet results) throws SQLException
    {
        List<Role> colorMeRoles = new LinkedList<>();
        List<Role> roleMeRoles = new LinkedList<>();
        List<Tag> importedTags = new LinkedList<>();
        Set<String> prefixes = new HashSet<>();
        String array = results.getString("prefixes");

        if(!(array == null))
        {
            for(Object prefix : new JSONArray(array))
                prefixes.add(prefix.toString());
        }

        array = results.getString("colorme_roles");
        if(!(array == null))
        {
            for(Object preRole : new JSONArray(array))
            {
                Role role = guild.getRoleById(preRole.toString());

                if(!(role==null))
                    colorMeRoles.add(role);
            }
        }

        array = results.getString("roleme_roles");
        if(!(array == null))
        {
            for(Object preRole : new JSONArray(array))
            {
                Role role = guild.getRoleById(preRole.toString());

                if(!(role==null))
                    roleMeRoles.add(role);
            }
        }

        array = results.getString("imported_tags");
        if(!(array == null))
        {
            for(Object part : new JSONArray(array))
            {
                long id = Long.valueOf(part.toString());
                Tag tag = bot.tdm.getGlobalTagById(id);
                if(tag==null)
                    tag = bot.tdm.getLocalTagById(guild.getIdLong(), id);
                if(!(tag==null))
                    importedTags.add(tag);
            }
        }

        ZoneId tz = results.getString("logs_timezone")==null?DEFAULT_TZ:
                ZoneId.of(results.getString("logs_timezone"));

        return new GuildSettingsImpl(false,
                results.getBoolean("fair_queue_enabled"),
                results.getBoolean("repeat_mode_enabled"),
                prefixes,
                guild,
                results.getInt("ban_delete_days"),
                results.getInt("starboard_count"),
                results.getInt("volume")==0?100:results.getInt("volume"),
                bot.db.getIgnoresForGuild(guild),
                colorMeRoles,
                roleMeRoles,
                importedTags,
                results.getString("locale")==null?Locale.EN_US:Locale.valueOf(results.getString("locale")),
                results.getLong("admin_role_id"),
                results.getLong("dj_role_id"),
                results.getLong("leave_id"),
                results.getLong("modlog_id"),
                results.getLong("mod_role_id"),
                results.getLong("muted_role_id"),
                results.getLong("serverlog_id"),
                results.getLong("starboard_id"),
                results.getLong("tc_music_id"),
                results.getLong("vc_music_id"),
                results.getLong("welcome_id"),
                results.getString("room_mode")==null?Room.Mode.NO_CREATION:Room.Mode.valueOf(results.getString("room_mode")),
                results.getString("leave_msg"),
                results.getString("starboard_emote")==null?"\u2B50":results.getString("starboard_emote"),
                results.getString("welcome_dm"),
                results.getString("welcome_msg"),
                tz);
    }

    public Ignore createIgnore(ResultSet results) throws SQLException
    {
        return new IgnoreImpl(results.getLong("entity_id"),
                results.getLong("guild_id"));
    }

    public LocalTag createLocalTag(ResultSet results) throws SQLException
    {
        return new LocalTagImpl(results.getBoolean("overriden"),
                results.getLong("guild"),
                results.getLong("owner"),
                results.getInt("id"),
                results.getString("content"),
                results.getString("name"));
    }

    public ParsedAuditLog createParsedAuditLog(AuditLogEntry entry, AuditLogKey key)
    {
        JDA jda = entry.getJDA();
        User author = entry.getUser();
        User target = jda.getUserById(entry.getTargetIdLong());

        if(!(key==null))
        {
            AuditLogChange change = entry.getChangeByKey(key);
            if(!(change==null))
                return new ParsedAuditLogImpl(key, change.getNewValue(), change.getOldValue(), entry.getReason(), author, target);
            else
                return null;
        }
        else
            return new ParsedAuditLogImpl(null, null, null, entry.getReason(), author, target);
    }

    public Profile createProfile(ResultSet results, User user) throws SQLException
    {
        return new ProfileImpl(results.getString("donation"),
                results.getString("timezone"),
                results.getString("twitter"),
                results.getString("steam"),
                results.getString("wii"),
                results.getString("nnid"),
                results.getString("xboxlive"),
                results.getString("psn"),
                results.getString("3ds"),
                results.getString("skype"),
                results.getString("youtube"),
                results.getString("about"),
                results.getString("twitch"),
                results.getString("minecraft"),
                results.getString("email"),
                results.getString("lol"),
                results.getString("wow"),
                results.getString("battle"),
                results.getString("splatoon"),
                results.getString("mkwii"),
                results.getString("reddit"),
                user);
    }

    public TempPunishment createTempPunishment(ResultSet results) throws SQLException
    {
        Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmt.setTimeInMillis(results.getLong("time"));
        return new PunishmentImpl(PunishmentType.valueOf(results.getString("type")),
                results.getLong("guild_id"),
                results.getLong("user_id"),
                OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId()));
    }

    public Poll createPoll(ResultSet results) throws SQLException
    {
        return new PollImpl(results.getLong("end_time"),
                results.getLong("guild_id"),
                results.getLong("msg_id"),
                results.getLong("channel_id"));
    }

    public Punishment createPunishment(ResultSet results) throws SQLException
    {
        return new PunishmentImpl(PunishmentType.valueOf(results.getString("type")),
                results.getLong("guild_id"),
                results.getLong("user_id"),
                null);
    }

    public Reminder createReminder(ResultSet results) throws SQLException
    {
        Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmt.setTimeInMillis(results.getLong("expiry_time"));
        OffsetDateTime expiry = OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
        gmt.setTimeInMillis(results.getLong("start_time"));
        OffsetDateTime start = OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());

        return new ReminderImpl(results.getLong("id"),
                results.getLong("channel_id"),
                results.getLong("user_id"),
                expiry,
                start,
                results.getString("msg"));
    }

    public Room createRoom(ResultSet results) throws SQLException
    {
        return new RoomImpl(results.getBoolean("restricted"),
                results.getLong("guild_id"),
                results.getLong("tc_id"),
                results.getLong("owner_id"),
                results.getLong("vc_id"));
    }

    public StarboardMessage createStarboardMessage(ResultSet results) throws SQLException
    {
        return new StarboardMessageImpl(results.getLong("msg_id"),
                results.getLong("tc_id"),
                results.getLong("guild_id"),
                results.getInt("star_amount"),
                results.getLong("starboard_msg_id"));
    }

    public Tag createTag(ResultSet results) throws SQLException
    {
        return new GlobalTagImpl(results.getLong("owner"),
                results.getInt("id"),
                results.getString("content"),
                results.getString("name"));
    }
}
