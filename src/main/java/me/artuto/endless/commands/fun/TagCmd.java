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

package me.artuto.endless.commands.fun;

import com.jagrosh.jagtag.Parser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.artuto.endless.Bot;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.core.entities.LocalTag;
import me.artuto.endless.core.entities.Tag;
import me.artuto.endless.utils.ArgsUtils;
import me.artuto.endless.utils.FormatUtil;
import me.artuto.endless.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class TagCmd extends EndlessCommand
{
    private final Bot bot;
    private final Parser parser;

    public TagCmd(Bot bot)
    {
        this.bot = bot;
        this.parser = Bot.tagParser;
        this.name = "tag";
        this.aliases = new String[]{"t"};
        this.help = "Retrieves a tag with the specified name";
        this.arguments = "<name>";
        this.children = new Command[]{new CreateGlobalCmd(), new CreateLocalCmd(), new DeleteCmd(), new EditCmd(), new ExecCmd(),
                new ImportCmd(), new ListCmd(), new OverrideCmd(), new OwnerCmd(), new RawCmd(), new Raw2Cmd(), new UnImportCmd()};
        this.category = Categories.FUN;
        this.guildOnly = false;
        this.needsArgumentsMessage = "Specify a tag name!";
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(!(bot.dataEnabled))
        {
            event.replyError("core.data.disabled");
            return;
        }

        String[] args = ArgsUtils.split(2, event.getArgs());
        String tagName;
        String tagArgs;

        try
        {
            tagName = args[0].trim().toLowerCase();
            tagArgs = args[1];
        }
        catch(IndexOutOfBoundsException e)
        {
            tagName = event.getArgs().trim().toLowerCase();
            tagArgs = "";
        }

        Tag tag;
        if(event.isFromType(ChannelType.TEXT))
        {
            tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), tagName);
            if(tag==null)
            {
                tag = bot.endless.getGlobalTag(tagName);
                if(tag==null)
                {
                    event.replyError("command.tag.notFound", tagName);
                    return;
                }
            }
        }
        else
        {
            tag = bot.endless.getGlobalTag(tagName);
            if(tag==null)
            {
                event.replyError("command.tag.notFound", tagName);
                return;
            }
        }

        if(tag.isOverriden() && tag.getContent().isEmpty())
        {
            event.replyError("command.tag.overriden");
            return;
        }
        if(tag.isNSFW() && !(MiscUtils.isNSFWAllowed(event)))
        {
            event.replyError("misc.tag.nsfw");
            return;
        }

        EmbedBuilder tagEmbed = new EmbedBuilder();
        parser.clear().put("user", event.getAuthor()).put("guild", event.getGuild()).put("channel", event.getChannel())
                .put("args", tagArgs).put("builder", tagEmbed);
        String parsed = parser.parse(tag.getContent());
        if(!(tagEmbed.isEmpty()))
            event.reply(new MessageBuilder().setContent(parsed).setEmbed(tagEmbed.build()).build());
        else
            event.reply(false, parsed);
    }

    private class CreateGlobalCmd extends EndlessCommand
    {
        CreateGlobalCmd()
        {
            this.name = "createglobal";
            this.help = "Creates a new global tag";
            this.arguments = "<name> <content>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Message msg = event.getMessage();
            String[] args = ArgsUtils.split(2, event.getArgs());
            String name = args[0].trim().toLowerCase();
            String content = args[1].trim();

            if(content.isEmpty() && msg.getAttachments().isEmpty())
            {
                event.replyWarning("command.tag.create.noContent");
                return;
            }
            else if(!(msg.getAttachments().isEmpty()))
            {
                for(Message.Attachment att : msg.getAttachments())
                    content += "\n"+att.getUrl();
            }

            Tag tag = bot.endless.getGlobalTag(name);

            if(tag==null)
            {
                bot.tdm.createGlobalTag(event.getAuthor().getIdLong(), content, name);
                event.replySuccess("command.tag.create.createdGlobal", name);
            }
            else
                event.replyError("command.tag.create.alreadyExistant");
        }
    }

    private class CreateLocalCmd extends EndlessCommand
    {
        CreateLocalCmd()
        {
            this.name = "createlocal";
            this.aliases = new String[]{"create"};
            this.help = "Creates a new local tag";
            this.arguments = "<name> <content>";
            this.category = Categories.FUN;
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Message msg = event.getMessage();
            String[] args = ArgsUtils.split(2, event.getArgs());
            String name = args[0].trim().toLowerCase();
            String content = args[1].trim();

            if(content.isEmpty() && msg.getAttachments().isEmpty())
            {
                event.replyWarning("command.tag.create.noContent");
                return;
            }
            else if(!(msg.getAttachments().isEmpty()))
            {
                for(Message.Attachment att : msg.getAttachments())
                    content += "\n"+att.getUrl();
            }

            Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), name);

            if(tag==null)
            {
                bot.tdm.createLocalTag(event.getGuild().getIdLong(), event.getAuthor().getIdLong(), content, name);
                event.replySuccess("command.tag.create.createdLocal", name, event.getGuild().getName());
            }
            else
                event.replyError("command.tag.create.alreadyExistant");
        }
    }

    private class DeleteCmd extends EndlessCommand
    {
        DeleteCmd()
        {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "Removes a existant tag";
            this.arguments = "<name>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.needsArgumentsMessage = "Specify a tag name!";
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            String name = event.getArgs().trim().toLowerCase();
            if(event.isFromType(ChannelType.TEXT))
            {
                Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), name);

                if(tag == null)
                {
                    tag = bot.endless.getGlobalTag(name);
                    if(tag==null)
                    {
                        event.replyError("command.tag.notFound", name);
                        return;
                    }
                }

                if(tag.getOwnerId()==event.getAuthor().getIdLong() || event.isOwner())
                {
                    if(tag.isGlobal())
                        bot.tdm.deleteGlobalTag(event.getArgs().trim().toLowerCase());
                    else
                        bot.tdm.deleteLocalTag(event.getGuild().getIdLong(), name);
                    event.replySuccess("command.tag.delete.deleted", name);
                }
                else
                    event.replyError("command.tag.notOwner", name);
            }
            else
            {
                Tag tag = bot.endless.getGlobalTag(name);
                if(tag==null)
                {
                    event.replyError("command.tag.notFound", name);
                    return;
                }

                if(tag.getOwnerId()==event.getAuthor().getIdLong() || event.isOwner())
                {
                    bot.tdm.deleteGlobalTag(name);
                    event.replySuccess("command.tag.delete.deleted", name);
                }
                else
                    event.replyError("command.tag.notOwner", name);
            }
        }
    }

    private class EditCmd extends EndlessCommand
    {
        EditCmd()
        {
            this.name = "edit";
            this.help = "Edits an existant tag";
            this.arguments = "<name> <new content>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Message msg = event.getMessage();
            String[] args = ArgsUtils.split(2, event.getArgs());
            String name = args[0].trim().toLowerCase();
            String content = args[1].trim();

            if(content.isEmpty() && msg.getAttachments().isEmpty())
            {
                event.replyWarning("command.tag.create.noContent");
                return;
            }
            else if(!(msg.getAttachments().isEmpty()))
            {
                for(Message.Attachment att : msg.getAttachments())
                    content += "\n"+att.getUrl();
            }

            if(event.isFromType(ChannelType.TEXT))
            {
                Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), name);
                if(tag==null)
                {
                    tag = bot.endless.getGlobalTag(name);
                    if(tag==null)
                    {
                        event.replyError("command.tag.notFound", name);
                        return;
                    }
                }

                if(tag.getOwnerId()==event.getAuthor().getIdLong() || event.isOwner())
                {
                    if(tag.isGlobal())
                        bot.tdm.updateGlobalTagContent(name, content);
                    else
                        bot.tdm.updateLocalTagContent(event.getGuild().getIdLong(), name, content);
                    event.replySuccess("command.tag.edit.edited", name);
                }
                else
                    event.replyError("command.tag.notOwner", name);
            }
            else
            {
                Tag tag = bot.endless.getGlobalTag(name);
                if(tag==null)
                {
                    event.replyError("command.tag.notFound", name);
                    return;
                }

                for(Message.Attachment att : event.getMessage().getAttachments())
                    content += "\n"+att.getUrl();

                if(tag.getOwnerId()==event.getAuthor().getIdLong() || event.isOwner())
                {
                    bot.tdm.updateGlobalTagContent(name, content);
                    event.replySuccess("command.tag.edit.edited", name);
                }
                else
                    event.replyError("command.tag.notOwner", name);
            }
        }
    }

    private class ExecCmd extends EndlessCommand
    {
        ExecCmd()
        {
            this.name = "exec";
            this.help = "Parses the specified content";
            this.aliases = new String[]{"execute"};
            this.arguments = "<content>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            EmbedBuilder tagEmbed = new EmbedBuilder();
            parser.clear().put("user", event.getAuthor()).put("guild", event.getGuild()).put("channel", event.getTextChannel())
                    .put("builder", tagEmbed);
            String parsed = parser.parse(event.getArgs());
            if(!(tagEmbed.isEmpty()))
                event.reply(new MessageBuilder().setContent(parsed).setEmbed(tagEmbed.build()).build());
            else
                event.reply(false, parsed);
        }
    }

    private class ImportCmd extends EndlessCommand
    {
        ImportCmd()
        {
            this.name = "import";
            this.help = "Imports a tag";
            this.arguments = "<name>";
            this.category = Categories.FUN;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Specify a tag name!";
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            String name = event.getArgs().trim().toLowerCase();
            if(event.isFromType(ChannelType.TEXT))
            {
                Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), name);

                if(tag==null)
                {
                    tag = bot.endless.getGlobalTag(name);
                    if(tag==null)
                    {
                        event.replyError("command.tag.notFound", name);
                        return;
                    }
                }

                if(bot.tdm.isImported(event.getGuild().getIdLong(), String.valueOf(tag.getId())))
                    event.replyError("command.tag.import.alreadyImported", name);
                else
                {
                    bot.tdm.importTag(event.getGuild().getIdLong(), tag);
                    event.replySuccess("command.tag.import.imported", name);
                }
            }
        }
    }

    private class ListCmd extends EndlessCommand
    {
        ListCmd()
        {
            this.name = "list";
            this.help = "List of tags of the specified user";
            this.arguments = "[user]";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.needsArguments = false;
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            StringBuilder sb = new StringBuilder();
            User user;

            if(event.isFromType(ChannelType.TEXT))
            {
                if(event.getArgs().isEmpty())
                    user = event.getAuthor();
                else
                {
                    user = ArgsUtils.findUser(false, event, event.getArgs());
                    if(user==null)
                        return;
                }

                List<Tag> globalTags = bot.endless.getGlobalTags().stream().filter(t -> t.getOwnerId()==user.getIdLong()).collect(Collectors.toList());
                List<LocalTag> localTags = bot.endless.getLocalTags().stream().filter(t -> t.getOwnerId()==user.getIdLong() &&
                        t.getGuildId()==event.getGuild().getIdLong()).collect(Collectors.toList());

                if(!(globalTags.isEmpty()))
                {
                    sb.append("\n").append(event.getClient().getSuccess()).append(" **").append(globalTags.size()).append("** ")
                            .append(event.localize("command.tag.list.global", user.getName())).append("\n");
                    globalTags.forEach(t -> sb.append(t.getName()).append(" "));
                }
                if(!(localTags.isEmpty()))
                {
                    sb.append("\n").append(event.getClient().getSuccess()).append(" **").append(localTags.size()).append("** ")
                            .append(event.localize("command.tag.list.local", event.getGuild().getName())).append("\n");
                    localTags.forEach(t -> sb.append(t.getName()).append(" "));
                }

                if(sb.toString().isEmpty())
                {
                    if(user==event.getAuthor())
                        event.replyWarning("command.tag.list.none");
                    else
                        event.replyWarning("command.tag.list.none.other", user.getName()+"#"+user.getDiscriminator());
                    return;
                }
                event.reply(false, sb.toString());
            }
            else
            {
                user = event.getAuthor();

                List<Tag> globalTags = bot.endless.getGlobalTags().stream().filter(t -> t.getOwnerId()==user.getIdLong()).collect(Collectors.toList());

                if(!(globalTags.isEmpty()))
                {
                    sb.append(event.getClient().getSuccess()).append(" **").append(globalTags.size()).append("** ")
                            .append(event.localize("command.tag.list.global", user.getName())).append("\n");
                    globalTags.forEach(t -> sb.append(t.getName()).append(" "));
                }

                if(sb.toString().isEmpty())
                {
                    event.replyWarning("command.tag.list.none");
                    return;
                }
                event.reply(false, sb.toString());
            }
        }
    }

    private class OverrideCmd extends EndlessCommand
    {
        OverrideCmd()
        {
            this.name = "override";
            this.help = "Overrides a tag in the current server.";
            this.arguments = "<tag> [new content]";
            this.category = Categories.FUN;
            this.needsArgumentsMessage = "Specify a tag name!";
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            Message msg = event.getMessage();
            String[] args = ArgsUtils.split(2, event.getArgs());
            String name = args[0].trim().toLowerCase();
            String content = args[1].trim();

            if(content.isEmpty() && msg.getAttachments().isEmpty())
            {
                event.replyWarning("command.tag.create.noContent");
                return;
            }
            else if(!(msg.getAttachments().isEmpty()))
            {
                for(Message.Attachment att : msg.getAttachments())
                    content += "\n"+att.getUrl();
            }

            Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), name);
            if(tag==null)
            {
                tag = bot.endless.getGlobalTag(name);
                if(tag==null)
                {
                    event.replyError("command.tag.notFound", name);
                    return;
                }
            }

            for(Message.Attachment att : event.getMessage().getAttachments())
                content += "\n"+att.getUrl();

            if(tag.isOverriden())
            {
                bot.tdm.deleteLocalTag(event.getGuild().getIdLong(), name);
                event.replySuccess("command.tag.delete.deleted", name);
            }
            else
            {
                if(tag.isGlobal())
                {
                    if(content.isEmpty())
                        bot.tdm.createLocalTag(true, event.getGuild().getIdLong(), event.getGuild().getIdLong(), "", name);
                    else
                        bot.tdm.createLocalTag(event.getGuild().getIdLong(), event.getGuild().getIdLong(), content, name);
                    event.replySuccess("command.tag.override.overriden", name);
                }
                else
                {
                    if(content.isEmpty())
                        bot.tdm.deleteLocalTag(event.getGuild().getIdLong(), name);
                    else
                    {
                        bot.tdm.deleteLocalTag(event.getGuild().getIdLong(), name);
                        bot.tdm.createLocalTag(event.getGuild().getIdLong(), event.getGuild().getIdLong(), content, name);
                    }
                    event.replySuccess("command.tag.override.overriden", name);
                }
            }
        }
    }

    private class OwnerCmd extends EndlessCommand
    {
        OwnerCmd()
        {
            this.name = "owner";
            this.help = "Gets the owner of a existant tag";
            this.arguments = "<name>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.needsArgumentsMessage = "Specify a tag name!";
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            String name = event.getArgs().trim().toLowerCase();
            if(event.isFromType(ChannelType.TEXT))
            {
                Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), name);
                if(tag==null)
                {
                    tag = bot.endless.getGlobalTag(name);
                    if(tag==null)
                    {
                        event.replyError("command.tag.notFound", name);
                        return;
                    }
                }
                if(tag.getOwnerId()==event.getGuild().getIdLong())
                    event.reply("command.tag.owner.server", name, event.getGuild().getName());
                else
                {
                    event.getJDA().asBot().getShardManager().retrieveUserById(tag.getOwnerId()).queue(user -> event.reply("command.tag.owner.user",
                            name, user.getName()+"#"+user.getDiscriminator(), user.getId()),
                            e -> event.reply("command.tag.owner.user.unknown", name));
                }
            }
            else
            {
                Tag tag = bot.endless.getGlobalTag(name);
                if(tag==null)
                {
                    event.replyError("command.tag.notFound", name);
                    return;
                }
                event.getJDA().asBot().getShardManager().retrieveUserById(tag.getOwnerId()).queue(user -> event.reply("command.tag.owner.user",
                        name, user.getName()+"#"+user.getDiscriminator(), user.getId()),
                        e -> event.reply("command.tag.owner.user.unknown", name));
            }
        }
    }

    private class RawCmd extends EndlessCommand
    {
        RawCmd()
        {
            this.name = "raw";
            this.help = "Shows the content of a tag without parsing the args";
            this.arguments = "<name>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.needsArgumentsMessage = "Specify a tag name!";
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            if(event.isFromType(ChannelType.TEXT))
            {
                Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), event.getArgs().trim().toLowerCase());

                if(tag==null)
                {
                    tag = bot.endless.getGlobalTag(event.getArgs().trim().toLowerCase());
                    if(tag==null)
                    {
                        event.replyError("command.tag.notFound", name);
                        return;
                    }
                }

                event.reply(false, "```"+tag.getContent()+"```");
            }
            else
            {
                Tag tag = bot.endless.getGlobalTag(event.getArgs().trim().toLowerCase());

                if(tag==null)
                {
                    event.replyError("command.tag.notFound", name);
                    return;
                }

                event.reply(false, "```"+tag.getContent()+"```");
            }
        }
    }

    private class Raw2Cmd extends EndlessCommand
    {
        Raw2Cmd()
        {
            this.name = "raw2";
            this.help = "Shows the content of a tag without parsing the args on a codeblock";
            this.arguments = "<name>";
            this.category = Categories.FUN;
            this.guildOnly = false;
            this.needsArgumentsMessage = "Specify a tag name!";
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            if(event.isFromType(ChannelType.TEXT))
            {
                Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), event.getArgs().trim().toLowerCase());

                if(tag==null)
                {
                    tag = bot.endless.getGlobalTag(event.getArgs().trim().toLowerCase());
                    if(tag==null)
                    {
                        event.replyError("command.tag.notFound", name);
                        return;
                    }
                }

                event.reply(false, "```"+tag.getContent()+"```");
            }
            else
            {
                Tag tag = bot.endless.getGlobalTag(event.getArgs().trim().toLowerCase());

                if(tag==null)
                {
                    event.replyError("command.tag.notFound", name);
                    return;
                }

                event.reply(false, "```"+tag.getContent()+"```");
            }
        }
    }

    private class UnImportCmd extends EndlessCommand
    {
        UnImportCmd()
        {
            this.name = "unimport";
            this.help = "Unimports a tag";
            this.arguments = "<name>";
            this.category = Categories.FUN;
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArgumentsMessage = "Specify a tag name!";
            this.parent = TagCmd.this;
        }

        @Override
        protected void executeCommand(EndlessCommandEvent event)
        {
            if(!(bot.dataEnabled))
            {
                event.replyError("core.data.disabled");
                return;
            }

            String name = event.getArgs().trim().toLowerCase();
            if(event.isFromType(ChannelType.TEXT))
            {
                Tag tag = bot.endless.getLocalTag(event.getGuild().getIdLong(), name);

                if(tag==null)
                {
                    tag = bot.endless.getGlobalTag(name);
                    if(tag==null)
                    {
                        event.replyError("command.tag.notFound", name);
                        return;
                    }
                }

                if(!(bot.tdm.isImported(event.getGuild().getIdLong(), String.valueOf(tag.getId()))))
                    event.replyError("command.tag.unimport.notImported", name);
                else
                {
                    bot.tdm.unImportTag(event.getGuild().getIdLong(), tag);
                    event.replySuccess("command.tag.unimport.unimported", name);
                }
            }
        }
    }
}
