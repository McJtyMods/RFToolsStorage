package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.items.CapabilityItemHandler;

public class CraftingManagerRenderer extends TileEntityRenderer<CraftingManagerTileEntity> {

    public CraftingManagerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(CraftingManagerTileEntity te, float v, MatrixStack matrixStack, IRenderTypeBuffer buffer, int ii, int i1) {

        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            // @todo 1.15
//            bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder renderer = tessellator.getBuffer();

            GlStateManager.pushMatrix();
            // @todo 1.15
//            GlStateManager.translated(x, y, z);
            BlockPos pos = te.getPos();
            GlStateManager.translatef(-pos.getX(), -pos.getY(), -pos.getZ());
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            for (int i = 0 ; i < 4 ; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, te.getWorld(), null);
                    // @todo 1.15
//                    model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.FIXED, false);
                    if (model.isBuiltInRenderer()) {
                        GlStateManager.pushMatrix();
                        GlStateManager.scalef(.3f, .3f, .3f);
                        GlStateManager.translatef(((i & 1) == 0) ? .15f : .55f, 0.93f, ((i & 2) == 0) ? .15f : .55f);

                        // @todo 1.15
//                        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
//                        BlockState mimicState = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
//                        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(mimicState, pos,
//                                getWorld(), renderer, getWorld().rand, EmptyModelData.INSTANCE);
//                        tessellator.draw();



                        //                        GlStateManager.scalef(4.3f, 4.3f, 4.3f);
//                        Minecraft.getInstance().getRenderManager()
//                        renderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
                        GlStateManager.popMatrix();
                    }
                }
            }

            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
        });
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(CraftingManagerSetup.TYPE_CRAFTING_MANAGER.get(), CraftingManagerRenderer::new);
    }

}
