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

import net.dv8tion.jda.core.JDAInfo;

/**
 * @author Artuto
 */

public class Const
{
    public static String VERSION = Const.class.getPackage().getImplementationVersion()==null?"DEV":Const.class.getPackage().getImplementationVersion();
    public static String INVITE = "https://github.com/MikeModder/Endless";
    public static String DEV = "`@Artuto`";
    public static String USER_AGENT = "Endless "+VERSION+" (Discord Bot/JDA "+JDAInfo.VERSION+")";
    public static String[] PROFILE_FIELDS = new String[]{"timezone", "twitter", "steam", "wii", "nnid",
            "xboxLive", "psn", "threeds", "skype", "youtube", "about", "twitch", "minecraft", "email",
            "lol", "wow", "battle", "splatoon", "mkwii", "reddit"};

    // IDs
    public static long ARTUTO_ID = 264499432538505217L;
    public static long ARTUTO_ALT_ID = 302534881370439681L;
    public static long MAIN_GUILD = 312776731704426496L;
    public static long GUILD_TESTING = 323954859713888256L;

    // Emotes
    public static String BAN = ":hammer:";
    public static String BOT = "<:bot:334859813915983872>";
    public static String BOTADM = "<:stafftools:334859814700187650>";
    public static String DND = "<:dnd:334859814029099008>";
    public static String ENDLESS = "<:endless:447899790727053324>";
    public static String GIPHY = "<:giphy:373675520099090436>";
    public static String GITHUB = "<:github:326118305062584321>";
    public static String GOOGLE = "<:google:447911997783277569>";
    public static String IDLE = "<:idle:334859813869584384>";
    public static String INFO = "<:endlessInfo:444203939303522305>";
    public static String INVISIBLE = "<:invisible:334859814410649601>";
    public static String LINE_START = ":white_small_square:";
    public static String LOADING = "<a:endlessLoading:444198122965434399>";
    public static String MUSIC = ":notes:";
    public static String NITRO = "<:nitro:334859814566101004>";
    public static String OFFLINE = "<:offline:334859814423232514>";
    public static String ONLINE = "<:online:334859814410911745>";
    public static String PARTNER = "<:partner:334859814561775616>";
    public static String PEOPLE = ":bust_in_silhouette:";
    public static String SERVER_SETTINGS = ":wrench:";
    public static String STREAMING = "<:streaming:334859814771359744>";
    public static String VERIFIED = "<:verified:465670198775644161>";
    public static String VOICE_JOIN = "<:voiceJoin:470420015418572801>";
    public static String VOICE_LEAVE = "<:voiceLeave:470420087896145941>";
    public static String VOICE_MOVE = "<:voiceMove:470420166791004172>";

    // Roles
    public static String ONLINE_ROLE = "<@&436352838822658049>";
    public static String RESUMED_ROLE = "<@&436352909685424134>";
    public static String RECONNECTED_ROLE = "<@&436352708304175106>";
    public static String OFFLINE_ROLE = "<@&436352915469369345>";

    // URLs
    public static String BING_MAPS = "https://dev.virtualearth.net/REST/v1/Locations/?query=%s&maxResults=1&key=%s";
}
