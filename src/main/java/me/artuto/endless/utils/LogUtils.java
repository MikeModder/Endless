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
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Artuto
 */

public class LogUtils
{
    public static StringBuilder getStackTrace(ILoggingEvent event)
    {
        IThrowableProxy proxy = event.getThrowableProxy();
        ThrowableProxy throwableImpl = (ThrowableProxy)proxy;
        StringBuilder stacktrace = new StringBuilder(event.getFormattedMessage());

        if(!(proxy==null))
        {
            Throwable throwable = throwableImpl.getThrowable();

            List<StackTraceElementProxy> list = Arrays.asList(proxy.getStackTraceElementProxyArray());
            String message = proxy.getMessage();
            if(!(message==null))
            {
                stacktrace.append("\n\n```java\n");
                if(!(throwable==null))
                    stacktrace.append(throwable.getClass().getName()).append(": ");
                stacktrace.append(message);
            }
            for(StackTraceElementProxy element : list)
            {
                String call = element.getSTEAsString();
                if(call.length()+stacktrace.length()>MessageEmbed.TEXT_MAX_LENGTH)
                {
                    stacktrace.append("\n... (").append(list.size()-list.indexOf(element)+1).append(" more calls)");
                    break;
                }
                stacktrace.append("\n").append(call).append("\n");
            }
            stacktrace.append("```");
        }

        return stacktrace;
    }

    public static File getAvatarUpdateImage(UserUpdateAvatarEvent event)
    {
        String newA = event.getNewAvatarUrl()==null?event.getUser().getDefaultAvatarUrl():event.getUser().getEffectiveAvatarUrl();
        String oldA = event.getOldAvatarUrl()==null?event.getUser().getDefaultAvatarUrl():event.getOldAvatarUrl();

        try
        {
            BufferedImage img1 = ImageIO.read(Objects.requireNonNull(MiscUtils.getInputStream(oldA)));
            BufferedImage img2 = ImageIO.read(Objects.requireNonNull(MiscUtils.getInputStream(newA)));
            BufferedImage combo = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = combo.createGraphics();
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, 256, 128);
            g2.drawImage(img1, 0, 0, 128, 128, null);
            g2.drawImage(img2, 128, 0, 128, 128, null);

            File f = new File("avatarchange"+event.getUser().getId()+".png");
            ImageIO.write(combo, "png", f);
            return f;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
