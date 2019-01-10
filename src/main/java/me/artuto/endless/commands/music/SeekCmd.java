package me.artuto.endless.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.music.AudioPlayerSendHandler;
import me.artuto.endless.utils.FormatUtil;

public class SeekCmd extends MusicCommand
{
    public SeekCmd(Bot bot)
    {
        super(bot);
        this.name = "seek";
        this.category = MusicCommand.DJ;
        this.arguments = "";
        this.needsArguments = false;
        this.playing = true;
        this.listening = true;
    }

    @Override
    public void executeMusicCommand(EndlessCommandEvent event)
    {
        String args = event.getArgs();
        if(args.isEmpty())
        {
            event.replyError("command.seek.noArgs");
            return;
        }

        AudioPlayerSendHandler handler = (AudioPlayerSendHandler) event.getGuild().getAudioManager().getSendingHandler();
        AudioPlayer player = handler.getPlayer();
        AudioTrack track = player.getPlayingTrack();

        if(!(track.isSeekable()))
        {
            event.replyError("command.seek.notSeekable");
            return;
        }

        String[] split = args.split(":");
        long hour = 3600000;
        long duration = track.getDuration();
        if(duration >= hour && !(split.length > 2))
            args += ":00";
        else if(hour > duration)
            args = "00:"+args;

        long position = FormatUtil.formatTime(args);
        if(position == -1)
        {
            event.replyError("command.seek.invalid");
            return;
        }

        if(position > duration)
        {
            event.replyError("command.seek.invalidPos");
            return;
        }

        track.setPosition(position);
        event.replySuccess("command.seek.seeked", args);
    }
}
