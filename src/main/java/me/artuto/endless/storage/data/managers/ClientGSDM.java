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

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import me.artuto.endless.Bot;
import me.artuto.endless.core.entities.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;

import javax.annotation.Nullable;

public class ClientGSDM implements GuildSettingsManager<GuildSettings>
{
    private final Bot bot;

    public ClientGSDM(Bot bot)
    {
        this.bot = bot;
    }

    @Nullable
    @Override
    public GuildSettings getSettings(Guild guild)
    {
        if(bot.endless==null)
            return null;
        else
            return bot.endless.getGuildSettings(guild);
    }
}
