package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nonnull;

public class CraftingManagerRenderer implements BlockEntityRenderer<CraftingManagerTileEntity> {

    public CraftingManagerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CraftingManagerTileEntity te, float v, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        te.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            matrixStack.pushPose();

//            BlockPos pos = te.getPos();
//            GlStateManager.translatef(-pos.getX(), -pos.getY(), -pos.getZ());
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

            for (int i = 0 ; i < 4 ; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    matrixStack.pushPose();
                    matrixStack.scale(.3f, .3f, .3f);
                    matrixStack.translate(((i & 1) == 0) ? .45f : 1.8f, 0.93f, ((i & 2) == 0) ? .45f : 1.8f);
                    BlockState state = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                    // @todo 1.18
//                    blockRenderer.renderBatched(state, te.getBlockPos(), te.getLevel(), matrixStack, buffer, RenderHelper.MAX_BRIGHTNESS, combinedOverlay, EmptyModelData.INSTANCE);
                    matrixStack.popPose();
                }
            }

            matrixStack.popPose();
        });
    }

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CraftingManagerModule.TYPE_CRAFTING_MANAGER.get(), CraftingManagerRenderer::new);
    }

}
