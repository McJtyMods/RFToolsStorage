package mcjty.rftoolsstorage.modules.scanner.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class StorageControlClientScreenModule implements IClientScreenModule<StorageControlScreenModule.ModuleDataStacks> {

    @Override
    public TransformMode getTransformMode(ItemStack moduleItem) {
        return TransformMode.ITEM;
    }

    @Override
    public int getHeight(ItemStack moduleItem) {
        return 114;
    }

    @Override
    public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {

    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, StorageControlScreenModule.ModuleDataStacks screenData, ModuleRenderInfo renderInfo) {
        if (screenData == null) {
            return;
        }

        StorageControlScreenModule data = StorageControlModuleItem.data(renderInfo.moduleStack);
        List<ItemStack> stacks = data.stacks();

        PoseStack poseStack = graphics.pose();

        if (renderInfo.hitx >= 0) {
            poseStack.pushPose();
            poseStack.translate(-0.5F, 0.5F, 0.07F);
            float f3 = 0.0105F;
            poseStack.scale(f3 * renderInfo.factor, -f3 * renderInfo.factor, f3);

            int y = currenty;
            int i = 0;

            for (int yy = 0 ; yy < 3 ; yy++) {
                for (int xx = 0 ; xx < 3 ; xx++) {
                    if (!stacks.get(i).isEmpty()) {
                        int x = xx * 40;
                        boolean hilighted = renderInfo.hitx >= x+8 && renderInfo.hitx <= x + 38 && renderInfo.hity >= y-7 && renderInfo.hity <= y + 22;
                        if (hilighted) {
                            RenderHelper.drawFlatButtonBox(graphics, buffer, (int) (5 + xx * 30.5f), 10 + yy * 24 - 4, (int) (29 + xx * 30.5f), 10 + yy * 24 + 20, 0xffffffff, 0xff333333, 0xffffffff,
                                    renderInfo.getLightmapValue());
                        }
                    }
                    i++;
                }
                y += 35;
            }
            poseStack.popPose();
        }

        poseStack.pushPose();
        float f3 = 0.0105F;
        poseStack.translate(-0.5F, 0.5F, 0.06F);
        float factor = renderInfo.factor;
        poseStack.scale(f3 * factor, f3 * factor, 0.0001f);

        int y = currenty;
        int i = 0;

        for (int yy = 0 ; yy < 3 ; yy++) {
            for (int xx = 0 ; xx < 3 ; xx++) {
                if (!stacks.get(i).isEmpty()) {
                    int x = 7 + xx * 30;
                    renderSlot(poseStack, buffer, -16-y, stacks.get(i), x, renderInfo.getLightmapValue());
                }
                i++;
            }
            y += 24;
        }

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.5F, 0.5F, 0.08F);
        f3 = 0.0050F;
        poseStack.scale(f3 * factor, -f3 * factor, 0.0001f);

        y = currenty + 30;
        i = 0;

        for (int yy = 0 ; yy < 3 ; yy++) {
            for (int xx = 0 ; xx < 3 ; xx++) {
                if (!stacks.get(i).isEmpty()) {
                    renderSlotOverlay(poseStack, buffer, fontRenderer, y, stacks.get(i), screenData.getAmount(i), 32 + xx * 64,
                            renderInfo.getLightmapValue());
                }
                i++;
            }
            y += 52;
        }

        boolean insertStackActive = renderInfo.hitx >= 0 && renderInfo.hitx < 60 && renderInfo.hity > 98 && renderInfo.hity <= 120;
        RenderHelper.renderText(fontRenderer, "Insert Stack", 20, y - 20, insertStackActive ? 0xffffff : 0x666666, poseStack, buffer, renderInfo.getLightmapValue());
        boolean insertAllActive = renderInfo.hitx >= 60 && renderInfo.hitx <= 120 && renderInfo.hity > 98 && renderInfo.hity <= 120;
        RenderHelper.renderText(fontRenderer, "Insert All", 120, y - 20, insertAllActive ? 0xffffff : 0x666666, poseStack, buffer, renderInfo.getLightmapValue());

        poseStack.popPose();
    }

    private void renderSlot(PoseStack matrixStack, MultiBufferSource buffer, int currenty, ItemStack stack, int x, int lightmapValue) {
        matrixStack.pushPose();
        matrixStack.translate(x +8f, currenty +8f, 5);
        matrixStack.scale(16, 16, 16);

        RenderHelper.renderItemGui(matrixStack, buffer, stack, lightmapValue, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }

    private void renderSlotOverlay(PoseStack matrixStack, MultiBufferSource buffer, Font fontRenderer, int currenty, ItemStack stack, int amount, int x, int lightmapValue) {
        if (!stack.isEmpty()) {
            String s1;
            if (amount < 10000) {
                s1 = String.valueOf(amount);
            } else if (amount < 1000000) {
                s1 = amount / 1000 + "k";
            } else if (amount < 1000000000) {
                s1 = amount / 1000000 + "m";
            } else {
                s1 = amount / 1000000000 + "g";
            }
            RenderHelper.renderText(fontRenderer, s1, x + 19 - 2 - fontRenderer.width(s1), currenty + 6 + 3, 16777215, matrixStack, buffer, lightmapValue);

            if (stack.getItem().isBarVisible(stack)) {
                double health = stack.getItem().getBarWidth(stack);
                int j1 = (int) Math.round(13.0D - health * 13.0D);
                int k = (int) Math.round(255.0D - health * 255.0D);
                int l = 255 - k << 16 | k << 8;
                int i1 = (255 - k) / 4 << 16 | 16128;
                VertexConsumer builder = buffer.getBuffer(CustomRenderTypes.QUADS_NOTEXTURE);
                renderQuad(builder, x + 2, currenty + 13, 13, 2, 0, 0.0f, lightmapValue);
                renderQuad(builder, x + 2, currenty + 13, 12, 1, i1, 0.02f, lightmapValue);
                renderQuad(builder, x + 2, currenty + 13, j1, 1, l, 0.04f, lightmapValue);
            }
        }
    }

    private static void renderQuad(VertexConsumer builder, int x, int y, int width, int height, int color, float offset, int lightmapValue) {
        builder.addVertex(x, y, offset).setColor(1.0f, 1.0f, 1.0f, 1.0f).setLight(lightmapValue);
        builder.addVertex(x, (y + height), offset).setColor(1.0f, 1.0f, 1.0f, 1.0f).setLight(lightmapValue);
        builder.addVertex((x + width), (y + height), offset).setColor(1.0f, 1.0f, 1.0f, 1.0f).setLight(lightmapValue);
        builder.addVertex((x + width), y, offset).setColor(1.0f, 1.0f, 1.0f, 1.0f).setLight(lightmapValue);
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
