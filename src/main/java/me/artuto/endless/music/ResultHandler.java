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

package me.artuto.endless.music;

import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.utils.ChecksUtil;
import me.artuto.endless.utils.FormatUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

/**
 * @author Artuto
 */

public class ResultHandler implements AudioLoadResultHandler
{
    private final Bot bot;
    private final boolean ytSearch;
    private final EndlessCommandEvent event;
    private final Message msg;

    public ResultHandler(Bot bot, boolean ytSearch, EndlessCommandEvent event, Message msg)
    {
        this.bot = bot;
        this.ytSearch = ytSearch;
        this.event = event;
        this.msg = msg;
    }

    @Override
    public void trackLoaded(AudioTrack track)
    {
        loadSingle(track, null);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist)
    {
        if(playlist.getTracks().size()==1 || playlist.isSearchResult())
        {
            AudioTrack track = playlist.getSelectedTrack()==null?playlist.getTracks().get(0):playlist.getSelectedTrack();
            loadSingle(track, null);
        }
        else if(!(playlist.getSelectedTrack()==null))
        {
            AudioTrack track = playlist.getSelectedTrack();
            loadSingle(track, playlist);
        }
        else
        {
            String message = event.localize("core.music.results.playlist", (playlist.getName()==null?"Unknown title":playlist.getName()),
                    playlist.getTracks().size());
            msg.editMessage(FormatUtil.sanitize(event.getClient().getSuccess()+" "+message)).queue();
        }
    }

    @Override
    public void noMatches()
    {
        if(ytSearch)
        {
            msg.editMessage(FormatUtil.sanitize(event.getClient().getWarning()+" "+event.localize("core.music.results.notFound",
                    event.getArgs()))).queue();
        }
        else
            bot.audioManager.loadItemOrdered(event.getGuild(), "ytsearch:"+event.getArgs(), new ResultHandler(bot, true, event, msg));
    }

    @Override
    public void loadFailed(FriendlyException exception)
    {
        if(exception.severity==FriendlyException.Severity.COMMON)
            msg.editMessage(event.getClient().getError()+" "+event.localize("core.music.error.message", exception.getMessage())).queue();
        else
            msg.editMessage(event.getClient().getError()+" "+event.localize("core.music.error")).queue();
    }

    private void loadSingle(AudioTrack track, AudioPlaylist playlist)
    {
        int pos = bot.musicTasks.putInQueue(track, event)+2;
        String added = event.localize("core.music.added", track.getInfo().title, FormatUtil.formatTime(track.getDuration()));
        String position = pos==1?event.localize("core.music.added.playing"):event.localize("core.music.added.queued", pos);
        String addMsg = FormatUtil.sanitize(event.getClient().getSuccess()+" "+added+" "+position);

        if(playlist==null || !(ChecksUtil.hasPermission(event.getSelfMember(), event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)))
            msg.editMessage(addMsg).queue();
        else
        {
            new ButtonMenu.Builder()
                    .setText(addMsg+"\n"+event.getClient().getWarning()+" "+event.localize("core.music.added.playlistAsk",
                            playlist.getTracks().size(), event.getClient().getSuccess()))
                    .setChoices("444226239683624962", "444226355555729428")
                    .setEventWaiter(bot.waiter)
                    .setTimeout(20, TimeUnit.SECONDS)
                    .setAction(re -> {
                        if(re.isEmote() && re.getEmote().getAsMention().equals(event.getClient().getSuccess()))
                        {
                            msg.editMessage(addMsg+"\n"+event.getClient().getSuccess()+" "+event.localize("core.music.added.playlist",
                                    loadPl(playlist))).queue();
                        }
                    }).setFinalAction(m -> {
                        try{m.clearReactions().queue();}
                        catch(PermissionException ignored){}
            }).build().display(msg);
        }
    }

    private int loadPl(AudioPlaylist pl)
    {
        pl.getTracks().forEach(track -> bot.musicTasks.putInQueue(track, event));
        return pl.getTracks().size();
    }
}
