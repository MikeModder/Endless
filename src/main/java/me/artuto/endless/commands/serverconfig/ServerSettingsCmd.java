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

package me.artuto.endless.commands.serverconfig;

import com.jagrosh.jdautilities.command.Command;
import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.core.entities.Room;
import me.artuto.endless.utils.ArgsUtils;
import me.artuto.endless.utils.GuildUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

public class ServerSettingsCmd extends EndlessCommand
{
    private final Bot bot;

    public ServerSettingsCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "settings";
        this.children = new Command[]{new ModlogCmd(), new ServerlogCmd(), new WelcomeCmd(), new LeaveCmd(),
                new AdminRoleCmd(), new ModRoleCmd(), new MutedRoleCmd(), new BanDeleteDaysCmd(), new TimezoneCmd(), new SetFairQueueCmd()};
        this.help = "Displays the settings of the server";
        this.category = Categories.SERVER_CONFIG;
        this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
        this.guildOnly = true;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        EmbedBuilder builder = new EmbedBuilder();
        Guild guild = event.getGuild();
        String title = Const.INFO+" "+event.localize("command.settings", guild.getName());
        GuildSettings settings = bot.endless.getGuildSettings(guild);

        // pre-localized strings
        String yes = event.localize("misc.yes");
        String no = event.localize("misc.no");
        String none = " "+event.localize("misc.none");
        String disabled = " "+event.localize("misc.disabled");
        String dontdel = event.localize("command.settings.value.bdd.dontDel");

        // titles
        String logs = event.localize("command.settings.title.logs");
        String msgs = event.localize("command.settings.title.msgs");
        String gs = event.localize("command.settings.title.gs");
        String sb = event.localize("command.settings.title.sb");
        String m = event.localize("command.settings.title.m");

        Emote starboardEmote = getEmote(guild, settings.getStarboardEmote());
        int banDeleteDays = settings.getBanDeleteDays();
        int starboardCount = settings.getStarboardCount();
        Role adminRole = GuildUtils.getAdminRole(guild);
        Role djRole = event.getGuild().getRoleById(settings.getDJRole());
        Role modRole = GuildUtils.getModRole(guild);
        Role mutedRole = GuildUtils.getMutedRole(guild);
        Room.Mode roomMode = settings.getRoomMode();
        String fairQueue = "**"+(settings.isFairQueueEnabled()?yes:no)+"**";
        String welcomeDm = settings.getWelcomeDM();
        String welcomeMsg = settings.getWelcomeMsg();
        String leaveMsg = settings.getLeaveMsg();
        TextChannel modlog = guild.getTextChannelById(settings.getModlog());
        TextChannel serverlog = guild.getTextChannelById(settings.getServerlog());
        TextChannel welcome = guild.getTextChannelById(settings.getWelcomeChannel());
        TextChannel leave = guild.getTextChannelById(settings.getLeaveChannel());
        TextChannel musicTc = guild.getTextChannelById(settings.getTextChannelMusic());
        TextChannel starboard = guild.getTextChannelById(settings.getStarboard());
        VoiceChannel musicVc = guild.getVoiceChannelById(settings.getVoiceChannelMusic());
        ZoneId tz = settings.getTimezone();

        StringBuilder logsString = new StringBuilder();
        StringBuilder messagesString = new StringBuilder();
        StringBuilder settingsString = new StringBuilder();
        StringBuilder starboardString = new StringBuilder();
        StringBuilder musicString = new StringBuilder();

        logsString.append(event.localize("command.settings.value.modlog")).append((modlog==null?none:" **"+modlog.getAsMention()+"**"))
                .append(event.localize("command.settings.value.serverlog")).append((serverlog==null?none:" **"+serverlog.getAsMention()+"**"))
                .append(event.localize("command.settings.value.welcome")).append((welcome==null?none:" **"+welcome.getAsMention()+"**"))
                .append(event.localize("command.settings.value.leave")).append((leave==null?none:" **"+leave.getAsMention()+"**"));

        messagesString.append(event.localize("command.settings.value.welcome.dm")).append((welcomeDm==null?none:" `"+welcomeDm+"`"))
                .append(event.localize("command.settings.value.welcome.msg")).append((welcomeMsg==null?none:" `"+welcomeMsg+"`"))
                .append(event.localize("command.settings.value.leave.msg")).append((leaveMsg==null?none:" `"+leaveMsg+"`"));

        settingsString.append(event.localize("command.settings.value.adminR")).append((adminRole==null?none:" **"+adminRole.getAsMention()+"**"))
                .append(event.localize("command.settings.value.modR")).append((modRole==null?none:" **"+modRole.getAsMention()+"**"))
                .append(event.localize("command.settings.value.mutedR")).append((mutedRole==null?none:" **"+mutedRole.getAsMention()+"**"))
                .append(event.localize("command.settings.value.bdd")).append((banDeleteDays==0?dontdel:" **"+banDeleteDays+"**"))
                .append(event.localize("command.settings.value.room")).append(" **").append(roomMode.getName()).append("**")
                .append(event.localize("command.settings.value.tz")).append(" **").append(tz.toString()).append("**")
                .append(event.localize("command.settings.value.locale")).append(" **").append(settings.getLocale().getLocalizedName()).append("**");

        starboardString.append(event.localize("command.settings.value.starboard")).append((starboard==null?none:" **"+starboard.getAsMention()+"**"))
                .append(event.localize("command.settings.value.starboard.count")).append((starboardCount==0?disabled:" **"+starboardCount+"**"))
                .append(event.localize("command.settings.value.starboard.emote")).append(" "+(starboardEmote==null?
                settings.getStarboardEmote():starboardEmote.getAsMention()));

        musicString.append(event.localize("command.settings.value.music.djR")).append((djRole==null?none:" **"+djRole.getAsMention()+"**"))
                .append(event.localize("command.settings.value.music.tc")).append((musicTc==null?none:" **"+musicTc.getAsMention()+"**"))
                .append(event.localize("command.settings.value.music.vc")).append((musicVc==null?none:" **"+musicVc.getName()+"**"))
                .append(event.localize("command.settings.value.music.fq")).append(" ").append(fairQueue);

        builder.addField(":mag: "+logs, logsString.toString(), false);
        builder.addField(":speech_balloon: "+msgs, messagesString.toString(), false);
        builder.addField(":bar_chart: "+gs, settingsString.toString(), false);
        builder.addField(":star: "+sb, starboardString.toString(), false);
        builder.addField(":notes: "+m, musicString.toString(), false);

        builder.setColor(event.getSelfMember().getColor());
        event.reply(new MessageBuilder().append(title).setEmbed(builder.build()).build());
    }

    private class ModlogCmd extends EndlessCommand
    {
        ModlogCmd()
        {
            this.name = "modlog";
            this.help = "Sets the modlog channel";
            this.arguments = "<#channel|Channel ID|Channel name>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a text channel or NONE";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(event.getArgs().equalsIgnoreCase("none"))
            {
                bot.gsdm.setModlogChannel(event.getGuild(), null);
                event.replySuccess("command.settings.disabled", "Modlogging");
            }
            else
            {
                TextChannel tc = ArgsUtils.findTextChannel(event, event.getArgs());
                if(tc==null)
                    return;

                bot.gsdm.setModlogChannel(event.getGuild(), tc);
                event.replySuccess("command.settings.modlog", tc.getAsMention());
            }
        }
    }

    private class ServerlogCmd extends EndlessCommand
    {
        ServerlogCmd()
        {
            this.name = "serverlog";
            this.help = "Sets the serverlog channel";
            this.arguments = "<#channel|Channel ID|Channel name>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a text channel or NONE";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(event.getArgs().equalsIgnoreCase("none"))
            {
                bot.gsdm.setServerlogChannel(event.getGuild(), null);
                event.replySuccess("command.settings.disabled", "Serverlogging");
            }
            else
            {
                TextChannel tc = ArgsUtils.findTextChannel(event, event.getArgs());
                if(tc==null)
                    return;

                bot.gsdm.setServerlogChannel(event.getGuild(), tc);
                event.replySuccess("command.settings.serverlog", tc.getAsMention());
            }
        }
    }

    private class WelcomeCmd extends EndlessCommand
    {
        WelcomeCmd()
        {
            this.name = "welcome";
            this.aliases = new String[]{"joinschannel", "joinslog", "joins"};
            this.help = "Sets the welcome channel";
            this.arguments = "<#channel|Channel ID|Channel name>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a text channel or NONE";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(event.getArgs().equalsIgnoreCase("none"))
            {
                bot.gsdm.setWelcomeChannel(event.getGuild(), null);
                event.replySuccess("command.settings.disabled", "Welcome channel");
            }
            else
            {
                TextChannel tc = ArgsUtils.findTextChannel(event, event.getArgs());
                if(tc==null)
                    return;

                bot.gsdm.setWelcomeChannel(event.getGuild(), tc);
                event.replySuccess("command.settings.welcome", tc.getAsMention());
            }
        }
    }

    private class LeaveCmd extends EndlessCommand
    {
        LeaveCmd()
        {
            this.name = "leave";
            this.aliases = new String[]{"leaveschannel", "leaveslog", "leaves"};
            this.help = "Sets the leave channel";
            this.arguments = "<#channel|Channel ID|Channel name>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a text channel or NONE";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(event.getArgs().equalsIgnoreCase("none"))
            {
                bot.gsdm.setLeaveChannel(event.getGuild(), null);
                event.replySuccess("command.settings.disabled", "Leave channel");
            }
            else
            {
                TextChannel tc = ArgsUtils.findTextChannel(event, event.getArgs());
                if(tc==null)
                    return;

                bot.gsdm.setLeaveChannel(event.getGuild(), tc);
                event.replySuccess("command.settings.leave", tc.getAsMention());
            }
        }
    }

    private class AdminRoleCmd extends EndlessCommand
    {
        AdminRoleCmd()
        {
            this.name = "adminrole";
            this.help = "Sets the admin role";
            this.arguments = "<@Role|Role ID|Role name>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a role or NONE";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(event.getArgs().equalsIgnoreCase("none"))
            {
                bot.gsdm.setAdminRole(event.getGuild(), null);
                event.replySuccess("command.settings.disabled", "Admin role");
            }
            else
            {
                Role role = ArgsUtils.findRole(event, event.getArgs());
                if(role==null)
                    return;

                if(!(GuildUtils.getAdminRole(event.getGuild())==null))
                {
                    event.replyError("command.settings.adminR.already");
                    return;
                }

                bot.gsdm.setAdminRole(event.getGuild(), role);
                event.replySuccess("command.settings.adminR", role.getName());
            }
        }
    }

    private class ModRoleCmd extends EndlessCommand
    {
        ModRoleCmd()
        {
            this.name = "modrole";
            this.help = "Sets the mod role";
            this.arguments = "<@Role|Role ID|Role name>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a role or NONE";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(event.getArgs().equalsIgnoreCase("none"))
            {
                bot.gsdm.setModRole(event.getGuild(), null);
                event.replySuccess("command.settings.disabled", "Mod role");
            }
            else
            {
                Role role = ArgsUtils.findRole(event, event.getArgs());
                if(role==null)
                    return;

                if(!(GuildUtils.getModRole(event.getGuild())==null))
                {
                    event.replyError("command.settings.modR.already");
                    return;
                }

                bot.gsdm.setModRole(event.getGuild(), role);
                event.replySuccess("command.settings.modR", role.getName());
            }
        }
    }

    private class MutedRoleCmd extends EndlessCommand
    {
        MutedRoleCmd()
        {
            this.name = "mutedrole";
            this.help = "Sets the muted role";
            this.arguments = "<@Role|Role ID|Role name>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a role or NONE";
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(event.getArgs().equalsIgnoreCase("none"))
            {
                bot.gsdm.setMutedRole(event.getGuild(), null);
                event.replySuccess("command.settings.disabled", "Muted role");
            }
            else
            {
                Role role = ArgsUtils.findRole(event, event.getArgs());
                if(role==null)
                    return;

                if(!(GuildUtils.getMutedRole(event.getGuild())==null))
                {
                    event.replyError("command.settings.mutedR.already");
                    return;
                }

                bot.gsdm.setMutedRole(event.getGuild(), role);
                event.replySuccess("command.settings.mutedR", role.getName());
            }
        }
    }

    private class BanDeleteDaysCmd extends EndlessCommand
    {
        BanDeleteDaysCmd()
        {
            this.name = "bandeletedays";
            this.help = "Sets the amount of messages to delete when banning";
            this.arguments = "<number of day(s)>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please include a number or 0";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            switch(event.getArgs())
            {
                case "0":
                    bot.gsdm.setBanDeleteDays(event.getGuild(), 0);
                    event.replySuccess("command.settings.bdd.set", "0 (No deletion)");
                case "1":
                    bot.gsdm.setBanDeleteDays(event.getGuild(), 1);
                    event.replySuccess("command.settings.bdd.set", "1");
                case "7":
                    bot.gsdm.setBanDeleteDays(event.getGuild(), 7);
                    event.replySuccess("command.settings.bdd.set", "7");
                default:
                    event.replyError("command.settings.bdd.invalid");
            }
        }
    }

    private class TimezoneCmd extends EndlessCommand
    {
        TimezoneCmd()
        {
            this.name = "timezone";
            this.help = "Sets the timezone for logs on the guild";
            this.arguments = "<timezone>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            String args = event.getArgs();
            ZoneId tz;

            try {tz = ZoneId.of(args);}
            catch(ZoneRulesException e)
            {
                event.replyError("command.settings.tz.invalid");
                return;
            }

            bot.gsdm.setTimezone(event.getGuild(), tz);
            event.replySuccess("command.settings.tz.set");
        }
    }

    private class SetFairQueueCmd extends EndlessCommand
    {
        SetFairQueueCmd()
        {
            this.name = "fairqueue";
            this.help = "Toggles on and off the fairqueue";
            this.arguments = "<status true or false>";
            this.category = Categories.SERVER_CONFIG;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Please provide me `true` or `false`!";
            this.parent = ServerSettingsCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(GuildUtils.isPremiumGuild(event.getGuild())))
            {
                event.replyError("command.settings.fp.notDonator");
                return;
            }

            String args = event.getArgs();

            if(args.equalsIgnoreCase("true"))
            {
                bot.gsdm.setFairQueueStatus(event.getGuild(), true);
                event.replySuccess("command.settings.fp.set", "enabled");
            }
            else if(args.equalsIgnoreCase("false"))
            {
                bot.gsdm.setFairQueueStatus(event.getGuild(), false);
                event.replySuccess("command.settings.fp.set", "disabled");
            }
            else
                event.replyError("command.settings.fp.invalid");
        }
    }

    private Emote getEmote(Guild guild, String emote)
    {
        long id;
        try {id = Long.parseLong(emote);}
        catch(NumberFormatException e) {return null;}

        return guild.getEmoteById(id);
    }
}
