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

package me.artuto.endless.utils;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.artuto.endless.music.AudioPlayerSendHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Artuto
 *
 */

public class FormatUtil
{
    private static final Pattern MENTION = Pattern.compile("<@!?(\\d{17,22})>");

    public static Message nowPlayingMessage(Guild guild, String successEmoji)
    {
        MessageBuilder mb = new MessageBuilder();
        mb.append(successEmoji).append(" **Now Playing...**");
        EmbedBuilder eb = new EmbedBuilder();
        AudioPlayerSendHandler ah = (AudioPlayerSendHandler)guild.getAudioManager().getSendingHandler();
        eb.setColor(guild.getSelfMember().getColor());
        if(ah==null || !(ah.isMusicPlaying()))
        {
            eb.setTitle("No music playing");
            eb.setDescription("\u23F9 "+progressBar(-1)+" "+volumeIcon(ah==null?100:ah.getPlayer().getVolume()));
        }
        else
        {
            if(ah.getRequester()!=0)
            {
                User u = guild.getJDA().getUserById(ah.getRequester());
                if(u==null)
                    eb.setAuthor("Unknown (ID:"+ah.getRequester()+")", null, null);
                else
                    eb.setAuthor(u.getName()+"#"+u.getDiscriminator(), null, u.getEffectiveAvatarUrl());
            }

            try{eb.setTitle(ah.getPlayer().getPlayingTrack().getInfo().title, ah.getPlayer().getPlayingTrack().getInfo().uri);}
            catch(Exception ignored){eb.setTitle(ah.getPlayer().getPlayingTrack().getInfo().title);}

            if(ah.getPlayer().getPlayingTrack() instanceof YoutubeAudioTrack)
                eb.setThumbnail("https://img.youtube.com/vi/"+ah.getPlayer().getPlayingTrack().getIdentifier()+"/mqdefault.jpg");

            eb.setDescription(FormatUtil.embedFormat(ah));
        }
        return mb.setEmbed(eb.build()).build();
    }

    public static String embedFormat(AudioPlayerSendHandler handler)
    {
        if(handler==null)
            return "No music playing\n\u23F9 "+progressBar(-1)+" "+volumeIcon(100);
        else if (!(handler.isMusicPlaying()))
            return "No music playing\n\u23F9 "+progressBar(-1)+" "+volumeIcon(handler.getPlayer().getVolume());
        else
        {
            AudioTrack track = handler.getPlayer().getPlayingTrack();
            double progress = (double)track.getPosition()/track.getDuration();
            return (handler.getPlayer().isPaused()?"\u23F8":"\u25B6")
                    +" "+progressBar(progress)
                    +" `["+formatTime(track.getPosition()) + "/" + formatTime(track.getDuration()) +"]` "
                    +volumeIcon(handler.getPlayer().getVolume());
        }
    }

    private static String progressBar(double percent)
    {
        String str = "";
        for(int i=0; i<12; i++)
            if(i == (int)(percent*12))
                str+="\uD83D\uDD18";
            else
                str+="▬";
        return str;
    }

    public static String volumeIcon(int volume)
    {
        if(volume == 0)
            return "\uD83D\uDD07";
        if(volume < 30)
            return "\uD83D\uDD08";
        if(volume < 70)
            return "\uD83D\uDD09";
        return "\uD83D\uDD0A";
    }

    public static String formatTime(long duration)
    {
        if(duration == Long.MAX_VALUE)
            return "LIVE";
        long seconds = Math.round(duration/1000.0);
        long hours = seconds/(60*60);
        seconds %= 60*60;
        long minutes = seconds/60;
        seconds %= 60;
        return (hours>0 ? hours+":" : "") + (minutes<10 ? "0"+minutes : minutes) + ":" + (seconds<10 ? "0"+seconds : seconds);
    }

    public static String formatLogClean(String message, OffsetDateTime now, ZoneId tz, int caseId, String emote, String aN, String aD, String verb, int number, long tcId, String crit, String reason)
    {
        Matcher m = MENTION.matcher(crit);
        while(m.find())
            crit = crit.replaceAll(MENTION.pattern(), "$1");
        return sanitize(String.format(message, timeF(now, tz), caseId, emote, aN, aD, verb, number, tcId, crit, reason));
    }

    public static String formatLogGeneral(String message, OffsetDateTime now, ZoneId tz, int caseId, String emote, String aN, String aD, String verb, String tN, String tD, long tId, String reason)
    {
        return String.format(message, timeF(now, tz), caseId, emote, aN, aD, verb, tN, tD, tId, reason);
    }

    public static String formatLogTemp(String message, OffsetDateTime now, ZoneId tz, int caseId, String expT, String emote, String aN, String aD, String verb, String tN, String tD, long tId, String reason)
    {
        return String.format(message, timeF(now, tz), caseId, emote, aN, aD, verb, tN, tD, tId, reason, expT);
    }

    public static String listOfCategories(List<Category> list, String query)
    {
        String out = " Multiple categories found matching \""+query+"\":";
        for(int i = 0; i<6 && i<list.size(); i++)
            out += "\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6) out += "\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String listOfEmotes(List<Emote> list, String query)
    {
        String out = " Multiple emotes found matching \""+query+"\":";
        for(int i = 0; i<6 && i<list.size(); i++)
            out += "\n"+list.get(i).getAsMention()+" - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6) out += "\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String listOfMembers(List<Member> list, String query)
    {
        String out = " Multiple members found matching \""+query+"\":";
        for(int i = 0; i<6 && i<list.size(); i++)
            out += "\n - "+list.get(i).getUser().getName()+"#"+list.get(i).getUser().getDiscriminator()+" (ID:"+list.get(i).getUser().getId()+")";
        if(list.size()>6) out += "\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String listOfUsers(List<User> list, String query)
    {
        String out = " Multiple users found matching \""+query+"\":";
        for(int i = 0; i<6 && i<list.size(); i++)
            out += "\n - "+list.get(i).getName()+"#"+list.get(i).getDiscriminator()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6) out += "\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String listOfRoles(List<Role> list, String query)
    {
        String out = " Multiple roles found matching \""+query+"\":";
        for(int i = 0; i<6 && i<list.size(); i++)
            out += "\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6) out += "\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String listOfTcChannels(List<TextChannel> list, String query)
    {
        String out = " Multiple text channels found matching \""+query+"\":";
        for(int i = 0; i<6 && i<list.size(); i++)
            out += "\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6) out += "\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String listOfVcChannels(List<VoiceChannel> list, String query)
    {
        String out = " Multiple voice channels found matching \""+query+"\":";
        for(int i = 0; i<6 && i<list.size(); i++)
            out += "\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
        if(list.size()>6) out += "\n**And "+(list.size()-6)+" more...**";
        return out;
    }

    public static String formatTimeFromSeconds(long seconds)
    {
        StringBuilder builder = new StringBuilder();
        int years = (int)(seconds / (60*60*24*365));
        if(years>0)
        {
            builder.append("**").append(years).append("** years, ");
            seconds = seconds % (60*60*24*365);
        }
        int weeks = (int)(seconds / (60*60*24*365));
        if(weeks>0)
        {
            builder.append("**").append(weeks).append("** weeks, ");
            seconds = seconds % (60*60*24*7);
        }
        int days = (int)(seconds / (60*60*24));
        if(days>0)
        {
            builder.append("**").append(days).append("** days, ");
            seconds = seconds % (60*60*24);
        }
        int hours = (int)(seconds / (60*60));
        if(hours>0)
        {
            builder.append("**").append(hours).append("** hours, ");
            seconds = seconds % (60*60);
        }
        int minutes = (int)(seconds / (60));
        if(minutes>0)
        {
            builder.append("**").append(minutes).append("** minutes, ");
            seconds = seconds % (60);
        }
        if(seconds>0)
            builder.append("**").append(seconds).append("** seconds");
        String str = builder.toString();
        if(str.endsWith(", "))
            str = str.substring(0, str.length()-2);
        if(str.isEmpty())
            str = "**No time**";
        return str;
    }

    public static String removeFormatting(String text)
    {
        return text.replace("_", "\\_").replace("*", "\\*").replace("~", "\\~")
                .replace("`", "\\`");
    }

    public static String sanitize(String message)
    {
        return message.replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim();
    }

    public static String timeF(OffsetDateTime time, ZoneId zone)
    {
        return time.atZoneSameInstant(zone).format(DateTimeFormatter.ISO_LOCAL_TIME).substring(0,8);
    }
}
