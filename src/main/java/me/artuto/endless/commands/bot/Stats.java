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

package me.artuto.endless.commands.bot;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

import me.artuto.endless.cmddata.Categories;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

/**
 *
 * @author Artu
 */

public class Stats extends Command
{
    public Stats()
    {
        this.name = "stats";
        this.help = "Shows the stats of the bot";
        this.category = Categories.BOT;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.ownerCommand = false;
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(CommandEvent event)
    {
        String title = ":information_source: Stats of **"+event.getSelfUser().getName()+"**:";
        Color color;
        String os = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getName();
        String arch = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getArch();
        String version = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getVersion();
        os = os+" "+arch+" "+version;
        int cpus = Runtime.getRuntime().availableProcessors();
        String processCpuLoad = new DecimalFormat("###.###%").format(ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getProcessCpuLoad());
        String systemCpuLoad = new DecimalFormat("###.###%").format(ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class).getSystemCpuLoad());
        long ramUsed = ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / (1024 * 1024));

        if(event.isFromType(ChannelType.PRIVATE))
            color = Color.decode("#33ff00");
        else
            color = event.getGuild().getSelfMember().getColor();
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.addField("<:windows:371075985996775425> OS: ", os, true);
        builder.addField(":computer: RAM usage: ", ramUsed+"MB", true);
        builder.addField(":gear: CPU usage: ", processCpuLoad+" / "+systemCpuLoad+" ("+cpus+" Cores)", true);
        builder.addField(":map: Guilds: ", ""+event.getJDA().getGuilds().size() , true);
        builder.addField(":speech_balloon: Text Channels: ", ""+event.getJDA().getTextChannels().size(), true);
        builder.addField(":speaker: Voice Channels: ", ""+event.getJDA().getVoiceChannels().size(), true);
        builder.addField(":bust_in_silhouette: Users: ", ""+event.getJDA().getUsers().size(), true);
        builder.setFooter(event.getSelfUser().getName(), event.getSelfUser().getEffectiveAvatarUrl());
        builder.setColor(color);
        event.getChannel().sendMessage(new MessageBuilder().append(title).setEmbed(builder.build()).build()).queue();
    }
}