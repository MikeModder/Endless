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

package me.artuto.endless.commands.botadm;

import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import net.dv8tion.jda.bot.sharding.ShardManager;

/**
 * @author Artuto
 */

public class ShutdownCmd extends EndlessCommand
{
    private final Bot bot;

    public ShutdownCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "shutdown";
        this.aliases = new String[]{"quit", "exit", "close", "terminate"};
        this.arguments = "[shard id]";
        this.help = "Turns Off the bot.";
        this.category = Categories.BOTADM;
        this.ownerCommand = true;
        this.guildOnly = false;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        ShardManager shardManager = event.getJDA().asBot().getShardManager();

        if(event.getArgs().isEmpty())
        {
            event.reactSuccess();
            event.getClient().shutdown();
            shardManager.shutdown();
            bot.db.shutdown();
        }
        else
        {
            int shard;
            try
            {
                shard = Integer.parseInt(event.getArgs());
            }
            catch(NumberFormatException e)
            {
                event.replyError("Invalid shard ID provided!");
                return;
            }

            if(shard<event.getJDA().getShardInfo().getShardTotal()|| shard>event.getJDA().getShardInfo().getShardTotal())
            {
                event.replyWarning("A Shard with the ID `"+shard+"` doesn't exists!");
                return;
            }

            event.reactSuccess();
            event.getJDA().asBot().getShardManager().shutdown(shard);
        }
    }
}
