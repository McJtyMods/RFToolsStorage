package mcjty.rftoolsstorage.modules.craftingmanager.client;

import mcjty.lib.client.AbstractDynamicBakedModel;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CraftingManagerBakedModel extends AbstractDynamicBakedModel {

    public static final ResourceLocation TEXTURE_CRAFTING_MANAGER = new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager");
    public static final ResourceLocation TEXTURE_TOP = new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager_top");
    public static final ResourceLocation TEXTURE_SIDE = new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager");
    public static final ResourceLocation TEXTURE_BOTTOM = new ResourceLocation(RFToolsBase.MODID, "block/base/machinebottom");

    private TextureAtlasSprite bottom;
    private TextureAtlasSprite top;
    private TextureAtlasSprite side;

    private TextureAtlasSprite getTop() {
        if (top == null) {
            top = getTexture(TEXTURE_TOP);
        }
        return top;
    }

    private TextureAtlasSprite getSide() {
        if (side == null) {
            side = getTexture(TEXTURE_SIDE);
        }
        return side;
    }

    private TextureAtlasSprite getBottom() {
        if (bottom == null) {
            bottom = getTexture(TEXTURE_BOTTOM);
        }
        return bottom;
    }


    private static void appendQuads(List<BakedQuad> quads, BlockState state, @Nullable Direction side, @Nonnull Random rand, float xoffset, float zoffset) {
        ModelResourceLocation location = BlockModelShapes.getModelLocation(state);
        if (location != null) {
            IBakedModel model = Minecraft.getInstance().getModelManager().getModel(location);
            if (model != null && !model.isBuiltInRenderer()) {
                List<BakedQuad> input = model.getQuads(state, side, rand, EmptyModelData.INSTANCE);
                // @todo 1.15
//                TRSRTransformation transformation = new TRSRTransformation(new Vector3f(xoffset, .3f, zoffset), null, new Vector3f(.3f, .3f, .3f), null);
//                List<BakedQuad> output = QuadTransformer.processMany(input, transformation.getMatrixVec());
//                quads.addAll(output);
            }
        }
    }


    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        List<BakedQuad> quads = new ArrayList<>();
        if (side == null) {
            quads.add(createQuadReversed(v(0.0, 1.0, 0.0), v(0.0, 1.0, 1.0), v(1.0, 1.0, 1.0), v(1.0, 1.0, 0.0), getTop()));
            quads.add(createQuad(v(1.0, 1.0, 0.0), v(1.0, 1.0, 1.0), v(0.0, 1.0, 1.0), v(0.0, 1.0, 0.0), getTop()));

            quads.add(createQuad(v(0.0, 0.0, 0.0), v(1.0, 0.0, 0.0), v(1.0, 0.0, 1.0), v(0.0, 0.0, 1.0), getBottom()));
            quads.add(createQuadReversed(v(0.0, 0.3, 1.0), v(1.0, 0.3, 1.0), v(1.0, 0.3, 0.0), v(0.0, 0.3, 0.0), getBottom()));

            quads.add(createQuad(v(1.0, 1.0, 0.0), v(1.0, 0.0, 0.0), v(1.0, 0.0, 1.0), v(1.0, 1.0, 1.0), getSide()));
            quads.add(createQuadReversed(v(1.0, 1.0, 1.0), v(1.0, 0.0, 1.0), v(1.0, 0.0, 0.0), v(1.0, 1.0, 0.0), getSide()));

            quads.add(createQuad(v(0.0, 1.0, 0.0), v(0.0, 0.0, 0.0), v(0.0, 0.0, 1.0), v(0.0, 1.0, 1.0), getSide()));
            quads.add(createQuadReversed(v(0.0, 1.0, 1.0), v(0.0, 0.0, 1.0), v(0.0, 0.0, 0.0), v(0.0, 1.0, 0.0), getSide()));

            quads.add(createQuad(v(1.0, 1.0, 0.0), v(1.0, 0.0, 0.0), v(0.0, 0.0, 0.0), v(0.0, 1.0, 0.0), getSide()));
            quads.add(createQuadReversed(v(0.0, 1.0, 0.0), v(0.0, 0.0, 0.0), v(1.0, 0.0, 0.0), v(1.0, 1.0, 0.0), getSide()));

            quads.add(createQuad(v(0.0, 1.0, 1.0), v(0.0, 0.0, 1.0), v(1.0, 0.0, 1.0), v(1.0, 1.0, 1.0), getSide()));
            quads.add(createQuadReversed(v(1.0, 1.0, 1.0), v(1.0, 0.0, 1.0), v(0.0, 0.0, 1.0), v(0.0, 1.0, 1.0), getSide()));
        }

        for (int i = 0 ; i < 4 ; i++) {
            BlockState mimic = extraData.getData(CraftingManagerTileEntity.MIMIC[i]);
            if (mimic != null) {
                appendQuads(quads, mimic, side, rand, ((i & 1) == 0) ? .15f : .55f, ((i & 2) == 0) ? .15f : .55f);
            }
        }

        return quads;
    }

    public TextureAtlasSprite getParticleTexture() {
        return getTexture(TEXTURE_CRAFTING_MANAGER);
    }
}
