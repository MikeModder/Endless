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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.artuto.endless.commands.EndlessCommandEvent;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Artuto
 */

public class ArgsUtils
{
    private static final Pattern ID = Pattern.compile("(?:^|\\s)(\\d{17,22})(?:$|\\s)");

    public static long parseTime(String timestr)
    {
        timestr = timestr.replaceAll("(?i)(\\s|,|and)","").replaceAll("(?is)(-?\\d+|[a-z]+)", "$1 ").trim();
        String[] vals = timestr.split("\\s+");
        long timeinseconds = 0;

        try
        {
            for(int j=0; j<vals.length; j+=2)
            {
                int num = Integer.parseInt(vals[j]);
                if(vals[j+1].toLowerCase().startsWith("m"))
                    num*=60;
                else if(vals[j+1].toLowerCase().startsWith("h"))
                    num*=60*60;
                else if(vals[j+1].toLowerCase().startsWith("d"))
                    num*=60*60*24;
                else if(vals[j+1].toLowerCase().startsWith("w"))
                    return -1;
                else if(vals[j+1].toLowerCase().startsWith("mo"))
                    num*=60*60*24*30;
                else if(vals[j+1].toLowerCase().startsWith("y"))
                    num*=60*60*24*365;
                timeinseconds+=num;
            }
        }
        catch(Exception ex)
        {
            return 0;
        }

        return timeinseconds;
    }

    public static Channel findChannel(CommandEvent event, String query)
    {
        return findChannel(false, event, query);
    }

    public static Channel findChannel(boolean categories, CommandEvent event, String query)
    {
        Guild guild = event.getGuild();
        List<TextChannel> tcs = FinderUtil.findTextChannels(query, guild);
        if(tcs.isEmpty())
        {
            List<VoiceChannel> vcs = FinderUtil.findVoiceChannels(query, guild);
            if(vcs.isEmpty())
            {
                if(categories)
                {
                    List<Category> cats = FinderUtil.findCategories(query, guild);
                    if(cats.isEmpty())
                    {
                        event.replyWarning("I was not able to found a channel with the provided arguments: '"+query+"'");
                        return null;
                    }
                    else if(cats.size()>1)
                    {
                        event.replyWarning(FormatUtil.listOfCategories(cats, query));
                        return null;
                    }
                    else
                        return cats.get(0);
                }
                else
                {
                    event.replyWarning("I was not able to found a channel with the provided arguments: '"+query+"'");
                    return null;
                }
            }
            else if(vcs.size()>1)
            {
                event.replyWarning(FormatUtil.listOfVcChannels(vcs, query));
                return null;
            }
            else
                return vcs.get(0);
        }
        else if(tcs.size()>1)
        {
            event.replyWarning(FormatUtil.listOfTcChannels(tcs, query));
            return null;
        }
        else
            return tcs.get(0);
    }

    public static Emote findEmote(CommandEvent event, String query)
    {
        List<Emote> list = FinderUtil.findEmotes(query, event.getJDA());
        if(list.isEmpty())
        {
            event.replyWarning("No Emotes found matching \""+query+"\"");
            return null;
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfEmotes(list, query));
            return null;
        }
        else
            return list.get(0);
    }

    public static Member findMember(CommandEvent event, String query)
    {
        List<Member> list = FinderUtil.findMembers(query, event.getGuild());

        if(list.isEmpty())
        {
            event.replyWarning("I was not able to found a user with the provided arguments: '"+query+"'");
            return null;
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfMembers(list, query));
            return null;
        }
        else
            return list.get(0);
    }

    public static TextChannel findTextChannel(boolean jda, EndlessCommandEvent event, String query)
    {
        List<TextChannel> list = FinderUtil.findTextChannels(query, event.getGuild());
        if(list.isEmpty())
        {
            if(jda)
                list = FinderUtil.findTextChannels(query, event.getJDA());

            if(list.isEmpty())
            {
                event.replyWarning("No Text Channels found matching \""+query+"\"");
                return null;
            }
            else if(list.size()>1)
            {
                event.replyWarning(FormatUtil.listOfTcChannels(list, query));
                return null;
            }
            else
                return list.get(0);
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfTcChannels(list, query));
            return null;
        }
        else
            return list.get(0);
    }

    public static TextChannel findTextChannel(EndlessCommandEvent event, String query)
    {
        return findTextChannel(false, event, query);
    }

    public static Role findRole(EndlessCommandEvent event, String query)
    {
        List<Role> list = FinderUtil.findRoles(query, event.getGuild());
        if(list.isEmpty())
        {
            event.replyWarning("No Roles found matching \""+query+"\"");
            return null;
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfRoles(list, query));
            return null;
        }
        else
            return list.get(0);
    }

    public static User findBannedUser(CommandEvent event, String query)
    {
        List<User> list = FinderUtil.findBannedUsers(query, event.getGuild());

        if(list.isEmpty())
        {
            event.replyWarning("I was not able to found a banned user with the provided arguments: '"+query+"'");
            return null;
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfUsers(list, query));
            return null;
        }
        else
            return list.get(0);
    }

    public static User findUser(boolean full, CommandEvent event, String query)
    {
        List<User> list = FinderUtil.findUsers(query, event.getJDA());

        if(list.isEmpty())
        {
            if(full)
            {
                Matcher m = ID.matcher(query);
                if(m.matches())
                {
                    try
                    {
                        return event.getJDA().retrieveUserById(m.group(1)).complete();
                    }
                    catch(ErrorResponseException ignored) {}
                }
            }
            event.replyWarning("I was not able to found a user with the provided arguments: '"+query+"'");
            return null;
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfUsers(list, query));
            return null;
        }
        else
            return list.get(0);
    }

    public static VoiceChannel findVoiceChannel(EndlessCommandEvent event, String query)
    {
        List<VoiceChannel> list = FinderUtil.findVoiceChannels(query, event.getGuild());
        if(list.isEmpty())
        {
            event.replyWarning("No Voice Channels found matching \""+query+"\"");
            return null;
        }
        else if(list.size()>1)
        {
            event.replyWarning(FormatUtil.listOfVcChannels(list, query));
            return null;
        }
        else
            return list.get(0);
    }

    public static String[] split(int limit, String args)
    {
        try
        {
            String[] argsArr = args.split("\\s", limit);
            return new String[]{argsArr[0], argsArr[1]};
        }
        catch(IndexOutOfBoundsException e)
        {
            return new String[]{args, ""};
        }
    }

    public static String[] splitWithReason(int limit, String args, String regex)
    {
        try
        {
            String[] argsArr = args.split(regex, limit);
            return new String[]{argsArr[0], argsArr[1]};
        }
        catch(IndexOutOfBoundsException e)
        {
            return new String[]{args, "[no reason provided]"};
        }
    }

    public static String[] splitWithReasonAndTime(int limit, String args, String regex)
    {
        long time;
        String reason = "[no reason specified]";
        String target;

        try
        {
            String[] argsArr = args.split(regex, limit);
            target = argsArr[0].trim();
            time = ArgsUtils.parseTime(argsArr[1].trim());

            try
            {
                if(time==0)
                    reason = argsArr[1].trim();
                else
                    reason = argsArr[1].trim().split(regex, 2)[1].trim();
            }
            catch(ArrayIndexOutOfBoundsException ignored) {}
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            target = args.trim();
            time = 0;
        }

        return new String[]{target, String.valueOf(time), reason};
    }

    public static String[] splitWithSeparator(int limit, String args, String regex)
    {
        try
        {
            String[] argsArr = args.split(regex, limit);
            return new String[]{argsArr[0], argsArr[1]};
        }
        catch(IndexOutOfBoundsException e)
        {
            return new String[]{args, ""};
        }
    }

    public static Map<String, String> parseArgs(String[] flags, String[] args)
    {
        String currentFlag = null;
        Map<String, String> output = new HashMap<>();

        for(String w : args)
        {
            if(w.startsWith("-"))
            {
                if(w.length()>1)
                {
                    String flag = Arrays.stream(flags).filter(f -> f.equalsIgnoreCase(w.substring(1, 2))).findFirst().orElse(null);
                    if(!(flag==null))
                    {
                        currentFlag = flag;
                        output.put(flag, w.replace(w, "").trim());
                    }
                }
            }
            else
            {
                if(currentFlag==null)
                {
                    String title = output.getOrDefault("title", "");
                    output.put("title", (title+" "+w).trim());
                }
                else
                {
                    String value = output.get(currentFlag);
                    output.put(currentFlag, (value+" "+w).trim());
                }
            }
        }

        return output;
    }
}
