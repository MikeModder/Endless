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

package me.artuto.endless.commands.utils;

import com.jagrosh.jdautilities.menu.Paginator;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Artuto
 */

public class DiscriminatorCmd extends EndlessCommand
{
    private final Paginator.Builder menu;

    public DiscriminatorCmd(Bot bot)
    {
        this.name = "discriminator";
        this.help = "Displays a list of users with the specified discriminator";
        this.arguments = "[discriminator]";
        this.aliases = new String[]{"discrim", "searchdiscrim"};
        this.category = Categories.UTILS;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.needsArguments = false;
        this.menu = new Paginator.Builder().setColumns(1)
                .setItemsPerPage(10)
                .showPageNumbers(true)
                .waitOnSinglePage(false)
                .useNumberedItems(false)
                .setFinalAction(m -> {
                    try {m.clearReactions().queue();}
                    catch(PermissionException ignored) {m.delete().queue();}
                })
                .setEventWaiter(bot.waiter)
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        String discrim;
        int number;

        if(event.getArgs().isEmpty())
        {
            number = Integer.parseInt(event.getAuthor().getDiscriminator());
            discrim = event.getAuthor().getDiscriminator();
        }
        else
        {
            try
            {
                number = Integer.parseInt(event.getArgs());
                discrim = event.getArgs();
            }
            catch(NumberFormatException ignored)
            {
                event.replyError("command.discrim.invalid.number");
                return;
            }
        }

        if(number<1 || number>9999)
        {
            event.replyError("command.discrim.invalid.range");
            return;
        }

        List<User> users = event.getJDA().asBot().getShardManager().getUsers().stream().filter(u -> u.getDiscriminator().equals(discrim))
                .collect(Collectors.toList());
        if(users.isEmpty())
        {
            event.replyWarning("Could not find any user with that discriminator!");
            return;
        }

        menu.clearItems();
        users.forEach(u -> menu.addItems("**"+u.getName()+"**#**"+u.getDiscriminator()+"** (ID: "+u.getId()+")"));
        Paginator p = menu.setColor(event.isFromType(ChannelType.TEXT)?event.getSelfMember().getColor():Color.decode("#33ff00"))
                .setText(event.getClient().getSuccess()+" "+event.localize("command.discrim.title", discrim))
                .setUsers(event.getAuthor()).build();
        p.paginate(event.getChannel(), 1);
    }
}
