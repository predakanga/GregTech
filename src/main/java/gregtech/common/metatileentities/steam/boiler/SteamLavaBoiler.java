package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankHandler;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class SteamLavaBoiler extends SteamBoiler {

    private FluidTank lavaFluidTank;

    public SteamLavaBoiler(String metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.LAVA_BOILER_OVERLAY, 100);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SteamLavaBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected FluidTankHandler createImportFluidHandler() {
        FluidTankHandler superHandler = super.createImportFluidHandler();
        this.lavaFluidTank = new FilteredFluidHandler(16000)
            .setFillPredicate(ModHandler::isLava);
        return new FluidTankHandler(superHandler, lavaFluidTank);

    }

    public static final int LAVA_PER_OPERATION = 100;

    @Override
    protected void tryConsumeNewFuel() {
        if(lavaFluidTank.getFluidAmount() >= LAVA_PER_OPERATION) {
            lavaFluidTank.drain(LAVA_PER_OPERATION, true);
            setFuelMaxBurnTime(LAVA_PER_OPERATION);
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected ModularUI<IUIHolder> createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer)
            .widget(100, new TankWidget<>(lavaFluidTank, 109, 18, 10, 54)
                .setBackgroundTexture(getGuiTexture("bar_%s_empty"), 1))
            .build(getHolder(), entityPlayer);
    }
}