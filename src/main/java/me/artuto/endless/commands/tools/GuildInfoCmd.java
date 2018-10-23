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

package me.artuto.endless.commands.tools;

import me.artuto.endless.Const;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.utils.FormatUtil;
import me.artuto.endless.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.time.format.DateTimeFormatter;

/**
 * @author Artuto
 */

public class GuildInfoCmd extends EndlessCommand
{
    public GuildInfoCmd()
    {
        this.name = "guild";
        this.aliases = new String[]{"server", "serverinfo", "guildinfo", "ginfo", "sinfo"};
        this.help = "Shows info about the current guild";
        this.category = Categories.TOOLS;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        StringBuilder sb = new StringBuilder();
        Guild guild;

        if(!(event.getArgs().isEmpty()) && event.isOwner())
        {
            long id;
            try {id = Long.parseLong(event.getArgs());}
            catch(NumberFormatException ignored) {id = 0L;}

            guild = event.getJDA().asBot().getShardManager().getGuildById(id);
            if(guild==null)
            {
                event.replyWarning(false, "Could not find that guild! :(");
                return;
            }
        }
        else
            guild = event.getGuild();

        int memberCount = (int)guild.getMemberCache().size();
        int botCount = (int)guild.getMemberCache().stream().filter(m -> m.getUser().isBot()).count();
        int onlineCount = (int)guild.getMemberCache().stream().filter(m -> m.getOnlineStatus()==OnlineStatus.ONLINE).count();
        int idleCount = (int)guild.getMemberCache().stream().filter(m -> m.getOnlineStatus()==OnlineStatus.IDLE).count();
        int dndCount = (int)guild.getMemberCache().stream().filter(m -> m.getOnlineStatus()==OnlineStatus.DO_NOT_DISTURB).count();
        int offlineCount = (int)guild.getMemberCache().stream().filter(m -> m.getOnlineStatus()==OnlineStatus.OFFLINE).count();
        User owner = guild.getOwner().getUser();

        sb.append(Const.LINE_START).append(" ID: **").append(guild.getId()).append("**\n");
        sb.append(Const.LINE_START).append(" ").append(event.localize("command.guild.owner")).append(" **").append(owner.getName())
                .append("#").append(owner.getDiscriminator()).append("**\n");
        sb.append(Const.LINE_START).append(" ").append(event.localize("command.guild.region")).append(" **").append(guild.getRegion().getName()).append(" ")
                .append(guild.getRegion().getEmoji()).append("**\n");
        sb.append(Const.LINE_START).append(" ").append(event.localize("command.guild.creation")).append(" **")
                .append(guild.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("**\n");
        sb.append(Const.LINE_START).append(" ").append(event.localize("command.guild.members")).append(" ").append(Const.ONLINE).append(" **")
                .append(onlineCount).append("** - ").append(Const.IDLE).append(" **").append(idleCount).append("** - ").append(Const.DND).append(" **")
                .append(dndCount).append("** - ").append(Const.OFFLINE).append(" **").append(offlineCount).append("** (**").append(memberCount).append("**, ")
                .append(Const.BOT).append(" **").append(botCount).append("**)\n");
        sb.append(Const.LINE_START).append(" ").append(event.localize("command.guild.channels")).append(" ").append(event.localize("command.guild.tcs"))
                .append(" **").append(guild.getTextChannelCache().size()).append("** - ").append(event.localize("command.guild.vcs")).append(" **")
                .append(guild.getVoiceChannelCache().size()).append("**\n");
        sb.append(Const.LINE_START).append(" ").append(event.localize("command.guild.verify")).append(" **")
                .append(getVerificationLevel(event, guild.getVerificationLevel().getKey())).append("**\n");
        sb.append(Const.LINE_START).append(" ").append(event.localize("command.guild.explicitLevel")).append(" **")
                .append(guild.getExplicitContentLevel().getDescription()).append("**");

        if(!(guild.getSplashId()==null))
        {
            sb.append("\n_ _\n").append(Const.PARTNER).append(" **").append("command.guild.partner").append("** ").append(Const.PARTNER);
            builder.setImage(MiscUtils.getImageUrl("png", "2048", guild.getSplashUrl()));
        }

        builder.setColor(guild.getMember(owner).getColor()).setDescription(sb)
                .setThumbnail(MiscUtils.getImageUrl("png", null, guild.getIconUrl()));
        boolean verified = guild.getFeatures().contains("VERIFIED");
        String title = FormatUtil.sanitize(":computer: "+event.localize("command.guild.title", guild.getName(), verified?Const.VERIFIED:""));
        event.reply(mb.setContent(title).setEmbed(builder.build()).build());
    }

    private String getVerificationLevel(EndlessCommandEvent event, int level)
    {
        switch(level)
        {
            case 0:
                return event.localize("misc.none");
            case 1:
                return event.localize("command.guild.verify.low");
            case 2:
                return event.localize("command.guild.verify.medium");
            case 3:
                return "(╯°□°）╯︵ ┻━┻";
            case 4:
                return "┻━┻ ﾐヽ(ಠ益ಠ)ノ彡┻━┻";
            default:
                return event.localize("misc.unknown");
        }
    }
}
