package mcjty.rftoolsstorage.modules.craftingmanager.client;

import mcjty.lib.client.QuadTransformer;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
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

    private TextureAtlasSprite getTexture() {
        String name = RFToolsStorage.MODID + ":block/machinecraftingmanager";
        return Minecraft.getInstance().getTextureMap().getAtlasSprite(name);
    }

    private TextureAtlasSprite getTop() {
        if (top == null) {
            top = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsStorage.MODID + ":block/machinecraftingmanager_top");
        }
        return top;
    }

    private TextureAtlasSprite getSide() {
        if (side == null) {
            side = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsStorage.MODID + ":block/machinecraftingmanager");
        }
        return side;
    }

    private TextureAtlasSprite getBottom() {
        if (bottom == null) {
            bottom = Minecraft.getInstance().getTextureMap().getAtlasSprite(RFToolsBase.MODID + ":block/base/machinebottom");
        }
        return bottom;
    }


    private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal,
                           double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float r, float g, float b) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float) x, (float) y, (float) z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, r, g, b, 1.0f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        u = sprite.getInterpolatedU(u);
                        v = sprite.getInterpolatedV(v);
                        builder.put(e, u, v, 0f, 1f);
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

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, 1.0f, 1.0f, 1.0f);
        putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, 1.0f, 1.0f, 1.0f);
        return builder.build();
    }

    private BakedQuad createQuadReversed(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
        Vec3d normal = v3.subtract(v1).crossProduct(v2.subtract(v1)).normalize();

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
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
                TRSRTransformation transformation = new TRSRTransformation(new Vector3f(xoffset, .3f, zoffset), null, new Vector3f(.3f, .3f, .3f), null);
                List<BakedQuad> output = QuadTransformer.processMany(input, transformation.getMatrixVec());
                quads.addAll(output);
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
