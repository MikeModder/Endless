package me.artuto.endless.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import me.artuto.endless.cmddata.Categories;
import me.artuto.endless.data.Blacklists;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BlacklistUsers extends Command
{
    public BlacklistUsers()
    {
        this.name = "ignoreuser";
        this.help = "Adds, removes or displays the list with ignored users.";
        this.category = Categories.BOTADM;
        this.children = new Command[]{new Add(), new Remove(), new Check(), new BlacklistList()};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.ownerCommand = false;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String prefix = event.getClient().getPrefix();

        if(event.getArgs().isEmpty())
        {
            event.replyWarning("Please choose a subcommand:\n" +
                    "- `"+prefix+"ignoreuser add`: Adds a user ID to the blacklisted users list.\n" +
                    "- `"+prefix+"ignoreuser remove`: Removes a user ID from the blacklisted users list.\n" +
                    "- `"+prefix+"ignoreuser list`: Displays blacklisted users.\n" +
                    "- `"+prefix+"ignoreuser check`: Checks if a user ID is blacklisted.");
        }
        else if(!(event.getArgs().contains("add")) || !(event.getArgs().contains("remove")) || !(event.getArgs().contains("list")))
        {
            event.replyWarning("Please choose a subcommand:\n" +
                    "- `"+prefix+"ignoreuser add`: Adds a user ID to the blacklisted users list.\n" +
                    "- `"+prefix+"ignoreuser remove`: Removes a user ID from the blacklisted users list.\n" +
                    "- `"+prefix+"ignoreuser list`: Displays blacklisted users.\n" +
                    "- `"+prefix+"ignoreuser check`: Checks if a user ID is blacklisted.");
        }
    }

    private class Add extends Command
    {
        Add()
        {
            this.name = "add";
            this.help = "Adds a user ID to the blacklisted users list.";
            this.arguments = "<user ID>";
            this.category = Categories.BOTADM;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
            this.ownerCommand = false;
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event)
        {
            User user;

            if(event.getArgs().isEmpty())
            {
                event.replyWarning("Please specify a user ID!");
                return;
            }

            try
            {
                user = event.getJDA().retrieveUserById(event.getArgs()).complete();
            }
            catch(Exception e)
            {
                event.replyError("That ID isn't valid!");
                return;
            }

            try
            {
                if(Blacklists.isUserListed(user.getId()))
                {
                    event.replyError("That user is already on the blacklist!");
                    return;
                }
            }
            catch(IOException e)
            {
                event.replyError("Something went wrong when writing to the blacklisted users file: \n```"+e+"```");
                return;
            }

            try
            {
                Blacklists.addUser(user.getId());
                event.replySuccess("Added **"+user.getName()+"#"+user.getDiscriminator()+"** to the blacklist.");
            }
            catch(IOException e)
            {
                event.replyError("Something went wrong when writing to the blacklisted users file: \n```"+e+"```");
            }
        }
    }

    private class Remove extends Command
    {
        Remove()
        {
            this.name = "remove";
            this.help = "Removes a user ID to the blacklisted users list.";
            this.arguments = "<user ID>";
            this.category = Categories.BOTADM;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
            this.ownerCommand = false;
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event)
        {
            User user;

            if(event.getArgs().isEmpty())
            {
                event.replyWarning("Please specify a user ID!");
                return;
            }

            try
            {
                user = event.getJDA().retrieveUserById(event.getArgs()).complete();
            }
            catch(Exception e)
            {
                event.replyError("That ID isn't valid!");
                return;
            }

            try
            {
                if(!(Blacklists.isUserListed(user.getId())))
                {
                    event.replyError("That ID isn't in the blacklist!");
                    return;
                }
            }
            catch(IOException e)
            {
                event.replyError("Something went wrong when reading the blacklisted users file: \n```"+e+"```");
                return;
            }

            try
            {
                Blacklists.removeUser(user.getId());
                event.replySuccess("Removed **"+user.getName()+"#"+user.getDiscriminator()+"** from the blacklist.");
            }
            catch(IOException e)
            {
                event.replyError("Something went wrong when writing to the blacklisted users file: \n```"+e+"```");
            }
        }
    }

    private class BlacklistList extends Command
    {
        BlacklistList()
        {
            this.name = "list";
            this.help = "Displays blacklisted users.";
            this.category = Categories.BOTADM;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
            this.ownerCommand = false;
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event)
        {
            List<String> list;
            EmbedBuilder builder = new EmbedBuilder();
            Color color;

            if(event.isFromType(ChannelType.PRIVATE))
            {
                color = Color.decode("#33ff00");
            }
            else
            {
                color = event.getGuild().getSelfMember().getColor();
            }

            try
            {
                list = Blacklists.getUsersList();

                if(list.isEmpty())
                {
                    event.reply("The list is empty!");
                }
                else
                {

                    builder.setDescription(list.stream().collect(Collectors.joining("\n")));
                    builder.setFooter(event.getSelfUser().getName()+"'s Blacklisted Users", event.getSelfUser().getEffectiveAvatarUrl());
                    builder.setColor(color);
                    event.reply(builder.build());
                }
            }
            catch(IOException e)
            {
                event.replyError("Something went wrong when reading the blacklisted users file: \n```"+e+"```");
            }
        }
    }

    private class Check extends Command
    {
        Check()
        {
            this.name = "check";
            this.help = "Checks if a user ID is blacklisted.";
            this.category = Categories.BOTADM;
            this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
            this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
            this.ownerCommand = false;
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event)
        {
            User user;

            if(event.getArgs().isEmpty())
            {
                event.replyWarning("Please specify a user ID!");
                return;
            }

            try
            {
                user = event.getJDA().retrieveUserById(event.getArgs()).complete();
            }
            catch(Exception e)
            {
                event.replyError("That ID isn't valid!");
                return;
            }

            try
            {
                if(!(Blacklists.isUserListed(user.getId())))
                {
                    event.replySuccess("**"+user.getName()+"#"+user.getDiscriminator()+"** isn't blacklisted!");
                }
                else
                {
                    event.replySuccess("**"+user.getName()+"#"+user.getDiscriminator()+"** is blacklisted!");
                }
            }
            catch(IOException e)
            {
                event.replyError("Something went wrong when reading the blacklisted users file: \n```"+e+"```");
            }
        }
    }
}