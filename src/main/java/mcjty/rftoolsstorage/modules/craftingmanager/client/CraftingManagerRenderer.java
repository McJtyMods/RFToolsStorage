package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
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

public class CraftingManagerRenderer extends TileEntityRenderer<CraftingManagerTileEntity> {

    public CraftingManagerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CraftingManagerTileEntity te, float v, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            matrixStack.push();

//            BlockPos pos = te.getPos();
//            GlStateManager.translatef(-pos.getX(), -pos.getY(), -pos.getZ());
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

            for (int i = 0 ; i < 4 ; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    matrixStack.push();
                    matrixStack.scale(.3f, .3f, .3f);
                    matrixStack.translate(((i & 1) == 0) ? .45f : 1.8f, 0.93f, ((i & 2) == 0) ? .45f : 1.8f);
                    BlockState state = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
                    blockRenderer.renderBlock(state, matrixStack, buffer, 0xf000f0, combinedOverlay, EmptyModelData.INSTANCE);
                    matrixStack.pop();
                }
            }

            matrixStack.pop();
        });
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(CraftingManagerSetup.TYPE_CRAFTING_MANAGER.get(), CraftingManagerRenderer::new);
    }

}
