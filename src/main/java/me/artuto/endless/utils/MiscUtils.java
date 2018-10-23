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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.artuto.endless.Const;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author Artuto
 */

public class MiscUtils
{
    public static int isCase(Message m, int caseNum)
    {
        if(!(m.getAuthor().getIdLong()==m.getJDA().getSelfUser().getIdLong()))
            return 0;
        String match = "(?is)`\\[.{8}\\]` `\\["+(caseNum==-1?"(\\d+)":caseNum)+"\\]` .+";
        if(m.getContentRaw().matches(match))
            return caseNum==-1?Integer.parseInt(m.getContentRaw().replaceAll(match, "$1")):caseNum;
        return 0;
    }

    public static InputStream getInputStream(String url)
    {
        try
        {
            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url)
                    .method("GET", null)
                    .header("user-agent", Const.USER_AGENT)
                    .build();

            return client.newCall(request).execute().body().byteStream();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String getImageUrl(String format, String size, String url)
    {
        if(url==null)
            return null;

        if(!(format==null) && !(url.endsWith("gif")))
            url = url.replace(url.substring(url.length()-3), format);
        if(!(size==null))
            url = url+"?size="+size;

        return url;
    }

    public static boolean isNSFWAllowed(CommandEvent event)
    {
        if(event.isFromType(ChannelType.TEXT))
            return event.getTextChannel().isNSFW();
        return true;
    }
}
