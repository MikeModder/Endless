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

package me.artuto.endless.libraries;

import com.jagrosh.jagtag.Environment;
import com.jagrosh.jagtag.Method;
import com.jagrosh.jagtag.ParseException;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author Artuto
 */

public class JagTag
{
    public static Collection<Method> getMethods()
    {
        return Arrays.asList(
                // User
                new Method("user", (env) -> {
                    User u = env.get("user");
                    return u.getName();
                }, (env, in) -> {
                    if(in[0].isEmpty())
                        return "";
                    Guild guild = env.get("guild");
                    if(!(guild==null))
                        return findMember(guild, in[0]).getUser().getName();
                    User user = env.get("user");
                    JDA jda = user.getJDA();
                    return findUser(jda, in[0], env).getName();
                }),
                new Method("nick", (env) -> {
                    User user = env.get("user");
                    TextChannel tc = env.get("channel");
                    Guild guild = tc.getGuild();
                    if(guild==null)
                        return user.getName();
                    Member member = guild.getMember(user);
                    if(member==null)
                        return user.getName();
                    return member.getEffectiveName();
                }, (env, in) -> {
                    if(in[0].isEmpty())
                        return "";
                    Guild guild = env.get("guild");
                    if(!(guild==null))
                        return findMember(guild, in[0]).getEffectiveName();
                    User user = env.get("user");
                    JDA jda = user.getJDA();
                    return findUser(jda, in[0], env).getName();
                }),
                new Method("discrim", (env) -> {
                    User user = env.get("user");
                    return user.getDiscriminator();
                }, (env, in) -> {
                    if(in[0].isEmpty())
                        return "";
                    Guild guild = env.get("guild");
                    if(!(guild==null))
                        return findMember(guild, in[0]).getUser().getDiscriminator();
                    User user = env.get("user");
                    JDA jda = user.getJDA();
                    return findUser(jda, in[0], env).getDiscriminator();
                }),
                new Method("userid", (env) -> {
                    User user = env.get("user");
                    return user.getId();
                }),
                new Method("atuser", (env) -> {
                    User user = env.get("user");
                    return user.getAsMention();
                }, (env, in) -> {
                    if(in[0].isEmpty())
                        return "";
                    Guild guild = env.get("guild");
                    if(!(guild==null))
                        return findMember(guild, in[0]).getUser().getAsMention();
                    User user = env.get("user");
                    JDA jda = user.getJDA();
                    return findUser(jda, in[0], env).getAsMention();
                }),
                new Method("avatar", (env) -> {
                    User user = env.get("user");
                    return user.getEffectiveAvatarUrl();
                }, (env, in) -> {
                    if(in[0].isEmpty())
                        return "";
                    Guild guild = env.get("guild");
                    if(!(guild==null))
                        return findMember(guild, in[0]).getUser().getEffectiveAvatarUrl();
                    User user = env.get("user");
                    JDA jda = user.getJDA();
                    return findUser(jda, in[0], env).getEffectiveAvatarUrl();
                }),
                // Guild
                new Method("server", (env) -> {
                    Guild guild = env.get("guild");
                    return guild==null?"Direct Message":guild.getName();
                }),
                new Method("serverid", (env) -> {
                    Guild guild = env.get("guild");
                    return guild==null?"0":guild.getId();
                }),
                new Method("servercount", (env) -> {
                    Guild guild = env.get("guild");
                    return guild==null?"2":String.valueOf(guild.getMemberCache().size());
                }),
                // Channel
                new Method("channel", (env) -> {
                    MessageChannel channel = env.get("channel");
                    return channel.getName();
                }),
                new Method("channelid", (env) -> {
                    MessageChannel channel = env.get("channel");
                    return channel.getId();
                }),
                // Random
                new Method("randuser", (env) -> {
                    Guild guild = env.get("guild");
                    User user = env.get("user");
                    if(guild==null)
                        return user.getJDA().getUsers().get((int)(user.getJDA().getUserCache().size()*Math.random())).getName();
                    return guild.getMembers().get((int)(guild.getMemberCache().size()*Math.random())).getUser().getName();
                }),
                new Method("randonline", (env) -> {
                    Guild guild = env.get("guild");
                    User user = env.get("user");
                    if(guild==null)
                        return user.getName();
                    List<Member> online = guild.getMembers().stream().filter(m -> m.getOnlineStatus()==OnlineStatus.ONLINE).collect(Collectors.toList());
                    if(online.isEmpty())
                        return user.getName();
                    return online.get((int)(online.size()*Math.random())).getUser().getName();
                }),
                new Method("randchannel", (env) -> {
                    Guild guild = env.get("guild");
                    MessageChannel channel = env.get("channel");
                    if(guild==null)
                        return channel.getName();
                    return guild.getTextChannels().get((int)(guild.getTextChannelCache().size()*Math.random())).getName();
                }),
                // NSFW
                new Method("nsfw", (env) -> ""),
                // Embed. Taken from https://github.com/jagrosh/Selfbot/blob/master/src/jselfbot/entities/JagTagMethods.java#L24-L92
                new Method("title", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    String[] parts = in[0].split("\\|",2);
                    String title = parts[0];
                    String url = parts.length>1?parts[1]:null;
                    checkTitle(title, env);
                    checkUrl(url, false, env);
                    eb.setTitle(title, url);
                    return "";
                }),
                new Method("author", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    String[] parts = in[0].split("\\|",3);
                    String author = parts[0];
                    String url = parts.length>2?parts[2]:null;
                    String image = parts.length>1?parts[1]:null;
                    checkText(author, env);
                    checkUrl(url, false, env);
                    checkUrl(image, true, env);
                    eb.setAuthor(author, url, image);
                    return "";
                }),
                new Method("thumbnail", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    checkUrl(in[0], true, env);
                    eb.setThumbnail(in[0]);
                    return "";
                }),
                new Method("field", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    Guild guild = env.get("guild");
                    String[] parts = in[0].split("\\|",3);
                    if(parts.length<2)
                        throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.field"));
                    String title = parts[0];
                    String value = parts[1];
                    boolean inline = parts.length>2?parts[2].equalsIgnoreCase("true"):true;
                    checkTitle(title, env);
                    checkField(value, env);
                    eb.addField(title, value, inline);
                    return "";
                }),
                new Method("image", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    checkUrl(in[0], true, env);
                    eb.setImage(in[0]);
                    return "";
                }),
                new Method("color", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    Guild guild = env.get("guild");
                    switch(in[0].toLowerCase()) {
                        //standard
                        case "red": eb.setColor(Color.RED); break;
                        case "orange": eb.setColor(Color.ORANGE); break;
                        case "yellow": eb.setColor(Color.YELLOW); break;
                        case "green": eb.setColor(Color.GREEN); break;
                        case "cyan": eb.setColor(Color.CYAN); break;
                        case "blue": eb.setColor(Color.BLUE); break;
                        case "magenta": eb.setColor(Color.MAGENTA); break;
                        case "pink": eb.setColor(Color.PINK); break;
                        case "black": eb.setColor(Color.decode("#000001")); break;
                        case "dark_gray":
                        case "dark_grey": eb.setColor(Color.DARK_GRAY); break;
                        case "gray":
                        case "grey": eb.setColor(Color.GRAY); break;
                        case "light_gray":
                        case "light_grey": eb.setColor(Color.LIGHT_GRAY); break;
                        case "white": eb.setColor(Color.WHITE); break;
                        //discord
                        case "blurple": eb.setColor(Color.decode("#7289DA")); break;
                        case "greyple": eb.setColor(Color.decode("#99AAB5")); break;
                        case "darktheme": eb.setColor(Color.decode("#2C2F33")); break;
                        default:
                            Color color;
                            try {color = Color.decode(in[0]);}
                            catch(NumberFormatException ignored) {throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.color"));}
                            eb.setColor(color);
                    }
                    return "";
                }),
                new Method("description", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    checkText(in[0], env);
                    eb.setDescription(in[0]);
                    return "";
                }),
                new Method("footer", (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    String[] parts = in[0].split("\\|",2);
                    String title = parts[0];
                    String icon = parts.length>1?parts[1]:null;
                    checkTitle(title, env);
                    checkUrl(icon, true, env);
                    eb.setFooter(title, icon);
                    return "";
                }),
                new Method("timestamp", (env) -> {
                    EmbedBuilder eb = env.get("builder");
                    eb.setTimestamp(OffsetDateTime.now());
                    return "";
                    }, (env, in) -> {
                    EmbedBuilder eb = env.get("builder");
                    Guild guild = env.get("guild");
                    OffsetDateTime time;
                    try {time = OffsetDateTime.parse(in[0]);}
                    catch(DateTimeParseException ignored) {throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.time"));}
                    eb.setTimestamp(time);
                    return "";
                }),
                // Emote
                new Method("emote", (env) -> "", (env, in) -> {
                    if(in[0].isEmpty())
                        return "";
                    String args = in[0].toLowerCase();
                    CommandClient client = Bot.getInstance().client;
                    switch(args)
                    {
                        case "dot":
                            return Const.LINE_START;
                        case "error":
                            return client.getError();
                        case "linestart":
                            return Const.LINE_START;
                        case "success":
                            return client.getSuccess();
                        case "warning":
                            return client.getWarning();
                        case "warn":
                            return client.getWarning();
                        default:
                            User u = env.get("user");
                            return findEmote(u.getJDA(), args, env).getAsMention();
                    }
                }),
                // Repeat
                new Method("repeat", (env) -> "", (env, in) -> {
                    if(in[0].isEmpty())
                        return "";
                    String toRepeat = in[0];
                    int times;
                    if(in.length==2)
                    {
                        try {times = Integer.parseInt(in[1]);}
                        catch(NumberFormatException ignored) {times=1;}
                    }
                    else
                        times = 1;

                    return StringUtils.repeat(toRepeat, times);
                }));
    }

    private static Emote findEmote(JDA jda, String query, Environment env) throws ParseException
    {
        CommandClient client = Bot.getInstance().client;
        Guild guild = env.get("guild");
        List<Emote> list = FinderUtil.findEmotes(query, jda);

        if(list.isEmpty())
            throw new ParseException(String.format("%s %s", client.getWarning(), Bot.getInstance().localize(guild, "core.finder.empty.emote", query)));
        else
            return list.get(0);
    }

    private static Member findMember(Guild guild, String query) throws ParseException
    {
        CommandClient client = Bot.getInstance().client;
        List<Member> list = FinderUtil.findMembers(query, guild);

        if(list.isEmpty())
            throw new ParseException(String.format("%s %s", client.getWarning(), Bot.getInstance().localize(guild, "core.finder.empty.member", query)));
        else
            return list.get(0);
    }

    private static User findUser(JDA jda, String query, Environment env) throws ParseException
    {
        CommandClient client = Bot.getInstance().client;
        Guild guild = env.get("guild");
        List<User> list = FinderUtil.findUsers(query, jda);

        if(list.isEmpty())
            throw new ParseException(String.format("%s %s", client.getWarning(), Bot.getInstance().localize(guild, "core.finder.empty.user", query)));
        else
            return list.get(0);
    }

    private static void checkUrl(@Nullable String url, boolean image, Environment env) throws ParseException
    {
        Guild guild = env.get("guild");
        if(url==null)
            return;
        if(url.length()>MessageEmbed.URL_MAX_LENGTH)
            throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.tooLong."+(image?"imageUrl":"url")));
        Matcher m = EmbedBuilder.URL_PATTERN.matcher(url);
        if(!(m.matches()))
            throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.invalid."+(image?"imageUrl":"url")));
    }

    private static void checkText(String text, Environment env) throws ParseException
    {
        Guild guild = env.get("guild");
        if(text.length()>MessageEmbed.TEXT_MAX_LENGTH)
            throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.tooLong.description"));
    }

    private static void checkField(String text, Environment env) throws ParseException
    {
        Guild guild = env.get("guild");
        if(text.length()>MessageEmbed.VALUE_MAX_LENGTH)
            throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.tooLong.field"));
    }

    private static void checkTitle(String text, Environment env) throws ParseException
    {
        Guild guild = env.get("guild");
        if(text.length()>MessageEmbed.TITLE_MAX_LENGTH)
            throw new ParseException(Bot.getInstance().localize(guild, "core.jagtag.embed.tooLong.title"));
    }
}
