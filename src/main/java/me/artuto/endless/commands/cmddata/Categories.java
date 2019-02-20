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

package me.artuto.endless.commands.cmddata;

import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.Bot;
import me.artuto.endless.handlers.BlacklistHandler;
import me.artuto.endless.handlers.IgnoreHandler;
import me.artuto.endless.handlers.SpecialCaseHandler;

public class Categories
{
    private static boolean maintenance;
    private static BlacklistHandler bHandler;
    private static IgnoreHandler iHandler;
    public static SpecialCaseHandler sHandler;

    public Categories(boolean maintenance, BlacklistHandler bHandler, IgnoreHandler iHandler, SpecialCaseHandler shHndler)
    {
        Categories.maintenance = maintenance;
        Categories.bHandler = bHandler;
        Categories.iHandler = iHandler;
        Categories.sHandler = shHndler;
    }

    public static final Category BOT = new Category("Bot", Categories::doCheck);

    public static final Category BOTADM = new Category("Bot Administration", CommandEvent::isOwner);

    public static final Category MODERATION = new Category("Moderation", Categories::doCheck);

    public static final Category MUSIC = new Category("Music", Categories::doCheck);

    public static final Category SERVER_CONFIG = new Category("Guild Settings", event -> {
        if(!(Bot.getInstance().dataEnabled))
        {
            event.replyError("Endless is running in no-data mode.");
            return false;
        }

        return doCheck(event);
    });

    public static final Category TOOLS = new Category("Tools", Categories::doCheck);

    public static final Category UTILS = new Category("Utilities", Categories::doCheck);

    public static final Category FUN = new Category("Fun", Categories::doCheck);

    // public static final Category OTHERS = new Category("Others", Categories::doCheck);

    public static boolean doCheck(CommandEvent event)
    {
        if(event.isOwner())
            return true;

        if(!(Bot.getInstance().initialized))
            return false;
        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);
        if(!(event.getGuild()==null) &&  bHandler.isBlacklisted(event.getGuild()))
            return bHandler.handleBlacklist(event);
        if(bHandler.isBlacklisted(event.getAuthor()))
            return bHandler.handleBlacklist(event);

        return iHandler.handleIgnore(event);
    }
}
