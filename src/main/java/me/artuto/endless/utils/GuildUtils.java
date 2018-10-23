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

package me.artuto.endless.utils;

import me.artuto.endless.Bot;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.core.entities.Ignore;
import me.artuto.endless.core.entities.ParsedAuditLog;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.audit.AuditLogKey;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GuildUtils
{
    public static Bot bot;

    public GuildUtils(Bot bot)
    {
        GuildUtils.bot = bot;
    }

    public static boolean isPremiumGuild(Guild guild)
    {
        User owner = guild.getOwner().getUser();
        if(bot.client.getOwnerIdLong()==owner.getIdLong() || Arrays.asList(bot.client.getCoOwnerIdsLong()).contains(owner.getIdLong()))
            return true;
        Guild rootGuild = bot.shardManager.getGuildById(bot.config.getRootGuildId());
        if(rootGuild==null)
            return false;
        Role donators = rootGuild.getRolesByName("Donators", true).stream().findFirst().orElse(null);
        if(donators==null)
            return false;
        return rootGuild.getMembersWithRoles(donators).stream().anyMatch(m -> m.getUser().getIdLong()==owner.getIdLong());
    }

    public static int getBanDeleteDays(Guild guild)
    {
        GuildSettings settings = bot.endless.getGuildSettings(guild);
        return settings.getBanDeleteDays();
    }

    public static int getStarboardCount(Guild guild)
    {
        GuildSettings settings = bot.endless.getGuildSettings(guild);
        return settings.getStarboardCount();
    }

    public static Role getAdminRole(Guild guild)
    {
        return guild.getRolesByName("Admin", true)
                .stream().findFirst().orElse(guild.getRoleById(bot.endless.getGuildSettings(guild).getAdminRole()));
    }

    public static Role getModRole(Guild guild)
    {
        return guild.getRolesByName("Moderator", true)
                .stream().findFirst().orElse(guild.getRoleById(bot.endless.getGuildSettings(guild).getModRole()));
    }

    public static Role getMutedRole(Guild guild)
    {
        return guild.getRolesByName("Muted", true)
                .stream().findFirst().orElse(guild.getRoleById(bot.endless.getGuildSettings(guild).getMutedRole()));
    }

    public static TextChannel getStarboardChannel(Guild guild)
    {
        GuildSettings settings = bot.endless.getGuildSettings(guild);
        return guild.getTextChannelById(settings.getStarboard());
    }

    public static ParsedAuditLog getAuditLog(AuditLogEntry entry, AuditLogKey key)
    {
        return bot.endlessBuilder.entityBuilder.createParsedAuditLog(entry, key);
    }
}
