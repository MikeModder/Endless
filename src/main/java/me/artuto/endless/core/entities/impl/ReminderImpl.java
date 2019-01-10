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

package me.artuto.endless.core.entities.impl;

import me.artuto.endless.core.entities.Reminder;

import java.time.OffsetDateTime;

/**
 * @author Artuto
 */

public class ReminderImpl implements Reminder
{
    private final long id, channelId, userId;
    private final OffsetDateTime expiryTime, startTime;
    private final String message;

    public ReminderImpl(long id, long channelId, long userId, OffsetDateTime expiryTime, OffsetDateTime startTime, String message)
    {
        this.id = id;
        this.channelId = channelId;
        this.userId = userId;
        this.expiryTime = expiryTime;
        this.startTime = startTime;
        this.message = message;
    }

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public long getChannelId()
    {
        return channelId;
    }

    @Override
    public long getUserId()
    {
        return userId;
    }

    @Override
    public OffsetDateTime getExpiryTime()
    {
        return expiryTime;
    }

    @Override
    public OffsetDateTime getStartTime()
    {
        return startTime;
    }

    @Override
    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return String.format("RM:%d(%d)", channelId, userId);
    }
}
