package gregtech.common.covers.filter;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.util.ItemStackKey;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class SmartItemFilter extends ItemFilter {

    private SmartFilteringMode filteringMode = SmartFilteringMode.ELECTROLYZER;

    public SmartFilteringMode getFilteringMode() {
        return filteringMode;
    }

    public void setFilteringMode(SmartFilteringMode filteringMode) {
        this.filteringMode = filteringMode;
        markDirty();
    }

    @Override
    public int getSlotTransferLimit(Object matchSlot, Set<ItemStackKey> matchedStacks, int globalTransferLimit) {
        ItemAndMetadataAndStackSize itemAndMetadata = (ItemAndMetadataAndStackSize) matchSlot;
        return itemAndMetadata.transferStackSize;
    }

    @Override
    public Object matchItemStack(ItemStack itemStack) {
        ItemAndMetadata itemAndMetadata = new ItemAndMetadata(itemStack);
        Integer cachedTransferRateValue = filteringMode.transferStackSizesCache.get(itemAndMetadata);

        if (cachedTransferRateValue == null) {
            ItemStack infinitelyBigStack = itemStack.copy();
            infinitelyBigStack.setCount(Integer.MAX_VALUE);
            Recipe recipe = filteringMode.recipeMap.findRecipe(Long.MAX_VALUE, Collections.singletonList(infinitelyBigStack), Collections.emptyList(), Integer.MAX_VALUE);
            if(recipe == null) {
                filteringMode.transferStackSizesCache.put(itemAndMetadata, 0);
                cachedTransferRateValue = 0;
            } else {
                CountableIngredient inputIngredient = recipe.getInputs().iterator().next();
                filteringMode.transferStackSizesCache.put(itemAndMetadata, inputIngredient.getCount());
                cachedTransferRateValue = inputIngredient.getCount();
            }
        }

        if (cachedTransferRateValue == 0) {
            return null;
        }
        return new ItemAndMetadataAndStackSize(itemAndMetadata, cachedTransferRateValue);
    }

    @Override
    public void initUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new CycleButtonWidget(10, y, 75, 20,
            SmartFilteringMode.class, this::getFilteringMode, this::setFilteringMode));
    }

    @Override
    public int getTotalOccupiedHeight() {
        return 20;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("FilterMode", filteringMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.filteringMode = SmartFilteringMode.values()[tagCompound.getInteger("FilterMode")];
    }

    private class ItemAndMetadataAndStackSize {
        public final ItemAndMetadata itemAndMetadata;
        public final int transferStackSize;

        public ItemAndMetadataAndStackSize(ItemAndMetadata itemAndMetadata, int transferStackSize) {
            this.itemAndMetadata = itemAndMetadata;
            this.transferStackSize = transferStackSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemAndMetadataAndStackSize)) return false;
            ItemAndMetadataAndStackSize that = (ItemAndMetadataAndStackSize) o;
            return itemAndMetadata.equals(that.itemAndMetadata);
        }

        @Override
        public int hashCode() {
            return itemAndMetadata.hashCode();
        }
    }

    public enum SmartFilteringMode implements IStringSerializable {
        ELECTROLYZER("cover.smart_item_filter.mode.electrolyzer", RecipeMaps.ELECTROLYZER_RECIPES),
        CENTRIFUGE("cover.smart_item_filter.mode.centrifuge", RecipeMaps.CENTRIFUGE_RECIPES);

        private final Map<ItemAndMetadata, Integer> transferStackSizesCache = new HashMap<>();
        public final String localeName;
        public final RecipeMap<?> recipeMap;

        SmartFilteringMode(String localeName, RecipeMap<?> recipeMap) {
            this.localeName = localeName;
            this.recipeMap = recipeMap;
        }

        @Override
        public String getName() {
            return localeName;
        }
    }

}
