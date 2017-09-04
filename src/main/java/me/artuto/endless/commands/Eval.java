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

package me.artuto.endless.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Artu
 */

public class Eval extends Command
{
    private ScriptEngine engine;
    private List<String> imports;
    
    public Eval()
    {
        this.name = "eval";
        this.help = "Executes Groovy code";
        this.category = new Category("Bot Administration");
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.ownerCommand = false;
        this.guildOnly = false;
        
        engine = new ScriptEngineManager().getEngineByName("Groovy");

        try
        {
            imports = Arrays.asList("com.jagrosh.jdautilities",
                    "com.jagrosh.jdautilities.commandclient",
                    "com.jagrosh.jdautilities.commandclient.impl",
                    "com.jagrosh.jdautilities.entities",
                    "com.jagrosh.jdautilities.menu",
                    "com.jagrosh.jdautilities.utils",
                    "com.jagrosh.jdautilities.waiter",
                    "java.io",
                    "java.lang",
                    "java.util",
                    "me.artuto.endless",
                    "me.artuto.endless.commands",
                    "me.artuto.endless.data",
                    "me.artuto.endless.loader",
                    "me.artuto.endless.tools",
                    "me.artuto.endless.utils",
                    "net.dv8tion.jda.bot",
                    "net.dv8tion.jda.bot.entities",
                    "net.dv8tion.jda.bot.entities.impl",
                    "net.dv8tion.jda.core",
                    "net.dv8tion.jda.core.entities",
                    "net.dv8tion.jda.core.entities.impl",
                    "net.dv8tion.jda.core.managers",
                    "net.dv8tion.jda.core.managers.impl",
                    "net.dv8tion.jda.core.utils",
                    "net.dv8tion.jda.webhook");
        }
        catch(Exception ignored) {}
    }
	
    @Override
    protected void execute(CommandEvent event) 
    {
        String importString = "";
        String eval;

        if(!(event.isOwner()) && !(event.isCoOwner()))
        {
            event.replyError("Sorry, but you don't have access to this command! Only Bot owners!");
            return;   
        }
        
        try
        {
            engine.put("event", event);
            engine.put("jda", event.getJDA());
            engine.put("channel", event.getChannel());
            engine.put("message", event.getMessage());
            engine.put("bot", event.getSelfUser());
            engine.put("client", event.getClient());
            engine.put("author", event.getAuthor());
            if(event.isFromType(ChannelType.TEXT))
            {
                engine.put("member", event.getMember());
                engine.put("guild", event.getGuild());
                engine.put("tc", event.getTextChannel());
                engine.put("selfmember", event.getGuild().getSelfMember());
            }

            for(final String s : imports)
            {
                importString += "import "+ s + ".*;";
            }

            eval = event.getArgs().replaceAll("getToken", "getSelfUser");
            
            Object out = engine.eval(importString + eval);

            if(out==null || String.valueOf(out).isEmpty())
                event.reactSuccess();
            else
                event.replySuccess("Done! Output:\n```java\n"+out.toString().replaceAll(event.getJDA().getToken(), "Nice try.")+" ```");
        } 
        catch(Exception e2)
        {
            event.replyError("Error! Output:\n```java\n"+e2+" ```");
        }
    }
}
