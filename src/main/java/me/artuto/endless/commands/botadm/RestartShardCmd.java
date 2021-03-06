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

import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.cmddata.Categories;
import net.dv8tion.jda.bot.sharding.ShardManager;

/**
 * @author Artuto
 */

public class RestartShardCmd extends EndlessCommand
{
    public RestartShardCmd()
    {
        this.name = "restartshard";
        this.aliases = new String[]{"restart"};
        this.arguments = "[shard id]";
        this.help = "Restarts the specified shard.";
        this.category = Categories.BOTADM;
        this.ownerCommand = true;
        this.guildOnly = false;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(CommandEvent event)
    {
        ShardManager shardManager = event.getJDA().asBot().getShardManager();

        if(event.getArgs().isEmpty())
        {
            event.reactSuccess();
            shardManager.restart();
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
            event.getJDA().asBot().getShardManager().restart(shard);
        }
    }
}
