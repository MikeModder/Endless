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

import ch.qos.logback.classic.Logger;
import com.jagrosh.jagtag.Parser;
import com.jagrosh.jagtag.ParserBuilder;
import com.jagrosh.jagtag.libraries.*;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import io.sentry.Sentry;
import me.artuto.endless.bootloader.StartupChecker;
import me.artuto.endless.bootloader.ThreadLoader;
import me.artuto.endless.commands.bot.*;
import me.artuto.endless.commands.botadm.*;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.commands.cmddata.CommandHelper;
import me.artuto.endless.commands.fun.*;
import me.artuto.endless.commands.moderation.*;
import me.artuto.endless.commands.music.*;
import me.artuto.endless.commands.serverconfig.*;
import me.artuto.endless.commands.tools.*;
import me.artuto.endless.commands.utils.*;
import me.artuto.endless.core.EndlessCore;
import me.artuto.endless.core.EndlessCoreBuilder;
import me.artuto.endless.core.entities.PunishmentType;
import me.artuto.endless.core.exceptions.ConfigException;
import me.artuto.endless.handlers.BlacklistHandler;
import me.artuto.endless.handlers.IgnoreHandler;
import me.artuto.endless.handlers.SpecialCaseHandler;
import me.artuto.endless.libraries.JagTag;
import me.artuto.endless.loader.Config;
import me.artuto.endless.logging.BotLogging;
import me.artuto.endless.logging.ModLogging;
import me.artuto.endless.logging.ServerLogging;
import me.artuto.endless.music.MusicTasks;
import me.artuto.endless.storage.data.Database;
import me.artuto.endless.storage.data.managers.*;
import me.artuto.endless.utils.GuildUtils;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.Presence;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.utils.SessionControllerAdapter;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Artuto
 */

public class Bot extends ListenerAdapter
{
    public boolean dataEnabled;
    public boolean maintenance;
    public boolean initialized = false;
    public EndlessCore endless;
    public EndlessCoreBuilder endlessBuilder;

    // Audio
    public AudioPlayerManager audioManager;
    public MusicTasks musicTasks = new MusicTasks();

    // Config
    public Config config;

    // Data Managers
    public BlacklistDataManager bdm;
    public Database db;
    public DonatorsDataManager ddm;
    public GuildSettingsDataManager gsdm;
    public PollsDataManager pldm;
    public PunishmentsDataManager pdm;
    public ProfileDataManager prdm;
    public RemindersDataManager rdm;
    public RoomsDataManager rsdm;
    public StarboardDataManager sdm;
    public TagDataManager tdm;

    // JDA
    public CommandClient client;
    public ShardManager shardManager;

    // EventWaiter
    public EventWaiter waiter;

    // Logging
    BotLogging botlog;
    ServerLogging serverlog;
    public ModLogging modlog;

    // Loggers
    private final Logger CONFIGLOG = (Logger)LoggerFactory.getLogger("Config Loader");

    // Other
    public static final Parser tagParser = new ParserBuilder().addMethods(JagTag.getMethods())
            .addMethods(Arguments.getMethods()).addMethods(Functional.getMethods()).addMethods(Miscellaneous.getMethods())
            .addMethods(Strings.getMethods()).addMethods(Time.getMethods()).addMethods(Variables.getMethods())
            .setMaxOutput(2000).setMaxIterations(1000).build();

    // Pools
    public ScheduledThreadPoolExecutor endlessPool;

    // Schedulers;
    public ScheduledExecutorService voiceLeaverScheduler;
    private ScheduledExecutorService muteScheduler;
    private ScheduledExecutorService optimizerScheduler;
    private ScheduledExecutorService pollScheduler;
    private ScheduledExecutorService reminderScheduler;

    // Threads
    public ScheduledExecutorService archiveThread;
    public ScheduledExecutorService clearThread;
    public ScheduledExecutorService starboardThread;

    // Webhooks
    WebhookClient cmdWebhook;
    public WebhookClient logWebhook;

    public static Bot getInstance()
    {
        return Endless.bot;
    }

    void boot(boolean dataEnabled, boolean maintenance) throws LoginException, SQLException
    {
        this.dataEnabled = dataEnabled;
        this.maintenance = maintenance;
        Endless.LOG.info("Starting Endless "+Const.VERSION+"...");
        if(!(dataEnabled))
            Endless.LOG.warn("WARNING - Starting on No-data Mode - WARNING");
        if(maintenance)
            Endless.LOG.warn("WARNING - Starting on Maintenance Mode - WARNING");

        CONFIGLOG.info("Loading config file...");

        try
        {
            config = new Config();
            StartupChecker.checkConfig(config);
            CONFIGLOG.info("Successfully loaded config file!");
        }
        catch(Exception e)
        {
            throw new ConfigException(e.getMessage());
        }

        if(config.isSentryEnabled() && !(config.getSentryDSN().isEmpty()))
            Sentry.init(config.getSentryDSN());

        if(dataEnabled)
        {
            db = new Database(this, config.getDatabaseUrl(), config.getDatabaseUsername(), config.getDatabasePassword());
            bdm = new BlacklistDataManager(this);
            ddm = new DonatorsDataManager(db);
            gsdm = new GuildSettingsDataManager(this);
            pldm = new PollsDataManager(this);
            pdm = new PunishmentsDataManager(this);
            prdm = new ProfileDataManager(this);
            rdm = new RemindersDataManager(this);
            rsdm = new RoomsDataManager(this);
            sdm = new StarboardDataManager(this);
            tdm = new TagDataManager(this);
        }
        BlacklistHandler bHandler = new BlacklistHandler(this);
        IgnoreHandler iHandler = new IgnoreHandler(this);
        SpecialCaseHandler sHandler = new SpecialCaseHandler();
        new Categories(maintenance, bHandler, iHandler, sHandler);
        new GuildUtils(this);

        botlog = new BotLogging(this);
        cmdWebhook = new WebhookClientBuilder(config.getCommandlogWebhook()).setExecutorService(ThreadLoader.createThread("Command Log")).build();
        logWebhook = new WebhookClientBuilder(config.getBotlogWebhook()).setExecutorService(ThreadLoader.createThread("Botlog")).build();
        modlog = new ModLogging(this);
        serverlog = new ServerLogging(this);

        if(dataEnabled)
        {
            muteScheduler = ThreadLoader.createThread("Mutes");
            pollScheduler = ThreadLoader.createThread("Polls");
            reminderScheduler = ThreadLoader.createThread("Reminders");
            starboardThread = ThreadLoader.createThread("Starboard");
        }
        archiveThread = ThreadLoader.createThread("Archive Command");
        clearThread = ThreadLoader.createThread("Clear Command");
        endlessPool = ThreadLoader.createThread("Endless");
        optimizerScheduler = ThreadLoader.createThread("Optimizer");
        voiceLeaverScheduler = ThreadLoader.createThread("Voice Leaver");

        waiter = new EventWaiter();
        Listener listener = new Listener(this);

        if(config.isAudioEnabled())
            musicTasks.setupSystem(this);

        CommandClientBuilder clientBuilder = new CommandClientBuilder();
        Long[] coOwners = config.getCoOwnerIds();
        String[] owners = new String[coOwners.length];

        for(int i = 0; i<owners.length; i++)
            owners[i] = String.valueOf(coOwners[i]);

        clientBuilder.setOwnerId(String.valueOf(config.getOwnerId()))
                .setServerInvite(Const.INVITE)
                .setEmojis(config.getDoneEmote(), config.getWarnEmote(), config.getErrorEmote())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setGame(Game.playing("[ENDLESS] Loading..."))
                .setPrefix(config.getPrefix())
                .setAlternativePrefix("@mention")
                .setScheduleExecutor(ThreadLoader.createThread("Commands"))
                .setListener(listener)
                .setLinkedCacheSize(6)
                .setHelpConsumer(CommandHelper::getHelp)
                .setCoOwnerIds(owners)
                .setShutdownAutomatically(false)
                .setGuildSettingsManager(new ClientGSDM(this))
                .addCommands(
                // Bot
                new AboutCmd(this), new DonateCmd(this), new InviteCmd(), new PingCmd(), new StatsCmd(),

                // Bot Administration
                new BashCmd(), new BlacklistGuildCmd(this), new BlacklistUserCmd(this),
                new BotCPanelCmd(), new EvalCmd(this), new RestartShardCmd(), new ShutdownCmd(this), new StatusCmd(),

                // Fun
                new CatCmd(this), new ChooseCmd(), new DogCmd(),
                new GiphyGifCmd(this), new ProfileCmd(this), new SayCmd(), new TagCmd(this),

                // Moderation
                new BanCmd(this), new ClearCmd(this), new KickCmd(this), new HackbanCmd(this),
                new MuteCmd(this), new ReasonCmd(this), new SoftbanCmd(this), new UnbanCmd(this),

                // Music
                new ForceSkipCmd(this), new NowPlayingCmd(), new PauseCmd(this), new PlayCmd(this), new QueueCmd(this),
                new RemoveCmd(this), new ResumeCmd(this), new RepeatCmd(this), new SkipCmd(this), new StopCmd(), new VolumeCmd(this),

                // Server Settings
                new IgnoreCmd(this), new LeaveMsgCmd(this), new PrefixCmd(this), new RoomCmd(this),
                new ServerSettingsCmd(this), new SetDJCmd(this), new SetMusicTcCmd(this), new SetMusicVcCmd(this), new SetupCmd(this),
                new StarboardCmd(this), new WelcomeDmCmd(this), new WelcomeMsgCmd(this),

                // Tools
                new AfkCmd(), new AnnouncementCmd(), new AvatarCmd(), new ChannelInfoCmd(), new EmoteCmd(), new GuildInfoCmd(),
                new LookupCmd(), new NickCmd(), new PollCmd(this), new QuoteCmd(), new RoleCmd(), new RPCCmd(), new UserInfoCmd(),

                // Utils
                new ArchiveCmd(this), new ColorMeCmd(this), new DiscriminatorCmd(this), new GoogleSearchCmd(this),
                new ReminderCmd(this), new RoleMeCmd(this), new TimeForCmd(this), new TranslateCmd(this),
                new WeatherCmd(this), new YouTubeCmd(this));

        client = clientBuilder.build();
        Endless.LOG.info("Starting JDA...");

        DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder()
                .setToken(config.getToken())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setGame(Game.playing("[ENDLESS] Loading..."))
                .setBulkDeleteSplittingEnabled(false)
                .setEnableShutdownHook(true)
                .setSessionController(new SessionControllerAdapter(){
                    @Override
                    protected void runWorker()
                    {
                        synchronized(lock)
                        {
                            if(workerHandle==null)
                            {
                                workerHandle = new SessionControllerAdapter.QueueWorker(20);
                                System.gc();
                                workerHandle.start();
                            }
                        }
                    }
                });

        if(maintenance)
            builder.addEventListeners(this, client);
        else
            builder.addEventListeners(this, client, listener, waiter);
        shardManager = builder.build();

        endlessBuilder = new EndlessCoreBuilder(this, client, shardManager);
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        endlessBuilder.addShard(event.getJDA());
    }

    @Override
    public void onStatusChange(StatusChangeEvent event)
    {
        if(event.getNewStatus()==JDA.Status.CONNECTED)
        {
            if(event.getJDA().asBot().getShardManager().getShards().stream().allMatch(shard -> shard.getStatus()==JDA.Status.CONNECTED)
                    && !(initialized))
            {
                this.endless = endlessBuilder.build();
                if(dataEnabled)
                {
                    muteScheduler.scheduleWithFixedDelay(() -> pdm.updateTempPunishments(PunishmentType.TEMPMUTE, shardManager),
                            0, 1, TimeUnit.SECONDS);
                    pollScheduler.scheduleWithFixedDelay(() -> pldm.updatePolls(shardManager), 0, 1, TimeUnit.SECONDS);
                    reminderScheduler.scheduleWithFixedDelay(() -> rdm.updateReminders(shardManager), 0, 1, TimeUnit.SECONDS);
                }
                endlessPool.schedule(() -> db.toDelete.forEach(g -> db.deleteSettings(g)), 24, TimeUnit.HOURS);
                optimizerScheduler.scheduleWithFixedDelay(System::gc, 5, 30, TimeUnit.MINUTES);
                sendStats(event.getJDA());
            }
        }
    }

    void sendStats(JDA jda)
    {
        JSONObject body;
        OkHttpClient client = ((JDAImpl)jda).getHttpClient();

        // Send to DiscordBots.org
        body = new JSONObject().put("server_count", shardManager.getGuildCache().size());
        if(!(jda.getShardInfo()==null))
        {
            body.put("shard_id", jda.getShardInfo().getShardId())
                    .put("shard_count", jda.getShardInfo().getShardTotal());
        }

        if(!(config.getDBotsListToken().isEmpty()))
        {
            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://discordbots.org/api/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", config.getDBotsListToken())
                    .header("Content-Type", "application/json");

            client.newCall(builder.build()).enqueue(new Callback()
            {
                @ParametersAreNonnullByDefault
                @Override
                public void onResponse(Call call, Response response)
                {
                    Endless.LOG.info("Successfully send information to discordbots.org");
                    response.close();
                }

                @ParametersAreNonnullByDefault
                @Override
                public void onFailure(Call call, IOException e)
                {
                    Endless.LOG.error("Failed to send information to discordbots.org ", e);
                }
            });
        }

        // Send to bots.discord.pw
        if(!(config.getDBotsToken().isEmpty()))
        {
            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://bots.discord.pw/api/bots/"+jda.getSelfUser().getId()+"/stats")
                    .header("Authorization", config.getDBotsToken())
                    .header("Content-Type", "application/json");

            client.newCall(builder.build()).enqueue(new Callback()
            {
                @ParametersAreNonnullByDefault
                @Override
                public void onResponse(Call call, Response response)
                {
                    Endless.LOG.info("Successfully send information to bots.discord.pw");
                    response.close();
                }

                @ParametersAreNonnullByDefault
                @Override
                public void onFailure(Call call, IOException e)
                {
                    Endless.LOG.error("Failed to send information to bots.discord.pw ", e);
                }
            });
        }
    }
    
    void updateGame(JDA shard)
    {
        JDA.ShardInfo shardInfo = shard.getShardInfo();
        Presence presence = shard.getPresence();

        if(!(maintenance))
            presence.setPresence(config.getStatus(), Game.playing("Type "+config.getPrefix()+"help | Version "
                    +Const.VERSION+" | On "+shard.getGuildCache().size()+" Guilds | "+shard.getUserCache().size()+
                    " Users | Shard "+(shardInfo.getShardId()+1)));
        else
            presence.setPresence(OnlineStatus.DO_NOT_DISTURB, Game.playing("Maintenance mode enabled | Shard "
                    +(shardInfo.getShardId()+1)));
    }
}
