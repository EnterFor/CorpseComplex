package top.theillusivec4.corpsecomplex.common.modules.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability;
import top.theillusivec4.corpsecomplex.common.capability.DeathStorageCapability.Provider;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.Inventory;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.VanillaInventory;
import top.theillusivec4.corpsecomplex.common.modules.inventory.inventories.integration.CuriosInventory;

public class InventoryModule {

  public static final List<Inventory> STORAGE = new ArrayList<>();
  public static final Random RANDOM = new Random();

  @SubscribeEvent
  public void serverStart(final FMLServerStartedEvent evt) {
    STORAGE.add(new VanillaInventory());

    if (ModList.get().isLoaded("curios")) {
      STORAGE.add(new CuriosInventory());
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void playerDeath(final LivingDeathEvent evt) {

    if (!(evt.getEntityLiving() instanceof PlayerEntity)) {
      return;
    }
    PlayerEntity playerEntity = (PlayerEntity) evt.getEntityLiving();
    World world = playerEntity.getEntityWorld();

    if (!world.isRemote() && !world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
      DeathStorageCapability.getCapability(playerEntity).ifPresent(
          deathStorage -> STORAGE.forEach(storage -> storage.storeInventory(deathStorage)));
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void playerRespawn(final PlayerEvent.Clone evt) {

    if (evt.isWasDeath()) {
      DeathStorageCapability.getCapability(evt.getPlayer()).ifPresent(
          newStorage -> DeathStorageCapability.getCapability(evt.getOriginal()).ifPresent(
              oldStorage -> STORAGE
                  .forEach(storage -> storage.retrieveInventory(newStorage, oldStorage))));
    }
  }
}
