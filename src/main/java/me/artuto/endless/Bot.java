/*
 * Copyright (C) 2017 Artu
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

package me.artuto.endless;

import com.jagrosh.jdautilities.waiter.EventWaiter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import me.artuto.endless.data.Settings;
import me.artuto.endless.loader.Config;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Artu
 */

public class Bot extends ListenerAdapter
{
   private final HashMap<String,Settings> settings;
    private final Config config;
    private JDA jda;
    
    public Bot(Config config)
    {
        this.config = config;
        this.settings = new HashMap<>();
    }
    
    @Override
    public void onGuildJoin(GuildJoinEvent event)
    {
        Guild guild = event.getGuild();
        User owner = event.getJDA().getUserById(config.getOwnerId());
        String leavemsg = "Hi! Sorry, but you can't have a copy of Endless on Discord Bots, this is for my own security.\n"
                    + "Please remove this Account from the Discord Bots list or I'll take further actions.\n"
                    + "If you think this is an error, please contact the Developer. ~Artuto";
        String warnmsg = "<@264499432538505217>, **"+owner.getName()+"#"+owner.getDiscriminator()+"** has a copy of Endless here!";
        Long ownerId = config.getOwnerId();
        
        if(event.getGuild().getId().equals("110373943822540800") || event.getGuild().getId().equals("264445053596991498") && !(ownerId==264499432538505217L))
        {
            event.getJDA().getTextChannelById("119222314964353025").sendMessage(warnmsg).complete();
            owner.openPrivateChannel().queue(s -> s.sendMessage(leavemsg).queue(null, (e) -> SimpleLog.getLog("DISCORD BOTS").fatal(leavemsg)));
            guild.leave().complete();
        }
    }
}
