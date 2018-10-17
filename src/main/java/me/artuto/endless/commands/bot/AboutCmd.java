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
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.cmddata.Categories;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;

/**
 * @author Artuto
 */

public class AboutCmd extends EndlessCommand
{
    private final Bot bot;

    public AboutCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "about";
        this.category = Categories.BOT;
        this.help = "Info about the bot";
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(CommandEvent event)
    {
        Color color;

        if(event.isFromType(ChannelType.PRIVATE)) color = Color.decode("#33ff00");
        else color = event.getGuild().getSelfMember().getColor();

        String title = ":information_source: Information about **"+event.getSelfUser().getName()+"**";
        EmbedBuilder builder = new EmbedBuilder();
        User owner = event.getJDA().getUserById(bot.config.getOwnerId());
        String ownername = owner.getName()+"#"+owner.getDiscriminator();
        String ownerid = owner.getId();

        builder.setDescription("Hi, I'm Endless! A multipurpose bot designed to be smart.\n"+"If you found a bug please contact my dad\n"+"("+Const.DEV+")!\n");
        builder.addField(":bust_in_silhouette: Owner:", "**"+ownername+"** (**"+ownerid+"**)", false);
        builder.addField("<:jda:325395909347115008>  Library:", "Java Discord API (JDA) "+JDAInfo.VERSION+" and JDA Utilities "+JDAUtilitiesInfo.VERSION, false);
        builder.addField(":link: Support Guild:", "**[Support]("+Const.INVITE+")**\n", false);
        builder.setFooter("Version: "+Const.VERSION+" | Latest Start", null);
        builder.setColor(color);
        builder.setTimestamp(event.getClient().getStartTime());
        builder.setThumbnail(event.getSelfUser().getAvatarUrl());
        event.reply(new MessageBuilder().append(title).setEmbed(builder.build()).build());
    }
}
