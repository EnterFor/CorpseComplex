/*
 * Copyright (c) 2017. <C4>
 *
 * This Java class is distributed as a part of Corpse Complex.
 * Corpse Complex is open source and licensed under the GNU General Public
 * License v3.
 * A copy of the license can be found here: https://www.gnu.org/licenses/gpl
 * .text
 */

package c4.corpsecomplex.common.modules.inventory;

import c4.corpsecomplex.CorpseComplex;
import c4.corpsecomplex.common.Module;
import c4.corpsecomplex.common.Submodule;
import c4.corpsecomplex.common.modules.compatibility.advinv.AdvHandler;
import c4.corpsecomplex.common.modules.compatibility.advinv.AdvModule;
import c4.corpsecomplex.common.modules.compatibility.baubles.BaublesHandler;
import c4.corpsecomplex.common.modules.compatibility.baubles.BaublesModule;
import c4.corpsecomplex.common.modules.compatibility.camping.CampingHandler;
import c4.corpsecomplex.common.modules.compatibility.camping.CampingModule;
import c4.corpsecomplex.common.modules.compatibility.cosmeticarmorreworked.CosmeticHandler;
import c4.corpsecomplex.common.modules.compatibility.cosmeticarmorreworked.CosmeticModule;
import c4.corpsecomplex.common.modules.compatibility.galacticraftcore.GalacticraftHandler;
import c4.corpsecomplex.common.modules.compatibility.galacticraftcore.GalacticraftModule;
import c4.corpsecomplex.common.modules.compatibility.powerinventory.OPHandler;
import c4.corpsecomplex.common.modules.compatibility.powerinventory.OPModule;
import c4.corpsecomplex.common.modules.compatibility.rpginventory.RPGHandler;
import c4.corpsecomplex.common.modules.compatibility.rpginventory.RPGModule;
import c4.corpsecomplex.common.modules.compatibility.thebetweenlands.BetweenlandsHandler;
import c4.corpsecomplex.common.modules.compatibility.thebetweenlands.BetweenlandsModule;
import c4.corpsecomplex.common.modules.compatibility.thut_wearables.ThutHandler;
import c4.corpsecomplex.common.modules.compatibility.thut_wearables.ThutModule;
import c4.corpsecomplex.common.modules.compatibility.toolbelt.ToolbeltHandler;
import c4.corpsecomplex.common.modules.compatibility.toolbelt.ToolbeltModule;
import c4.corpsecomplex.common.modules.compatibility.wearablebackpacks.WBHandler;
import c4.corpsecomplex.common.modules.compatibility.wearablebackpacks.WBModule;
import c4.corpsecomplex.common.modules.inventory.capability.DeathInventory;
import c4.corpsecomplex.common.modules.inventory.capability.IDeathInventory;
import c4.corpsecomplex.common.modules.inventory.enchantment.EnchantmentModule;
import c4.corpsecomplex.common.modules.inventory.helpers.DeathInventoryHandler;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

public class InventoryModule extends Module {

  public static ArrayList<Class<? extends DeathInventoryHandler>> handlerClasses;
  public static boolean destroyCursed;
  public static double randomDrop;
  public static boolean randomDropOnlyMain;
  public static String[] essentialItems;
  public static String[] cursedItems;
  public static double randomDestroy;

  public static double dropLoss;
  public static boolean difficultyDropLoss;
  public static double dropLossEasy;
  public static double dropLossNormal;
  public static double dropLossHard;

  public static double keptLoss;
  public static boolean difficultyKeptLoss;
  public static double keptLossEasy;
  public static double keptLossNormal;
  public static double keptLossHard;

  public static double dropDrain;
  public static double keptDrain;
  public static boolean durabilityLossLimiter;

  static boolean keepArmor;
  static boolean keepHotbar;
  static boolean keepMainhand;
  static boolean keepOffhand;
  static boolean keepMainInventory;

  private static boolean noDropDespawn;
  private static int dropDespawnTimer;
  private static boolean cfgEnabled;

  {
    submoduleClasses = new ArrayList<>();
    handlerClasses = new ArrayList<>();

    addSubmodule(EnchantmentModule.class);
    addSubmodule("wearablebackpacks", WBModule.class, WBHandler.class);
    addSubmodule("thut_wearables", ThutModule.class, ThutHandler.class);
    addSubmodule("rpginventory", RPGModule.class, RPGHandler.class);
    addSubmodule("powerinventory", OPModule.class, OPHandler.class);
    addSubmodule("baubles", BaublesModule.class, BaublesHandler.class);
    addSubmodule("galacticraftcore", GalacticraftModule.class, GalacticraftHandler.class);
    addSubmodule("cosmeticarmorreworked", CosmeticModule.class, CosmeticHandler.class);
    addSubmodule("toolbelt", ToolbeltModule.class, ToolbeltHandler.class);
    addSubmodule("advinv", AdvModule.class, AdvHandler.class);
    addSubmodule("camping", CampingModule.class, CampingHandler.class);
    addSubmodule("thebetweenlands", BetweenlandsModule.class, BetweenlandsHandler.class);

    handlerClasses.add(InventoryHandler.class);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void storeDeathInventory(LivingDeathEvent e) {

    if (!(e.getEntityLiving() instanceof EntityPlayer)) {
      return;
    }

    EntityPlayer player = (EntityPlayer) e.getEntityLiving();

    if (!player.world.getGameRules().getBoolean("keepInventory") && !player.world.isRemote) {
      storeInventories(player);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
  public void canceledDeath(LivingDeathEvent e) {

    if (!(e.getEntityLiving() instanceof EntityPlayer)) {
      return;
    }

    EntityPlayer player = (EntityPlayer) e.getEntityLiving();

    if (!player.world.getGameRules().getBoolean("keepInventory") && e.isCanceled()) {
      retrieveInventories(player, player);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onPlayerRespawnBegin(PlayerEvent.Clone e) {

    if (!e.isWasDeath() || e.getEntityPlayer().world.isRemote) {
      return;
    }

    EntityPlayer player = e.getEntityPlayer();
    EntityPlayer oldPlayer = e.getOriginal();

    if (!player.world.getGameRules().getBoolean("keepInventory")) {
      retrieveInventories(player, oldPlayer);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onPlayerDrops(PlayerDropsEvent e) {

    if (noDropDespawn) {
      e.getDrops().forEach(EntityItem::setNoDespawn);
    } else if (dropDespawnTimer > 0) {
      e.getDrops().forEach(item -> item.lifespan = dropDespawnTimer * 20);
    }
  }


  public InventoryModule() {
    super("Inventory", "Customize how your inventory is handled on death and respawn");
  }

  public void loadModuleConfig() {
    setCategoryComment();
    cfgEnabled = getBool("Enable Inventory Module", false, "Set to true to enable inventory module",
        true);
    keepArmor = getBool("Keep Armor", false, "Set to true to keep equipped armor on death", false);
    keepHotbar = getBool("Keep Hotbar", false,
        "Set to true to keep non-mainhand hotbar items on death", false);
    keepMainhand = getBool("Keep Mainhand", false, "Set to true to keep mainhand item on death",
        false);
    keepOffhand = getBool("Keep Offhand", false, "Set to true to keep offhand item on death",
        false);
    keepMainInventory = getBool("Keep Main Inventory", false,
        "Set to true to keep main inventory (non-equipped non-hotbar) " + "items on death", false);
    destroyCursed = getBool("Destroy Cursed Items", false,
        "Set to true to destroy cursed items instead of dropping them", false);
    randomDrop = getDouble("Random Drop Chance", 0, 0, 1,
        "Percent chance that items that are kept will still drop", false);
    randomDropOnlyMain = getBool("Random Drop Only Main Inventory", false,
        "Set to true to only apply random drop chance to the main " + "inventory", false);
    randomDestroy = getDouble("Random Destroy Chance", 0, 0, 1,
        "Percent chance that dropped items will be destroyed", false);
    essentialItems = getStringList("Essential Items", new String[]{},
        "List of items that are always kept", false);
    cursedItems = getStringList("Cursed Items", new String[]{},
        "List of items that are always dropped", false);
    dropLoss = getDouble("Durability Loss on Drops", 0, 0, 1,
        "Percent of durability lost on death for drops", false);
    difficultyDropLoss = getBool("Durability Loss Dependent on Difficulty", false, "Set to true to make the durability loss on drops dependent on world difficulty",
            false);
    dropLossEasy = getDouble("Durability Loss on Drops on Easy", 0, 0, 1,
            "Percent of durability lost on death for drops on easy difficulty", false);
    dropLossNormal = getDouble("Durability Loss on Drops on Normal", 0, 0, 1,
            "Percent of durability lost on death for drops on normal difficulty", false);
    dropLossHard = getDouble("Durability Loss on Drops Hard", 0, 0, 1,
            "Percent of durability lost on death for drops on hard difficulty", false);
    keptLoss = getDouble("Durability Loss on Kept Items", 0, 0, 1,
        "Percent of durability lost on death for kept items", false);
    difficultyKeptLoss = getBool("Kept Durability Loss Dependent on Difficulty", false,
            "Set to true to make the durability loss on kept items dependent on world difficulty",
            false);
    keptLossEasy = getDouble("Durability Loss on Kept Items on Easy", 0, 0, 1,
            "Percent of durability lost on death for Kept Items on easy difficulty", false);
    keptLossNormal = getDouble("Durability Loss on Kept Items on Normal", 0, 0, 1,
            "Percent of durability lost on death for Kept Items on normal difficulty", false);
    keptLossHard = getDouble("Durability Loss on Kept Items Hard", 0, 0, 1,
            "Percent of durability lost on death for Kept Items on hard difficulty", false);
    dropDrain = getDouble("Energy Drain on Drops", 0, 0, 1,
        "Percent of energy drained on death for drops", false);
    keptDrain = getDouble("Energy Drain on Kept Items", 0, 0, 1,
        "Percent of energy drained on death for kept items", false);
    durabilityLossLimiter = getBool("Limit Durability Loss", false,
        "Set to true to limit durability loss so that items will never break due to death penalities",
        false);
    noDropDespawn = getBool("No Drop Despawn", false,
        "Set to true to " + "prevent death drops from despawning", false);
    dropDespawnTimer = getInt("Drop Despawn Timer", -1, -1, 10000,
        "Time (in seconds) to set for drop despawn timer, -1 for vanilla", false);
  }

  public void initPropOrder() {
    propOrder = new ArrayList<>(Arrays
        .asList("Enable Inventory Module", "Keep Armor", "Keep Hotbar", "Keep Mainhand",
            "Keep Offhand", "Keep Main Inventory", "Durability Loss on Drops", "Durability Loss on Drops on Difficulty",
            "Durability Loss on Drops on Easy", "Durability Loss on Drops on Normal", "Durability Loss on Drops on Hard",
            "Durability Loss on Kept Items", "Durability Loss on Drops on Difficulty",
            "Durability Loss on Kept Items on Easy", "Durability Loss on Kept Items on Normal", "Durability Loss on Kept Items on Hard",
            "Energy Drain on Drops", "Energy Drain on Kept Items",
            "Random Drop Chance", "Random Drop Only Main Inventory", "Random Destroy Chance",
            "Essential Items", "Cursed Items", "Destroy Cursed Items"));
  }

  public void setEnabled() {
    enabled = cfgEnabled;
  }

  private void storeInventories(EntityPlayer player) {

    handlerClasses.forEach(handler -> {
      try {
        handler.getDeclaredConstructor(EntityPlayer.class).newInstance(player).storeInventory();
      } catch (Exception e1) {
        CorpseComplex.logger.log(Level.ERROR, "Failed to initialize handler " + handler, e1);
      }
    });
  }

  private void retrieveInventories(EntityPlayer player, EntityPlayer oldPlayer) {

    IDeathInventory oldDeathInventory = oldPlayer
        .getCapability(DeathInventory.Provider.DEATH_INV_CAP, null);

    handlerClasses.forEach(handler -> {
      try {
        handler.getDeclaredConstructor(EntityPlayer.class).newInstance(player)
            .retrieveInventory(oldDeathInventory);
      } catch (Exception e1) {
        CorpseComplex.logger.log(Level.ERROR, "Failed to initialize handler " + handler, e1);
      }
    });
  }

  private void addSubmodule(String modid, Class<? extends Submodule> submodule,
      Class<? extends DeathInventoryHandler> handler) {
    if (Loader.isModLoaded(modid) && !submoduleClasses.contains(submodule)) {
      submoduleClasses.add(submodule);
      if (!handlerClasses.contains(handler)) {
        handlerClasses.add(handler);
      }
    }
  }
}
