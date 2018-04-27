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

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.sentry.Sentry;
import me.artuto.endless.loader.Config;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Artuto
 */

public class EndlessLoader
{
    // Data Sources
    public Config config;
    public DatabaseLoader dbLoader;

    // Thread Loader
    private final ThreadLoader threadLoader = new ThreadLoader();

    // Threads

    public ScheduledExecutorService botlogThread;
    public ScheduledExecutorService cleanThread;
    public ScheduledExecutorService cmdThread;
    public ScheduledExecutorService starboardThread;

    // Waiter Loader
    private final WaiterLoader waiterLoader = new WaiterLoader();

    // Waiters
    public EventWaiter waiter;

    public void preLoad()
    {
        config = new StartupChecker().checkConfig();

        if(config.isSentryEnabled() && !(config.getSentryDSN().isEmpty()))
            Sentry.init(config.getSentryDSN());
    }

    public void databaseLoad(boolean maintenance)
    {
        dbLoader = new DatabaseLoader(config).initialize(maintenance);
    }

    public void threadLoad()
    {
        botlogThread = threadLoader.createThread("Botlog");
        cleanThread = threadLoader.createThread("Clean Command");
        cmdThread = threadLoader.createThread("Commands");
        starboardThread = threadLoader.createThread("Starboard");
    }

    public void waiterLoad()
    {
        waiter = waiterLoader.createWaiter();
    }
}
