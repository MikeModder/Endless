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

package me.artuto.endless.bootloader;

import me.artuto.endless.tools.EndlessThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Artuto
 */

public class ThreadLoader
{
    public static ScheduledThreadPoolExecutor createThread(String name)
    {
        try
        {
            return new ScheduledThreadPoolExecutor(2, new EndlessThreadFactory(name));
        }
        catch(Exception e)
        {
            throw new RuntimeException("Error creating the '"+name+"' thread!");
        }
    }
}
