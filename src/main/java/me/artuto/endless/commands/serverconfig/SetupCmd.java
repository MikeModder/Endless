package me.artuto.endless.commands.serverconfig;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.cmddata.Categories;
import me.artuto.endless.utils.ChecksUtil;
import me.artuto.endless.utils.GuildUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SetupCmd extends EndlessCommand
{
    private final Bot bot;

    public SetupCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "setup";
        this.children = new Command[]{new MutedRoleCmd(), new DisableAtEveryone()};
        this.help = "Server setup";
        this.category = Categories.SERVER_CONFIG;
        this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(CommandEvent event)
    {
        event.reply(Const.INFO+" Use this command to setup:\n" +
                Const.LINE_START+" Muted role (and channel overrides)\n" +
                Const.LINE_START+" Disable everyone permissions for every role that isn't Admin");
    }

    private class MutedRoleCmd extends EndlessCommand
    {
        MutedRoleCmd()
        {
            this.name = "mutedrole";;
            this.help = "Setup the muted role";
            this.category = Categories.SERVER_CONFIG;
            this.botPerms = new Permission[]{Permission.ADMINISTRATOR};
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArguments = false;
            this.parent = SetupCmd.this;
        }

        @Override
        protected void executeCommand(CommandEvent event)
        {
            Guild guild = event.getGuild();
            Role mutedRole = GuildUtils.getMutedRole(guild);
            String confirm;

            if(!(mutedRole==null))
            {
                if(!(ChecksUtil.canMemberInteract(event.getSelfMember(), mutedRole)))
                {
                    event.replyError("I can't interact with the existing *"+mutedRole.getName()+"* role!");
                    return;
                }

                if(!(ChecksUtil.canMemberInteract(event.getMember(), mutedRole)))
                {
                    event.replyError("You can't interact with the existing *"+mutedRole.getName()+"* role!");
                    return;
                }
                confirm = "This will modify the role \""+mutedRole.getName()+"\" role and assign it overrides on every Channel, Continue?";
            }
            else confirm = "This will create a new role called \"Muted\" and assign it overrides on every Channel, Continue?";

            waitForConfirm(event, confirm, () -> setupMutedRole(event, mutedRole));
        }
    }

    private class DisableAtEveryone extends EndlessCommand
    {
        DisableAtEveryone()
        {
            this.name = "disableateveryone";
            this.help = "Disables the everyone permission for every role that isn't Admin";
            this.category = Categories.SERVER_CONFIG;
            this.aliases = new String[]{"disableveryone", "disablevaronbros", "disableale0hio"};
            this.botPerms = new Permission[]{Permission.ADMINISTRATOR};
            this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
            this.needsArguments = false;
            this.parent = SetupCmd.this;
        }

        @Override
        protected void executeCommand(CommandEvent event)
        {
            long count = event.getGuild().getRoles().stream().filter(r -> r.getPermissions().contains(Permission.MESSAGE_MENTION_EVERYONE) &&
                    !(r.getPermissions().contains(Permission.MANAGE_SERVER))).count();

            if(count==0)
            {
                event.replySuccess("No changes made because every role had their Permission removed.");
                return;
            }

            String confirm = "This will remove the `Mention Everyone` permission form every role that **doesn't** has `Manage Server`. Continue?";
            waitForConfirm(event, confirm, () -> disableEveryone(event));
        }
    }

    private void setupMutedRole(CommandEvent event, Role role)
    {
        StringBuilder sb = new StringBuilder(event.getClient().getSuccess()+" Starting setup...\n");
        event.reply(sb+Const.LOADING+" Creating role...", m -> event.async(() -> {
            try
            {
                Role mutedRole;
                if(role==null)
                    mutedRole = event.getGuild().getController().createRole().setName("Muted").setColor(Color.RED).complete();
                else
                    mutedRole = role;
                sb.append(event.getClient().getSuccess()).append(" Role created!\n");
                m.editMessage(sb+Const.LOADING+" Creating Category overrides...").complete();

                PermissionOverride po;
                for(net.dv8tion.jda.core.entities.Category cat : event.getGuild().getCategories())
                {
                    po = cat.getPermissionOverride(mutedRole);
                    if(po==null)
                        cat.createPermissionOverride(mutedRole).setDeny(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE,
                                Permission.VOICE_CONNECT, Permission.VOICE_SPEAK).complete();
                    else
                        po.getManager().deny(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE,
                                Permission.VOICE_CONNECT, Permission.VOICE_SPEAK).complete();
                }
                sb.append(event.getClient().getSuccess()).append(" Successfully created Category overrides!\n");
                m.editMessage(sb+Const.LOADING+" Creating Text Channel overrides...").complete();
                for(TextChannel tc : event.getGuild().getTextChannels())
                {
                    po = tc.getPermissionOverride(mutedRole);
                    if(po==null)
                        tc.createPermissionOverride(mutedRole).setDeny(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE).complete();
                    else
                        po.getManager().deny(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE).complete();
                }
                sb.append(event.getClient().getSuccess()).append(" Successfully created Text Channel overrides!\n");
                m.editMessage(sb+Const.LOADING+" Creating Voice Channel overrides...").complete();
                for(VoiceChannel vc : event.getGuild().getVoiceChannels())
                {
                    po = vc.getPermissionOverride(mutedRole);
                    if(po==null)
                        vc.createPermissionOverride(mutedRole).setDeny(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK).complete();
                    else
                        po.getManager().deny(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK).complete();
                }
                m.editMessage(sb+event.getClient().getSuccess()+" Successfully created Voice Channel overrides!\n\n" +
                        event.getClient().getSuccess()+" Muted role setup has been completed!").queue();
            }
            catch(Exception e)
            {
                m.editMessage(sb+event.getClient().getError()+" Something went wrong while setting up the Muted role. Check that I have " +
                        "Administrator permission and the Muted role is below my higher role.").queue();
            }
        }));
    }

    private void disableEveryone(CommandEvent event)
    {
        StringBuilder sb = new StringBuilder(event.getClient().getSuccess()+" Starting setup...\n");
        event.reply(sb+Const.LOADING+" Filtering roles...", m -> event.async(() -> {
            try
            {
                List<Role> roles = event.getGuild().getRoles().stream().filter(r -> r.getPermissions().contains(Permission.MESSAGE_MENTION_EVERYONE) &&
                        !(r.getPermissions().contains(Permission.MANAGE_SERVER)) && ChecksUtil.canMemberInteract(event.getSelfMember(), r))
                        .collect(Collectors.toList());

                for(Role role : roles)
                    role.getManager().revokePermissions(Permission.MESSAGE_MENTION_EVERYONE).complete();

                sb.append(event.getClient().getSuccess()).append(" Successfully revoked permissions!");
                m.editMessage(sb).queue();
            }
            catch(Exception e)
            {
                m.editMessage(sb+event.getClient().getError()+" Something went wrong while editing the roles!" +
                        " Please check I still have `Administrator` permission.").queue();
            }
        }));
    }

    private void waitForConfirm(CommandEvent event, String confirm, Runnable action)
    {
        new ButtonMenu.Builder()
                .setChoices("444226239683624962", "444226355555729428")
                .setEventWaiter(bot.waiter)
                .setTimeout(1, TimeUnit.MINUTES)
                .setText(event.getClient().getWarning()+" "+confirm+"\n\n"+event.getClient().getSuccess()+" Continue\n" +
                        event.getClient().getError()+" Cancel")
                .setFinalAction(m -> m.delete().queue(s -> {}, e -> {}))
                .setUsers(event.getAuthor())
                .setAction(re -> {
                    if(re.isEmote() && re.getEmote().getAsMention().equals(event.getClient().getSuccess()))
                        action.run();
                }).build().display(event.getTextChannel());
    }
}
