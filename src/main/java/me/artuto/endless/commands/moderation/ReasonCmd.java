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

package me.artuto.endless.commands.moderation;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.cmddata.Categories;
import net.dv8tion.jda.core.Permission;

/**
 * @author Artuto
 */

public class ReasonCmd extends EndlessCommand
{
    private final Bot bot;

    public ReasonCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "reason";
        this.help = "Updates the reason for the specified case, if not specified uses the latest case.";
        this.arguments = "[case] <reason>";
        this.category = Categories.MODERATION;
        this.userPerms = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void executeCommand(CommandEvent event)
    {

    }
}
