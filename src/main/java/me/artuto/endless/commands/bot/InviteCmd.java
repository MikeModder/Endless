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

package me.artuto.endless.commands.bot;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.cmddata.Categories;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.utils.MiscUtil;

/**
 * @author Artuto
 */

public class InviteCmd extends EndlessCommand
{
    public InviteCmd()
    {
        this.name = "invite";
        this.help = "Shows the bot invite";
        this.category = Categories.BOT;
        this.guildOnly = false;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(CommandEvent event)
    {
        String message = "Hi! I'm **Endless**, if you want to add me to your guild, use this link:\n" +
                ":link: **<%s>**\n" +
                "You can also join the support server:\n" +
                ":inbox_tray: **<%s>**";

        if(event.getArgs().isEmpty())
            event.replyFormatted(message, event.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR),
                    event.getClient().getServerInvite());
        else
        {
            long id = 0L;
            try
            {
                id = MiscUtil.parseSnowflake(event.getArgs());
            }
            catch(NumberFormatException ignored) {}

            event.replyFormatted(message, event.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR)+(id==0L?"":"&guild_id="+id),
                    event.getClient().getServerInvite());
        }
    }
}
