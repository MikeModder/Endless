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
import me.artuto.endless.core.exceptions.ConfigException;
import me.artuto.endless.core.exceptions.GuildException;
import me.artuto.endless.core.exceptions.OwnerException;
import me.artuto.endless.loader.Config;
import org.slf4j.LoggerFactory;

/**
 * @author Artuto
 */

public class StartupChecker
{
    public static Logger LOG = (Logger)LoggerFactory.getLogger("Startup Checker");

    public static void checkConfig(Config config)
    {
        if(!(isConfigValid(config)))
            throw new ConfigException();
    }

    private static boolean isConfigValid(Config config)
    {
/*
        long owner = config.getOwnerId();
        long rootGuild = config.getRootGuildId();

        if(!(owner==Const.ARTUTO_ID || owner==Const.ARTUTO_ALT_ID))
            throw new OwnerException();

        if(!(rootGuild==Const.MAIN_GUILD || rootGuild==Const.GUILD_TESTING))
            throw new GuildException();
*/

        return true;
    }
}
