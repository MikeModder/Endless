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

package me.artuto.endless.bootloader;

import ch.qos.logback.classic.Logger;
import me.artuto.endless.Const;
import me.artuto.endless.exceptions.ConfigException;
import me.artuto.endless.exceptions.GuildException;
import me.artuto.endless.exceptions.OwnerException;
import me.artuto.endless.loader.Config;
import org.slf4j.LoggerFactory;

/**
 * @author Artuto
 */

public class StartupChecker
{
    private Config config;
    public static Logger LOG = (Logger)LoggerFactory.getLogger("Startup Checker");

    public Config checkConfig()
    {
        this.config = new ConfigLoader().loadConfig();

        if(isConfigValid())
            return config;
        else throw new ConfigException();
    }

    private boolean isConfigValid()
    {
        String owner = String.valueOf(config.getOwnerId());
        String rootGuild = String.valueOf(config.getRootGuildId());

        if(!(owner.equals(Const.ARTUTO_ID) || owner.equals(Const.ARTUTO_ALT_ID)))
            throw new OwnerException();

        if(!(rootGuild.equals(Const.MAIN_GUILD) || rootGuild.equals(Const.GUILD_TESTING)))
            throw new GuildException();

        return true;
    }
}
