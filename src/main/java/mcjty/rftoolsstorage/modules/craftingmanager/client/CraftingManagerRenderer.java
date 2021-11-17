package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;

public class CraftingManagerRenderer extends TileEntityRenderer<CraftingManagerTileEntity> {

    public CraftingManagerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CraftingManagerTileEntity te, float v, @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            matrixStack.pushPose();

//            BlockPos pos = te.getPos();
//            GlStateManager.translatef(-pos.getX(), -pos.getY(), -pos.getZ());
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

            for (int i = 0 ; i < 4 ; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    matrixStack.pushPose();
                    matrixStack.scale(.3f, .3f, .3f);
                    matrixStack.translate(((i & 1) == 0) ? .45f : 1.8f, 0.93f, ((i & 2) == 0) ? .45f : 1.8f);
                    BlockState state = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                    blockRenderer.renderBlock(state, matrixStack, buffer, RenderHelper.MAX_BRIGHTNESS, combinedOverlay, EmptyModelData.INSTANCE);
                    matrixStack.popPose();
                }
            }

            matrixStack.popPose();
        });
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(CraftingManagerModule.TYPE_CRAFTING_MANAGER.get(), CraftingManagerRenderer::new);
    }

}
