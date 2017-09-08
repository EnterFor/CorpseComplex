package c4.corpsecomplex.common;

import net.minecraftforge.common.config.ConfigCategory;

import java.util.function.Consumer;

/**
 * Java class created by C4 as part of the Corpse Complex mod for Minecraft.
 * Source Code: https://github.com/TheIllusiveC4/CorpseComplex
 * Corpse Complex is distributed under the MIT License: https://opensource.org/licenses/MIT
 */
public abstract class Submodule extends Module {

    public abstract void loadModuleConfig();

    protected Module parentModule;

    public Submodule(Module parentModule, String childCategory) {
        super();
        this.parentModule = parentModule;
        if (childCategory != null) {
            configCategory = new ConfigCategory(childCategory, parentModule.configCategory);
        } else {
            configCategory = parentModule.configCategory;
        }
    }

    public void setEnabled() {
        enabled = parentModule.enabled;
    }

    @Override
    public boolean hasEvents() {
        return false;
    }

    @Override
    public void loadSubmodules() {
        //NO-OP
    }

    @Override
    public void initPropOrder() {
        //NO-OP
    }

    @Override
    protected void addSubmodule(Class<? extends Submodule> submodule) {
        //NO-OP
    }

    @Override
    protected void addSubmodule(String modid, Class<? extends Submodule> submodule) {
        //NO-OP
    }

    @Override
    public void forEachSubmodule(Consumer<Submodule> submodule) {
        //NO-OP
    }
}
