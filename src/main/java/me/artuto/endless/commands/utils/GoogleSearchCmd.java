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

import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.libraries.GoogleAPIAuth;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleSearchCmd extends EndlessCommand
{
    private final Bot bot;
    private final Customsearch searcher;
    private final OrderedMenu.Builder oBuilder;

    public GoogleSearchCmd(Bot bot)
    {
        this.bot = bot;
        this.searcher = new Customsearch.Builder(GoogleAPIAuth.HTTP_TRANSPORT, GoogleAPIAuth.JSON_FACTORY, GoogleAPIAuth.HTTP_REQUEST)
                .setApplicationName("EndlessBot").build();
        this.name = "google";
        this.aliases = new String[]{"g", "search", "googlesearch"};
        this.help = "Search something on Google!";
        this.arguments = "[num] <query to search>";
        this.category = Categories.UTILS;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.cooldown = 15;
        this.oBuilder = new OrderedMenu.Builder().setDescription("Pick a result:\n")
                .setTimeout(2, TimeUnit.MINUTES).useCancelButton(true)
                .setCancel(m -> {})
                .setEventWaiter(bot.waiter);
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(bot.config.getGoogleKey().isEmpty())
        {
            event.replyError("command.google.key");
            return;
        }
        if(bot.config.getGoogleSearcherId().isEmpty())
        {
            event.replyError("command.google.searcher");
            return;
        }

        String[] inputs = event.getArgs().split("\\s+", 2);
        int num = 1;
        String query;

        if(inputs.length>1 && inputs[0].matches("\\d+"))
        {
            num = Integer.parseInt(inputs[0]);
            query = inputs[1];
        }
        else
            query = event.getArgs();

        if(num<1 || num>10)
        {
            event.replyWarning("command.google.range");
            return;
        }

        try
        {
            Customsearch.Cse.List list = searcher.cse().list(query);
            list.setKey(bot.config.getGoogleKey());
            list.setCx(bot.config.getGoogleSearcherId());
            list.setNum((long)num);
            Search searchResponse = list.execute();
            List<Result> resultList = searchResponse.getItems();

            if(resultList==null)
                event.replyWarning("command.google.notFound");
            else
            {
                if(resultList.size()==1)
                    handleSelection(event, resultList.get(0), query);
                else
                {
                    oBuilder.clearChoices();
                    int option = 0;
                    oBuilder.setUsers(event.getAuthor()).setColor(event.isFromType(ChannelType.TEXT)?event.getSelfMember().getColor():Color.decode("#33ff00"))
                            .setSelection((m, o) -> handleSelection(event, resultList.get(o-1), query));
                    oBuilder.setText(":mag: "+event.localize("command.google.title", query));
                    for(Result rslt : resultList)
                    {
                        if(option>9)
                            break;
                        String resultString = " **["+rslt.getTitle()+"]("+rslt.getLink()+")**\n";
                        oBuilder.addChoice(resultString);
                        option += 1;
                    }
                    oBuilder.build().display(event.getChannel());
                }
            }
        }
        catch(IOException ignored) {}
    }

    private void handleSelection(EndlessCommandEvent event, Result result, String query)
    {
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        String link = "**["+result.getTitle()+"]("+result.getLink()+")**\n";
        String descrip = result.getSnippet();

        builder.setColor(event.isFromType(ChannelType.TEXT)?event.getSelfMember().getColor():Color.decode("#33ff00")).setDescription(link+descrip);
        builder.setFooter("command.google.footer", "https://cdn.discordapp.com/emojis/447911997783277569.png");
        event.reply(mb.setContent(":mag: "+event.localize("command.google.title", query)).setEmbed(builder.build()).build());
    }
}