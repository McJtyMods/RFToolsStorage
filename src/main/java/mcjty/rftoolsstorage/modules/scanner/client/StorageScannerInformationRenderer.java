package mcjty.rftoolsstorage.modules.scanner.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.client.HudRenderHelper;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.TypedMap;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerInformationScreenInfo.CRAFT_KEYS;

public class StorageScannerInformationRenderer {

    public static void renderCrafting(MatrixStack matrixStack, IRenderTypeBuffer buffer, TypedMap data, Direction orientation, double x, double y, double z, double scale) {
        List<Pair<ItemStack, String>> log = getLog(data);
        HudRenderHelper.HudPlacement hudPlacement = HudRenderHelper.HudPlacement.HUD_FRONT;
        HudRenderHelper.HudOrientation hudOrientation = HudRenderHelper.HudOrientation.HUD_SOUTH;
        HudRenderHelper.renderHudItems(matrixStack, buffer, log, hudPlacement, hudOrientation, orientation, x - orientation.getXOffset() * .95, y, z - orientation.getZOffset() * .95, (float) (1.0f + scale));
    }

    private static List<Pair<ItemStack, String>> getLog(TypedMap data) {
        List<Pair<ItemStack, String>> list = new ArrayList<>();
        list.add(Pair.of(ItemStack.EMPTY, ""));

        if (data != null && data.size() > 0) {
            for (Pair<Key<ItemStack>, Key<Boolean>> pair : CRAFT_KEYS) {
                Key<ItemStack> stackKey = pair.getLeft();
                Key<Boolean> errorKey = pair.getRight();
                ItemStack stack = data.get(stackKey);
                if (stack != null && !stack.isEmpty()) {
                    list.add(Pair.of(stack, data.get(errorKey) ? "ERROR" : "crafting"));
                }
            }

        }
        return list;
    }

}
