package top.theillusivec4.corpsecomplex.common.modules.miscellaneous;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import top.theillusivec4.corpsecomplex.common.Setting;
import top.theillusivec4.corpsecomplex.common.config.ConfigParser;
import top.theillusivec4.corpsecomplex.common.config.CorpseComplexConfig;

public class MiscellaneousSetting implements Setting<MiscellaneousOverride> {

  private boolean restrictRespawning;
  private List<ItemStack> respawnItems = new ArrayList<>();

  public boolean isRestrictRespawning() {
    return restrictRespawning;
  }

  public void setRestrictRespawning(boolean restrictRespawning) {
    this.restrictRespawning = restrictRespawning;
  }

  public List<ItemStack> getRespawnItems() {
    return respawnItems;
  }

  public void setRespawnItems(List<ItemStack> respawnItems) {
    this.respawnItems = respawnItems;
  }

  @Override
  public void importConfig() {
    ConfigParser.parseItems(CorpseComplexConfig.respawnItems)
        .forEach((item, integer) -> this.getRespawnItems().add(new ItemStack(item, integer)));
    this.setRestrictRespawning(CorpseComplexConfig.restrictRespawning);
  }

  @Override
  public void applyOverride(MiscellaneousOverride override) {
    override.getRespawnItems().ifPresent(this::setRespawnItems);
    override.getRestrictRespawning().ifPresent(this::setRestrictRespawning);
  }
}