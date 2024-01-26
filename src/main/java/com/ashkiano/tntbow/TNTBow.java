package com.ashkiano.tntbow;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TNTBow extends JavaPlugin implements Listener {
    private String bowLore;
    private String bowName;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();

        bowLore = getConfig().getString("bow.lore", "Explosive TNT Bow");
        bowName = getConfig().getString("bow.name", "TNT Bow");

        Metrics metrics = new Metrics(this, 19537);

        this.getLogger().info("Thank you for using the TNTBow plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");

        this.getCommand("givetntbow").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be run by a player!");
                    return true;
                }

                Player player = (Player) sender;
                ItemStack tntBow = new ItemStack(Material.BOW);
                ItemMeta meta = tntBow.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(bowLore);
                meta.setLore(lore);
                meta.setDisplayName(bowName);
                tntBow.setItemMeta(meta);
                player.getInventory().addItem(tntBow);
                player.sendMessage("You have received the Explosive TNT Bow!");

                return true;
            }
        });
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && event.getBow() != null) {
            Player player = (Player) event.getEntity();
            ItemStack bow = event.getBow();

            if (bow == null) return;

            if (bow.hasItemMeta() && bow.getItemMeta().hasLore() && bow.getItemMeta().getLore().contains(bowLore)) {
                event.setConsumeItem(false);

                if (player.getInventory().contains(Material.TNT)) {
                    player.getInventory().removeItem(new ItemStack(Material.TNT, 1));
                    event.getProjectile().setMetadata(bowLore, new FixedMetadataValue(this, true));
                } else {
                    player.sendMessage("You don't have any TNT!");
                    event.setCancelled(true);
                }

                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onArrowLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();
        if (!arrow.hasMetadata(bowLore)) return;

        if (event.getHitBlock() == null) return;

        Block blockHit = event.getHitBlock();
        if (event.getEntity() instanceof Arrow && event.getEntity().getShooter() instanceof Player) {
            if (arrow.hasMetadata(bowLore)) {
                BlockFace blockFace = event.getHitBlockFace();
                Block placeForTNT = blockHit.getRelative(blockFace);

                if (placeForTNT.getType() == Material.AIR) {
                    TNTPrimed tnt = (TNTPrimed) placeForTNT.getWorld().spawnEntity(placeForTNT.getLocation().add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(40);
                }

                arrow.remove();
            }
        }
    }

}