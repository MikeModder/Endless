package me.artuto.endless.commands.serverconfig;

import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.Locale;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;

import java.util.Arrays;

/**
 * @author Artuto
 */

public class LocaleCmd extends EndlessCommand
{
    private Bot bot;

    public LocaleCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "locale";
        this.help = "Changes the server's language";
        this.aliases = new String[]{"language"};
        this.category = Categories.SERVER_CONFIG;
        this.userPerms = new Permission[]{Permission.MANAGE_SERVER};
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        if(event.getArgs().isEmpty() || event.getArgs().equalsIgnoreCase("list"))
        {
            EmbedBuilder builder = new EmbedBuilder();
            MessageBuilder mb = new MessageBuilder();
            StringBuilder sb = new StringBuilder();
            String title = Const.INFO+" "+event.localize("command.locale.title");

            for(Locale loc : Locale.values())
                sb.append(loc.getFlag()).append(" ").append(loc.getEnglishName()).append(" (`").append(loc.getLocalizedName()).append("`)\n");

            builder.setColor(event.getSelfMember().getColor()).setDescription(sb);
            mb.setEmbed(builder.build()).setContent(title);
            event.reply(mb.build());
        }
        else
        {
            String name = event.getArgs();
            Locale loc = getMatch(name);
            if(loc==null)
            {
                event.replyError("command.locale.set.notFound");
                return;
            }

            bot.gsdm.setLocale(event.getGuild(), loc);
            event.replySuccess("command.locale.set", loc.getEnglishName());
        }
    }

    private Locale getMatch(String arg)
    {
        return Arrays.stream(Locale.values()).filter(loc -> Arrays.asList(loc.getAliases())
                .contains(arg)).findFirst().orElse(null);
    }
}
