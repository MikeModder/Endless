package me.artuto.endless.commands.botadm;

import me.artuto.endless.Bot;
import me.artuto.endless.Const;
import me.artuto.endless.Endless;
import me.artuto.endless.commands.EndlessCommand;
import me.artuto.endless.commands.EndlessCommandEvent;
import me.artuto.endless.commands.cmddata.Categories;
import org.slf4j.Logger;

public class ReloadCmd extends EndlessCommand
{
    private final Bot bot;
    private final Logger LOG = Endless.getLog(ReloadCmd.class);

    public ReloadCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "reload";
        this.help = "Reloads from disk language files";
        this.category = Categories.BOTADM;
        this.ownerCommand = true;
        this.guildOnly = false;
        this.needsArguments = false;
    }

    @Override
    protected void executeCommand(EndlessCommandEvent event)
    {
        event.reply(Const.LOADING+" Reloading languages from disk...", m -> {
            try
            {
                bot.reloadLanguages();
                m.editMessage(event.getClient().getSuccess()+" Successfully reloaded all languages!").queue();
                LOG.debug("Successfully reloaded all languages.");
            }
            catch(Exception e)
            {
                m.editMessage(event.getClient().getError()+" Error while reloading all languages. " +
                        "Check console for more information: `"+e.getMessage()+"`").queue();
                LOG.error("Error while reloading all languages", e);
            }
        });
    }
}
