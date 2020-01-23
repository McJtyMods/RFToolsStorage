package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.google.common.collect.ImmutableList;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CraftingManagerBakedModel implements IDynamicBakedModel {

    private final VertexFormat format;

    private TextureAtlasSprite bottom;
    private TextureAtlasSprite top;
    private TextureAtlasSprite side;

    public CraftingManagerBakedModel(VertexFormat format) {
        this.format = format;
    }

    @Override
    public boolean func_230044_c_() {
        return false;
    }

    private TextureAtlasSprite getTexture() {
        return Minecraft.getInstance().getTextureGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager"));
    }

    private TextureAtlasSprite getTop() {
        if (top == null) {
            top = Minecraft.getInstance().getTextureGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager_top"));
        }
        return top;
    }

    private TextureAtlasSprite getSide() {
        if (side == null) {
            side = Minecraft.getInstance().getTextureGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager"));
        }
        return side;
    }

    private TextureAtlasSprite getBottom() {
        if (bottom == null) {
            bottom = Minecraft.getInstance().getTextureGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation(RFToolsBase.MODID, "block/base/machinebottom"));
        }
        return bottom;
    }


    private void putVertex(BakedQuadBuilder builder, Vec3d normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {
        // @todo 1.15 GENERALIZE THIS
        ImmutableList<VertexFormatElement> elements = format.func_227894_c_().asList();
        for (int e = 0; e < elements.size(); e++) {
            switch (elements.get(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float)x, (float)y, (float)z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, r, g, b, 1.0f);
                    break;
                case UV:
                    switch (elements.get(e).getIndex()) {
                        case 0:
                            float iu = sprite.getInterpolatedU(u);
                            float iv = sprite.getInterpolatedV(v);
                            builder.put(e, iu, iv);
                            break;
                        case 2:
                            builder.put(e, 0f, 1f);
                            break;
                        default:
                            builder.put(e);
                            break;
                    }
                case NORMAL:
                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z, 0f);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        Vec3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z));
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private BakedQuad createQuadReversed(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        Vec3d normal = v3.subtract(v1).crossProduct(v2.subtract(v1)).normalize();

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z));
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private static Vec3d v(double x, double y, double z) {
        return new Vec3d(x, y, z);
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

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return getTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

}
