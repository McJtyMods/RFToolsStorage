package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

// @todo temporary until we can do it in the baked model properly
public class CraftingManagerRenderer extends TileEntityRenderer<CraftingManagerTileEntity> {

    @Override
    public void render(CraftingManagerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        super.render(te, x, y, z, partialTicks, destroyStage);
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();
        BlockPos pos = te.getPos();

        //        GlStateManager.rotatef(180, 0, 1, 0);
        GlStateManager.scaled(.3, .3, .3);

        float lowOffset = .4f;
        float highOffset = 1.8f;

        GlStateManager.pushMatrix();
        GlStateManager.translatef(-pos.getX()+ lowOffset, -pos.getY()+.9f, -pos.getZ()+ lowOffset);
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(Blocks.GRINDSTONE.getDefaultState(), pos,
                getWorld(), renderer, getWorld().rand, EmptyModelData.INSTANCE);
        tessellator.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(-pos.getX()+ highOffset, -pos.getY()+.9f, -pos.getZ()+ lowOffset);
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(Blocks.CRAFTING_TABLE.getDefaultState(), pos,
                getWorld(), renderer, getWorld().rand, EmptyModelData.INSTANCE);
        tessellator.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(-pos.getX()+ highOffset, -pos.getY()+.9f, -pos.getZ()+ highOffset);
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(Blocks.FURNACE.getDefaultState(), pos,
                getWorld(), renderer, getWorld().rand, EmptyModelData.INSTANCE);
        tessellator.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(-pos.getX()+ lowOffset, -pos.getY()+.9f, -pos.getZ()+ highOffset);
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(Blocks.BLAST_FURNACE.getDefaultState(), pos,
                getWorld(), renderer, getWorld().rand, EmptyModelData.INSTANCE);
        tessellator.draw();
        GlStateManager.popMatrix();

//        GlStateManager.disableRescaleNormal();
//        RenderHelper.enableStandardItemLighting();
//        GlStateManager.enableLighting();
//        Minecraft.getInstance().getItemRenderer().renderItem(new ItemStack(Blocks.GRINDSTONE), ItemCameraTransforms.TransformType.NONE);

        GlStateManager.popMatrix();
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(CraftingManagerTileEntity.class, new CraftingManagerRenderer());
    }

}
