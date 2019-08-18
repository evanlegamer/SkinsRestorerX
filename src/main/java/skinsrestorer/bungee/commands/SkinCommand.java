package skinsrestorer.bungee.commands;

import co.aikar.commands.*;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bungee.contexts.OnlinePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import skinsrestorer.bungee.SkinsRestorer;
import skinsrestorer.shared.exception.SkinRequestException;
import skinsrestorer.shared.interfaces.ISkinCommand;
import skinsrestorer.shared.storage.Config;
import skinsrestorer.shared.storage.CooldownStorage;
import skinsrestorer.shared.storage.Locale;
import skinsrestorer.shared.utils.C;

import java.util.concurrent.TimeUnit;

@CommandAlias("skin") @CommandPermission("%skin")
public class SkinCommand extends BaseCommand implements ISkinCommand<CommandSender, ProxiedPlayer, OnlinePlayer> {
    private SkinsRestorer plugin;

    public SkinCommand(SkinsRestorer plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onDefault(CommandSender sender) {
        this.onHelp(sender, this.getCurrentCommandManager().generateCommandHelp());
    }

    @Default @CommandPermission("%skinSet")
    @Description("%helpSkinSet")
    @Syntax("<skin/url>")
    public void onSkinSetShort(ProxiedPlayer p, @Single String skin) {
        this.onSkinSetOther(p, new OnlinePlayer(p), skin);
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        if (Config.USE_OLD_SKIN_HELP)
            sendHelp(sender);
        else
            help.showHelp();
    }


    @Subcommand("clear") @CommandPermission("%skinClear")
    @Description("%helpSkinClear")
    public void onSkinClear(ProxiedPlayer p) {
        this.onSkinClearOther(p, new OnlinePlayer(p));
    }

    @Subcommand("clear") @CommandPermission("%skinClearOther")
    @CommandCompletion("@players")
    @Description("%helpSkinClearOther")
    public void onSkinClearOther(CommandSender sender, OnlinePlayer target) {
        ProxyServer.getInstance().getScheduler().runAsync(SkinsRestorer.getInstance(), () -> {
            ProxiedPlayer p = target.getPlayer();
            String skin = plugin.getSkinStorage().getDefaultSkinNameIfEnabled(p.getName(), true);

            // remove users custom skin and set default skin / his skin
            plugin.getSkinStorage().removePlayerSkin(p.getName());
            if (this.setSkin(sender, p, skin, false)) {
                if (!sender.getName().equals(target.getPlayer().getName()))
                    sender.sendMessage(new TextComponent(Locale.SKIN_CLEAR_ISSUER.replace("%player", target.getPlayer().getName())));
                else
                    sender.sendMessage(new TextComponent(Locale.SKIN_CLEAR_SUCCESS));
            }
        });
    }


    @Subcommand("update") @CommandPermission("%skinUpdate")
    @Description("%helpSkinUpdate")
    public void onSkinUpdate(ProxiedPlayer p) {
        this.onSkinUpdateOther(p, new OnlinePlayer(p));
    }

    @Subcommand("update") @CommandPermission("%skinUpdateOther")
    @CommandCompletion("@players")
    @Description("%helpSkinUpdateOther")
    public void onSkinUpdateOther(CommandSender sender, OnlinePlayer target) {
        ProxyServer.getInstance().getScheduler().runAsync(SkinsRestorer.getInstance(), () -> {
            ProxiedPlayer p = target.getPlayer();
            String skin = plugin.getSkinStorage().getPlayerSkin(p.getName());

            // User has no custom skin set, get the default skin name / his skin
            if (skin == null)
                skin = plugin.getSkinStorage().getDefaultSkinNameIfEnabled(p.getName(), true);

            if (!plugin.getSkinStorage().forceUpdateSkinData(skin)) {
                sender.sendMessage(new TextComponent(Locale.ERROR_UPDATING_SKIN));
                return;
            }

            if (this.setSkin(sender, p, skin, false)) {
                if (!sender.getName().equals(target.getPlayer().getName()))
                    sender.sendMessage(new TextComponent(Locale.SUCCESS_UPDATING_SKIN_OTHER.replace("%player", target.getPlayer().getName())));
                else
                    sender.sendMessage(new TextComponent(Locale.SUCCESS_UPDATING_SKIN));
            }
        });
    }


    @Subcommand("set") @CommandPermission("%skinSet")
    @Description("%helpSkinSet")
    @Syntax("<skin/url>")
    public void onSkinSet(ProxiedPlayer p, String skin) {
        this.onSkinSetOther(p, new OnlinePlayer(p), skin);
    }

    @Subcommand("set") @CommandPermission("%skinSetOther")
    @CommandCompletion("@players")
    @Description("%helpSkinSetOther")
    @Syntax("<target> <skin/url>")
    public void onSkinSetOther(CommandSender sender, OnlinePlayer target, String skin) {
        if (Config.PER_SKIN_PERMISSIONS && Config.USE_NEW_PERMISSIONS) {
            if (!sender.hasPermission("skinsrestorer.skin." + skin)) {
                if (!sender.getName().equals(target.getPlayer().getName()) || (!sender.hasPermission("skinsrestorer.ownskin") && !skin.equalsIgnoreCase(sender.getName()))) {
                    sender.sendMessage(new TextComponent(Locale.PLAYER_HAS_NO_PERMISSION_SKIN));
                    return;
                }
            }
        }

        ProxyServer.getInstance().getScheduler().runAsync(SkinsRestorer.getInstance(), () -> {
            if (this.setSkin(sender, target.getPlayer(), skin)) {
                if (!sender.getName().equals(target.getPlayer().getName())) {
                    sender.sendMessage(new TextComponent(Locale.ADMIN_SET_SKIN.replace("%player", target.getPlayer().getName())));
                }
            }
        });
    }


    private boolean setSkin(CommandSender sender, ProxiedPlayer p, String skin) {
        return this.setSkin(sender, p, skin, true);
    }
    // if save is false, we won't save the skin skin name
    // because default skin names shouldn't be saved as the users custom skin
    private boolean setSkin(CommandSender sender, ProxiedPlayer p, String skin, boolean save) {
        if (!C.validUsername(skin) && !C.validUrl(skin)) {
            sender.sendMessage(new TextComponent(Locale.INVALID_PLAYER.replace("%player", skin)));
            return false;
        }

        if (Config.DISABLED_SKINS_ENABLED)
            if (!sender.hasPermission("skinsrestorer.bypassdisabled")) {
                for (String dskin : Config.DISABLED_SKINS)
                    if (skin.equalsIgnoreCase(dskin)) {
                        sender.sendMessage(new TextComponent(Locale.SKIN_DISABLED));
                        return false;
                    }
            }

        if (!sender.hasPermission("skinsrestorer.bypasscooldown") && CooldownStorage.hasCooldown(sender.getName())) {
            sender.sendMessage(new TextComponent(Locale.SKIN_COOLDOWN_NEW.replace("%s", "" + CooldownStorage.getCooldown(sender.getName()))));
            return false;
        }

        CooldownStorage.resetCooldown(sender.getName());
        CooldownStorage.setCooldown(sender.getName(), Config.SKIN_CHANGE_COOLDOWN, TimeUnit.SECONDS);

        String oldSkinName = plugin.getSkinStorage().getPlayerSkin(p.getName());
        if (C.validUsername(skin)) {
            try {
                plugin.getMojangAPI().getUUID(skin);
                if (save) {
                    plugin.getSkinStorage().setPlayerSkin(p.getName(), skin);
                    plugin.getSkinApplier().applySkin(p);
                } else {
                    plugin.getSkinApplier().applySkin(p, skin, null);
                }
                p.sendMessage(new TextComponent(Locale.SKIN_CHANGE_SUCCESS));
                return true;
            } catch (SkinRequestException e) {
                sender.sendMessage(new TextComponent(e.getReason()));
                this.rollback(p, oldSkinName, save); // set custom skin name back to old one if there is an exception
            } catch (Exception e) {
                e.printStackTrace();
                this.rollback(p, oldSkinName, save); // set custom skin name back to old one if there is an exception
            }
            return false;
        }
        if (C.validUrl(skin)) {
            try {
                sender.sendMessage(new TextComponent(Locale.MS_UPDATING_SKIN));
                String skinentry = " "+p.getName(); // so won't overwrite premium playernames
                if (skinentry.length() > 16) { skinentry = skinentry.substring(0, 16); } // max len of 16 char
                plugin.getSkinStorage().setSkinData(skinentry, plugin.getMineSkinAPI().genSkin(skin),
                        Long.toString(System.currentTimeMillis() + (100L * 365 * 24 * 60 * 60 * 1000))); // "generate" and save skin for 100 years
                plugin.getSkinStorage().setPlayerSkin(p.getName(), skinentry); // set player to "whitespaced" name then reload skin
                plugin.getSkinApplier().applySkin(p);
                p.sendMessage(new TextComponent(Locale.SKIN_CHANGE_SUCCESS));
                return true;
            } catch (SkinRequestException e) {
                sender.sendMessage(new TextComponent(e.getReason()));
                this.rollback(p, oldSkinName, save); // set custom skin name back to old one if there is an exception
            } catch (Exception e) {
                e.printStackTrace();
                this.rollback(p, oldSkinName, save); // set custom skin name back to old one if there is an exception
            }
            return false;
        }
        return false;
    }

    private void rollback(ProxiedPlayer p, String oldSkinName, boolean save) {
        if (save)
            plugin.getSkinStorage().setPlayerSkin(p.getName(), oldSkinName != null ? oldSkinName : p.getName());
    }

    private void sendHelp(CommandSender sender) {
        if (!Locale.SR_LINE.isEmpty())
            sender.sendMessage(new TextComponent(Locale.SR_LINE));
        sender.sendMessage(new TextComponent(Locale.HELP_PLAYER.replace("%ver%", SkinsRestorer.getInstance().getVersion())));
        if (!Locale.SR_LINE.isEmpty())
            sender.sendMessage(new TextComponent(Locale.SR_LINE));
    }
}
