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

import me.artuto.endless.Const;
import me.artuto.endless.core.entities.Punishment;
import me.artuto.endless.core.entities.TempPunishment;

import java.time.OffsetDateTime;

/**
 * @author Artuto
 */

public class PunishmentImpl implements Punishment, TempPunishment
{
    private Const.PunishmentType type;
    private long guildId, userId;
    private OffsetDateTime expiryTime;

    public PunishmentImpl(Const.PunishmentType type, long guildId, long userId, OffsetDateTime expiryTime)
    {
        this.type = type;
        this.guildId = guildId;
        this.userId = userId;
        this.expiryTime = expiryTime;
    }

    @Override
    public Const.PunishmentType getType()
    {
        return type;
    }

    @Override
    public long getGuildId()
    {
        return guildId;
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
}
