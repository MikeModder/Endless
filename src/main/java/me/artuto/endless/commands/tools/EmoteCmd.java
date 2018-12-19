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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.vdurmont.emoji.EmojiParser;
import me.artuto.endless.Const;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.utils.ArgsUtils;
import me.artuto.endless.utils.FormatUtil;
import me.artuto.endless.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Artuto
 */

public class EmoteCmd extends EndlessCommand
{
    public EmoteCmd()
    {
        this.name = "emote";
        this.aliases = new String[]{"emoji", "charinfo"};
        this.children = new Command[]{new CreateCmd(), new StealCmd(), new RemoveCmd()};
        this.help = "Get the info of a specified emote, emoji or character.";
        this.arguments = "<emote>";
        this.category = Categories.TOOLS;
        this.botPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
    }

    @Override
    protected void executeCommand(CommandEvent event)
    {
        StringBuilder sb = new StringBuilder();
        List<String> emojis = EmojiParser.extractEmojis(event.getArgs());

        if(event.getMessage().getEmotes().isEmpty() && !(emojis.isEmpty()))
        {
            String args = event.getArgs();

            sb.append("Emoji/Character info:\n");
            args.codePoints().forEachOrdered(code -> {
                char[] chars = Character.toChars(code);
                String hex = Integer.toHexString(code).toUpperCase();
                while(hex.length()<4)
                    hex = "0"+hex;
                sb.append("\n`\\u").append(hex).append("`   ");
                if(chars.length>1)
                {
                    String hex0 = Integer.toHexString(chars[0]).toUpperCase();
                    String hex1 = Integer.toHexString(chars[1]).toUpperCase();
                    while(hex0.length()<4)
                        hex0 = "0"+hex0;
                    while(hex1.length()<4)
                        hex1 = "0"+hex1;
                    sb.append("[`\\u").append(hex0).append("\\u").append(hex1).append("`]   ");
                }
                sb.append(String.valueOf(chars)).append("   _").append(Character.getName(code)).append("_");
            });
            event.replySuccess(sb.toString());
        }
        else if(!(event.getMessage().getEmotes().isEmpty()))
        {
            Emote emote = event.getMessage().getEmotes().get(0);
            createEmoteInfoEmbed(event, emote);
        }
        else
        {
            Emote emote = ArgsUtils.findEmote(event, event.getArgs());
            if(emote==null)
                return;
            createEmoteInfoEmbed(event, emote);
        }
    }

    private void createEmoteInfoEmbed(CommandEvent event, Emote emote)
    {
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mb = new MessageBuilder();
        StringBuilder sb = new StringBuilder();
        Guild guild = emote.getGuild();
        String url = "[Image]("+emote.getImageUrl()+")";

        sb.append(Const.LINE_START).append(" ID: **").append(emote.getId()).append("**\n");
        sb.append(Const.LINE_START).append(" Guild: ").append(emote.isFake()?"Unknown":"**"+guild.getName()+"** (ID: "+guild.getId()+")").append("\n");
        sb.append(Const.LINE_START).append(" URL: **").append(url).append("**\n");
        if(!(emote.isFake()))
            sb.append(Const.LINE_START).append(" Global: **").append(emote.isManaged()?"Yes":"No").append("**\n");
        builder.setImage(emote.getImageUrl()).setColor(event.getSelfMember()==null?null:event.getSelfMember().getColor());
        builder.setDescription(sb);
        event.reply(mb.setContent(FormatUtil.sanitize(String.format("%s Emote **%s**", event.getClient().getSuccess(), emote.getName()))).setEmbed(builder.build()).build());
    }

    private class CreateCmd extends EndlessCommand
    {
        CreateCmd()
        {
            this.name = "create";
            this.aliases = new String[]{"add"};
            this.help = "Uploads the specified URL and creates an emote.";
            this.arguments = "<emote> <name>";
            this.botPerms = new Permission[]{Permission.MANAGE_EMOTES};
            this.userPerms = new Permission[]{Permission.MANAGE_EMOTES};
            this.parent = EmoteCmd.this;
        }

        @Override
        protected void executeCommand(CommandEvent event)
        {
            Guild guild = event.getGuild();
            List<Emote> animatedEmotes = guild.getEmotes().stream().filter(Emote::isAnimated).collect(Collectors.toList());
            List<Emote> emotes = guild.getEmotes().stream().filter(e -> !(e.isAnimated())).collect(Collectors.toList());
            String[] args = splitArgs(event.getArgs());
            String name;

            if(!(args[0].endsWith(".png") || args[0].endsWith(".jpg") || args[0].endsWith(".jpeg") || args[0].endsWith(".gif")))
            {
                event.replyError("You didn't specified a emote!");
                return;
            }

            if(!(guild.getFeatures().contains("MORE_EMOJI")))
            {
                if(args[0].endsWith(".gif") && animatedEmotes.size()>=50)
                {
                    event.replyError("This guild has reached the limit for animated emotes (50)!");
                    return;
                }
                else if(!(args[0].endsWith("gif")) && emotes.size()>=50)
                {
                    event.replyError("This guild has reached the limit for non-animated emotes (50)!");
                    return;
                }
            }

            if(args[1].isEmpty())
            {
                event.replyError("You didn't specified a name!");
                return;
            }
            name = args[1];

            Icon icon;
            try
            {
                InputStream iSteam = MiscUtils.getInputStream(args[0]);
                if(iSteam==null)
                {
                    event.replyError("An error occurred while retrieving the emote image!");
                    return;
                }
                icon = Icon.from(iSteam);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return;
            }

            guild.getController().createEmote(name, icon).reason("["+event.getAuthor().getName()+"#"+
                    event.getAuthor().getDiscriminator()+"]").queue(em -> event.replySuccess(FormatUtil.sanitize("Successfully added emote *"+
                    em.getName()+"* ("+em.getAsMention()+")")), e -> { 
                    event.replyError("Something went wrong while creating a new emote!");
                    Endless.LOG.error("Could not create emote", e);
            });
        }
    }

    private class StealCmd extends EndlessCommand
    {
        StealCmd()
        {
            this.name = "steal";
            this.aliases = new String[]{"upload"};
            this.help = "Uploads the specified emote to the current server.";
            this.arguments = "<emote> [name]";
            this.botPerms = new Permission[]{Permission.MANAGE_EMOTES};
            this.userPerms = new Permission[]{Permission.MANAGE_EMOTES};
            this.parent = EmoteCmd.this;
        }

        @Override
        protected void executeCommand(CommandEvent event)
        {
            Guild guild = event.getGuild();
            List<Emote> animatedEmotes = guild.getEmotes().stream().filter(Emote::isAnimated).collect(Collectors.toList());
            List<Emote> emotes = guild.getEmotes().stream().filter(e -> !(e.isAnimated())).collect(Collectors.toList());
            String[] args = splitArgs(event.getArgs());
            String name;

            if(event.getMessage().getEmotes().isEmpty())
            {
                event.replyError("You didn't specified a emote!");
                return;
            }

            Emote emote = event.getMessage().getEmotes().get(0);
            if(!(guild.getFeatures().contains("MORE_EMOJI")))
            {
                if(emote.isAnimated() && animatedEmotes.size()>=50)
                {
                    event.replyError("This guild has reached the limit for animated emotes (50)!");
                    return;
                }
                else if(!(emote.isAnimated()) && emotes.size()>=50)
                {
                    event.replyError("This guild has reached the limit for non-animated emotes (50)!");
                    return;
                }
            }

            if(args[1].isEmpty())
                name = emote.getName();
            else
                name = args[1];

            Icon icon;
            try
            {
                InputStream iSteam = MiscUtils.getInputStream(emote.getImageUrl());
                if(iSteam==null)
                {
                    event.replyError("An error occurred while retrieving the emote image!");
                    return;
                }
                icon = Icon.from(iSteam);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return;
            }

            guild.getController().createEmote(name, icon).reason("["+event.getAuthor().getName()+"#"+
                    event.getAuthor().getDiscriminator()+"]").queue(em -> event.replySuccess(FormatUtil.sanitize("Successfully added emote *"+
                    em.getName()+"* ("+em.getAsMention()+")")), e -> { 
                    event.replyError("Something went wrong while creating a new emote!");
                    Endless.LOG.error("Could not create emote", e);
            });
        }
    }

    private class RemoveCmd extends EndlessCommand
    {
        RemoveCmd()
        {
            this.name = "remove";
            this.aliases = new String[]{"delete"};
            this.help = "Deletes the specified emote.";
            this.arguments = "<emote>";
            this.botPerms = new Permission[]{Permission.MANAGE_EMOTES};
            this.userPerms = new Permission[]{Permission.MANAGE_EMOTES};
            this.parent = EmoteCmd.this;
        }

        @Override
        protected void executeCommand(CommandEvent event)
        {
            Emote emote;

            if(event.getMessage().getEmotes().isEmpty())
            {
                event.replyError("You didn't specified a emote to delete!");
                return;
            }
            emote = event.getMessage().getEmotes().get(0);

            emote.delete().reason("["+event.getAuthor().getName()+"#"+event.getAuthor().getDiscriminator()+"]")
                    .queue(em -> event.replySuccess(FormatUtil.sanitize("Successfully deleted emote *"+emote.getName()+"*")), e -> { 
                    event.replyError("Something went wrong while deleting the emote!");
                    Endless.LOG.error("Could not delete emote", e);
            });
        }
    }

    private String[] splitArgs(String preArgs)
    {
        try
        {
            String[] args = preArgs.split(" ", 2);
            return new String[]{args[0], args[1]};
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            return new String[]{preArgs, ""};
        }
    }
}
