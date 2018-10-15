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

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.libraries.GoogleAPIAuth;
import net.dv8tion.jda.core.Permission;

import java.io.IOException;
import java.util.List;

/**
 * @author Artuto
 */

public class YouTubeCmd extends EndlessCommand
{
    private final Bot bot;
    private final YouTube youtube;

    public YouTubeCmd(Bot bot)
    {
        this.youtube = new YouTube.Builder(GoogleAPIAuth.HTTP_TRANSPORT, GoogleAPIAuth.JSON_FACTORY, GoogleAPIAuth.HTTP_REQUEST)
                .setApplicationName("EndlessBot").build();
        this.bot = bot;
        this.name = "youtube";
        this.aliases = new String[]{"yt", "ytsearch"};
        this.help = "Search for a video on YouTube!";
        this.arguments = "<query>";
        this.category = Categories.UTILS;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
        this.cooldown = 10;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(bot.config.getYouTubeKey().isEmpty())
        {
            event.replyError(false, "YouTube API Key is not configured!");
            return;
        }

        String query = event.getArgs();
        try
        {
            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(bot.config.getYouTubeKey());
            search.setQ(query);
            search.setType("video,playlist");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url,id/playlistId)");
            search.setMaxResults(10L);
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            if(searchResultList==null)
                event.replyWarning("command.yt.notFound");
            else
            {
                StringBuilder sb = new StringBuilder();
                SearchResult result = searchResultList.get(0);
                ResourceId rId = result.getId();

                if(rId.getKind()==null)
                {
                    event.reactError();
                    return;
                }

                if(rId.getKind().equals("youtube#playlist"))
                    sb.append(":file_folder: **https://youtube.com/playlist?list=").append(rId.getPlaylistId()).append("**");
                else if(rId.getKind().equals("youtube#video"))
                    sb.append(":video_camera: **https://youtube.com/watch?v=").append(rId.getVideoId()).append("**");

                event.reply(sb.toString());
            }
        }
        catch(IOException ignored) {}
    }
}
