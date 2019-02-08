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

package me.artuto.endless;

import com.jagrosh.jagtag.Parser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import me.artuto.endless.core.entities.GuildSettings;
import me.artuto.endless.handlers.ImportedTagHandler;
import me.artuto.endless.handlers.MutedRoleHandler;
import me.artuto.endless.handlers.StarboardHandler;
import me.artuto.endless.logging.ModLogging;
import me.artuto.endless.logging.ServerLogging;
import me.artuto.endless.storage.tempdata.AfkManager;
import me.artuto.endless.utils.FinderUtil;
import me.artuto.endless.utils.FormatUtil;
import me.artuto.endless.utils.LogUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.webhook.WebhookClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artuto
 */

public class Listener implements CommandListener, EventListener
{
    private final Bot bot;
    private final ModLogging modlog;
    private final Parser parser;
    private final ServerLogging serverlog;
    private final WebhookClient webhook;
    
    Listener(Bot bot)
    {
        this.bot = bot;
        this.modlog = bot.modlog;
        this.parser = Bot.tagParser;
        this.serverlog = bot.serverlog;
        this.webhook = bot.logWebhook;
    }

    @Override
    public void onEvent(Event preEvent)
    {
        if(preEvent instanceof ReadyEvent)
        {
            ReadyEvent event = (ReadyEvent)preEvent;
            JDA jda = event.getJDA();
            JDA.ShardInfo shardInfo = jda.getShardInfo();

            if(bot.config.isBotlogEnabled())
                webhook.send("Endless is now "+Const.ONLINE_ROLE+" - Connected to shard `"+shardInfo.getShardString()+"` Guilds: `"
                        +jda.getGuildCache().size()+"` " +"Users: `"+jda.getUserCache().size()+"`");

            Endless.LOG.info("Shard "+shardInfo.getShardString()+" ready!");
        }
        else if(preEvent instanceof ResumedEvent)
        {
            ResumedEvent event = (ResumedEvent)preEvent;
            JDA jda = event.getJDA();
            JDA.ShardInfo shardInfo = jda.getShardInfo();

            if(bot.config.isBotlogEnabled())
                webhook.send("Endless has "+Const.RESUMED_ROLE+" - Shard `"+shardInfo.getShardString()+"`");
        }
        else if(preEvent instanceof ReconnectedEvent)
        {
            ReconnectedEvent event = (ReconnectedEvent)preEvent;
            JDA jda = event.getJDA();
            JDA.ShardInfo shardInfo = jda.getShardInfo();

            if(bot.config.isBotlogEnabled())
                webhook.send("Endless has "+Const.RECONNECTED_ROLE+" - Shard `"+shardInfo.getShardString()+"`");
        }
        else if(preEvent instanceof ShutdownEvent)
        {
            ShutdownEvent event = (ShutdownEvent)preEvent;
            JDA jda = event.getJDA();
            JDA.ShardInfo shardInfo = jda.getShardInfo();

            if(bot.config.isBotlogEnabled())
                webhook.send("Endless has gone "+Const.OFFLINE_ROLE+" - Shard `"+shardInfo.getShardString()+"`");
        }
        else if(preEvent instanceof GuildJoinEvent)
        {
            GuildJoinEvent event = (GuildJoinEvent)preEvent;
            bot.botlog.logJoin(event);
            bot.sendStats(event.getJDA());
            bot.updateGame(event.getJDA());
            sendJoinMessage(event);
        }
        else if(preEvent instanceof GuildLeaveEvent)
        {
            GuildLeaveEvent event = (GuildLeaveEvent)preEvent;
            bot.botlog.logLeave(event);
            bot.sendStats(event.getJDA());
            bot.updateGame(event.getJDA());
        }
        else if(preEvent instanceof GuildMessageReceivedEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent)preEvent;
            AfkManager.checkAfk(event);
            AfkManager.checkPings(event);
            serverlog.onGuildMessageReceived(event);
        }
        else if(preEvent instanceof MessageReceivedEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            MessageReceivedEvent event = (MessageReceivedEvent)preEvent;
            ImportedTagHandler.runTag(bot, event);
        }
        else if(preEvent instanceof GuildMemberRoleAddEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMemberRoleAddEvent event = (GuildMemberRoleAddEvent)preEvent;
            MutedRoleHandler.checkRoleAdd(event);
        }
        else if(preEvent instanceof GuildMemberRoleRemoveEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMemberRoleRemoveEvent event = (GuildMemberRoleRemoveEvent)preEvent;
            MutedRoleHandler.checkRoleRemove(event);
        }
        else if(preEvent instanceof GuildMemberJoinEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMemberJoinEvent event = (GuildMemberJoinEvent)preEvent;
            MutedRoleHandler.checkJoin(event);
            serverlog.onGuildMemberJoin(event);
            welcomeDm(event);
        }
        else if(preEvent instanceof GuildMemberLeaveEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMemberLeaveEvent event = (GuildMemberLeaveEvent)preEvent;
            modlog.onGuildMemberLeave(event);
            serverlog.onGuildMemberLeave(event);
        }
        else if(preEvent instanceof GuildMessageReactionAddEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMessageReactionAddEvent event = (GuildMessageReactionAddEvent)preEvent;
            StarboardHandler.checkAddReaction(event);
        }
        else if(preEvent instanceof GuildMessageReactionRemoveEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMessageReactionRemoveEvent event = (GuildMessageReactionRemoveEvent)preEvent;
            StarboardHandler.checkRemoveReaction(event);
        }
        else if(preEvent instanceof GuildMessageReactionRemoveAllEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMessageReactionRemoveAllEvent event = (GuildMessageReactionRemoveAllEvent)preEvent;
            StarboardHandler.checkRemoveAllReactions(event);
        }
        else if(preEvent instanceof GuildMessageDeleteEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMessageDeleteEvent event = (GuildMessageDeleteEvent)preEvent;
            StarboardHandler.checkDeleteMessage(event);
            serverlog.onGuildMessageDelete(event);
        }
        else if(preEvent instanceof MessageBulkDeleteEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            MessageBulkDeleteEvent event = (MessageBulkDeleteEvent)preEvent;
            serverlog.onMessageBulkDeleteEvent(event);
        }
        else if(preEvent instanceof GuildMessageUpdateEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildMessageUpdateEvent event = (GuildMessageUpdateEvent)preEvent;
            serverlog.onGuildMessageUpdate(event);
        }
        else if(preEvent instanceof UserUpdateAvatarEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            UserUpdateAvatarEvent event = (UserUpdateAvatarEvent)preEvent;
            serverlog.onUserUpdateAvatar(event);
        }
        else if(preEvent instanceof GuildVoiceJoinEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildVoiceJoinEvent event = (GuildVoiceJoinEvent)preEvent;
            serverlog.onGuildVoiceJoin(event);
            checkVoiceJoin(event);
        }
        else if(preEvent instanceof GuildVoiceMoveEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildVoiceMoveEvent event = (GuildVoiceMoveEvent)preEvent;
            serverlog.onGuildVoiceMove(event);
        }
        else if(preEvent instanceof GuildVoiceLeaveEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildVoiceLeaveEvent event = (GuildVoiceLeaveEvent)preEvent;
            serverlog.onGuildVoiceLeave(event);
            checkVoiceLeave(event);
        }
        else if(preEvent instanceof GuildBanEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildBanEvent event = (GuildBanEvent)preEvent;
            modlog.onGuildBan(event);
        }
        else if(preEvent instanceof GuildUnbanEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            GuildUnbanEvent event = (GuildUnbanEvent)preEvent;
            modlog.onGuildUnban(event);
        }
        else if(preEvent instanceof TextChannelDeleteEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            TextChannelDeleteEvent event = (TextChannelDeleteEvent)preEvent;
            TextChannel tc = event.getChannel();
            bot.rsdm.getRoomsForGuild(event.getGuild().getIdLong()).stream().filter(r -> r.getTextChannelId()==tc.getIdLong()).forEach(r -> {
                if(r.isCombo())
                {
                    VoiceChannel vc = event.getGuild().getVoiceChannelById(r.getVoiceChannelId());
                    if(!(vc==null))
                        vc.delete().reason("[Text Channel Deleted (Combo)]").queue(null, e -> {});
                    bot.rsdm.deleteComboRoom(tc.getIdLong(), r.getVoiceChannelId());
                }
                else
                    bot.rsdm.deleteTextRoom(tc.getIdLong());
            });
        }
        else if(preEvent instanceof VoiceChannelDeleteEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            VoiceChannelDeleteEvent event = (VoiceChannelDeleteEvent)preEvent;
            VoiceChannel vc = event.getChannel();
            bot.rsdm.getRoomsForGuild(event.getGuild().getIdLong()).stream().filter(r -> r.getVoiceChannelId()==vc.getIdLong()).forEach(r -> {
                if(r.isCombo())
                {
                    TextChannel tc = event.getGuild().getTextChannelById(r.getTextChannelId());
                    if(!(tc==null))
                        tc.delete().reason("[Voice Channel Deleted (Combo)]").queue(null, e -> {});
                    bot.rsdm.deleteComboRoom(r.getTextChannelId(), vc.getIdLong());
                }
                else
                    bot.rsdm.deleteVoiceRoom(vc.getIdLong());
            });
        }
        else if(preEvent instanceof RoleDeleteEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            RoleDeleteEvent event = (RoleDeleteEvent)preEvent;
            Guild guild = event.getGuild();
            GuildSettings gs = bot.endless.getGuildSettings(guild);
            Role role = event.getRole();

            if(gs.getColorMeRoles().contains(role))
                bot.gsdm.removeColormeRole(guild, role);
            if(gs.getRoleMeRoles().contains(role))
                bot.gsdm.removeRolemeRole(guild, role);
            if(!(bot.endless.getIgnore(guild, role.getIdLong())==null))
                bot.gsdm.removeIgnore(guild, role.getIdLong());
        }
        else if(preEvent instanceof EmoteRemovedEvent)
        {
            if(bot.maintenance || !(bot.initialized))
                return;
            EmoteRemovedEvent event = (EmoteRemovedEvent)preEvent;
            Emote emote = event.getEmote();
            Guild guild = event.getGuild();
            GuildSettings gs = bot.endless.getGuildSettings(guild);

            if(gs.getStarboardEmote().equals(emote.getId()))
                bot.gsdm.setStarboardEmote(guild, null);
        }
    }

    @Override
    public void onCommand(CommandEvent event, Command command)
    {
        User author = event.getAuthor();
        Guild guild = event.getGuild();

        // If is the help command is null
        if(command==null)
            return;
        if(event.isOwner())
            return;

        if(guild==null)
        {
            String toSend = FormatUtil.sanitize("`"+LogUtils.getTimeAndDate()+"` :keyboard: **"+author.getName()+"#"+author.getDiscriminator()+"** " +
                    "(ID: "+author.getId()+") used the command `"+command.getName()+"` (`"+event.getMessage().getContentStripped().trim()+
                    "`) in a **Direct message** (ID: "+event.getChannel().getId()+")");
            bot.cmdWebhook.send(toSend);
        }
        else
        {
            String toSend = FormatUtil.sanitize("`"+LogUtils.getTimeAndDate()+"` :keyboard: **"+author.getName()+"#"+author.getDiscriminator()+"** " +
                    "(ID: "+author.getId()+") used the command `"+command.getName()+"` (`"+event.getMessage().getContentStripped().trim()+
                    "`) in **"+guild.getName()+"** (ID: "+guild.getId()+")");
            bot.cmdWebhook.send(toSend);
        }
    }

    @Override
    public void onCommandException(CommandEvent event, Command command, Throwable throwable)
    {
        event.replyError("An error occurred while executing this command. Please join the support server by doing `e!help support`.");

        if(event.getGuild()==null)
        {
            Endless.LOG.error(String.format("Command Error: %s | Message ID: %s | Executed in: Direct Message %s",
                    command.getName(), event.getMessage().getIdLong(), event.getPrivateChannel().getIdLong()), throwable);
        }
        else
        {
            Endless.LOG.error(String.format("Command Error: %s | Message ID: %s | Executed in: Text Channel %s",
                    command.getName(), event.getMessage().getIdLong(), event.getTextChannel().getIdLong()), throwable);
        }
    }

    // I didnt found a better place for those methods
    private void sendJoinMessage(GuildJoinEvent event)
    {
        Guild guild = event.getGuild();
        SelfUser selfUser = event.getJDA().getSelfUser();
        TextChannel defaultTc = FinderUtil.getDefaultChannel(guild);
        if(defaultTc==null)
            return;

        String toSend = "Hi! Thanks for adding **"+selfUser.getName()+"** to your guild!\n" +
                "If you need help with the bot please join the support guild `e!help support`\n" +
                "Please report all the bugs you find. If you have a feature request is welcome as well :)\n\n" +
                Const.ENDLESS+" Join the support server: **<"+Const.INVITE+">**\n" +
                Const.GITHUB+" Endless GitHub: **<https://github.com/EndlessBot/Endless>**\n" +
                Const.LINE_START+" Contribute to Endless development **<https://paypal.me/artuto>**";

        Sender.sendMessage(defaultTc, toSend);
    }

    private void welcomeDm(GuildMemberJoinEvent event)
    {
        Guild guild = event.getGuild();
        GuildSettings gs = bot.endless.getGuildSettings(guild);
        String welcomeDM = gs.getWelcomeDM();
        User user = event.getUser();

        if(welcomeDM==null || user.isBot())
            return;

        parser.clear().put("user", user).put("guild", guild).put("channel", FinderUtil.getDefaultChannel(guild));
        user.openPrivateChannel().queue(c -> c.sendMessage(FormatUtil.sanitize(parser.parse(welcomeDM))).queue((s) -> parser.clear(), (e) -> parser.clear()));
    }

    private void checkVoiceLeave(GuildVoiceLeaveEvent event)
    {
        List<Member> actualListeners = new ArrayList<>();
        User user = event.getMember().getUser();
        VoiceChannel vc = event.getChannelLeft();

        if(user.getIdLong()==event.getJDA().getSelfUser().getIdLong())
        {
            bot.musicTasks.cancelLeave(vc);
            return;
        }

        vc.getMembers().stream().filter(m -> !(m.getVoiceState().isDeafened()) && !(m.getUser().isBot())).forEach(actualListeners::add);
        if(actualListeners.isEmpty())
            bot.musicTasks.scheduleLeave(vc, event.getGuild());
    }

    private void checkVoiceJoin(GuildVoiceJoinEvent event)
    {
        Member member = event.getMember();
        User user = member.getUser();
        VoiceChannel vc = event.getChannelJoined();

        if(user.getIdLong()==event.getJDA().getSelfUser().getIdLong())
            return;

        if(!(member.getVoiceState().isDeafened()) && !(user.isBot()))
            bot.musicTasks.cancelLeave(vc);
    }
}
