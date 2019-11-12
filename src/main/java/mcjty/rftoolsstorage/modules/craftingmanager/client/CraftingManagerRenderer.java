package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import org.lwjgl.opengl.GL11;

public class CraftingManagerRenderer extends TileEntityRenderer<CraftingManagerTileEntity> {

    @Override
    public void render(CraftingManagerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.render(te, x, y, z, partialTicks, destroyStage);

        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(h -> {
            bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder renderer = tessellator.getBuffer();

            GlStateManager.pushMatrix();
            GlStateManager.translated(x, y, z);
            BlockPos pos = te.getPos();
            GlStateManager.translatef(-pos.getX(), -pos.getY(), -pos.getZ());
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            for (int i = 0 ; i < 4 ; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, te.getWorld(), null);
                    model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.FIXED, false);
                    if (model.isBuiltInRenderer()) {
                        GlStateManager.pushMatrix();
                        GlStateManager.scalef(.3f, .3f, .3f);
                        GlStateManager.translatef(((i & 1) == 0) ? .15f : .55f, 0.93f, ((i & 2) == 0) ? .15f : .55f);

                        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
                        BlockState mimicState = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
                        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(mimicState, pos,
                                getWorld(), renderer, getWorld().rand, EmptyModelData.INSTANCE);
                        tessellator.draw();



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
        ClientRegistry.bindTileEntitySpecialRenderer(CraftingManagerTileEntity.class, new CraftingManagerRenderer());
    }

}
