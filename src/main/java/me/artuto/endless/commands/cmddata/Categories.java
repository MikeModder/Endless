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
import me.artuto.endless.handlers.SpecialCaseHandler;

public class Categories
{
    private static boolean maintenance;
    private static BlacklistHandler bHandler;
    private static SpecialCaseHandler sHandler;

    public Categories(boolean maintenance, BlacklistHandler bHandler, SpecialCaseHandler shHndler)
    {
        Categories.bHandler = bHandler;
        Categories.maintenance = maintenance;
        Categories.sHandler = shHndler;
    }

    public static final Category BOT = new Category("Bot", event ->
    {
        if(event.isOwner())
            return true;

        if(!(Bot.getInstance().initialized))
            return false;
        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);

        return bHandler.handleBlacklist(event);
    });

    public static final Category BOTADM = new Category("Bot Administration", CommandEvent::isOwner);

    public static final Category MODERATION = new Category("Moderation", event ->
    {
        if(event.isOwner())
            return true;

        if(!(Bot.getInstance().initialized))
            return false;
        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);

        return bHandler.handleBlacklist(event);
    });

    public static final Category SERVER_CONFIG = new Category("Guild Settings", event ->
    {
        if(event.isOwner())
            return true;

        if(!(Bot.getInstance().initialized))
            return false;
        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);

        return bHandler.handleBlacklist(event);
    });

    public static final Category TOOLS = new Category("Tools", event ->
    {
        if(event.isOwner())
            return true;

        if(!(Bot.getInstance().initialized))
            return false;
        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);

        return bHandler.handleBlacklist(event);
    });

    public static final Category UTILS = new Category("Utilities", event ->
    {
        if(event.isOwner())
            return true;

        if(!(Bot.getInstance().initialized))
            return false;
        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);

        return bHandler.handleBlacklist(event);
    });

    public static final Category FUN = new Category("Fun", event ->
    {
        if(event.isOwner())
            return true;

        if(!(Bot.getInstance().initialized))
            return false;
        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);

        return bHandler.handleBlacklist(event);
    });

    /*public static final Category OTHERS = new Category("Others", event ->
    {
        if(event.isOwner())
            return true;

        if(maintenance)
            return sHandler.handleCommandInMaintenance(event);

        return bHandler.handleBlacklist(event);
    });*/
}
