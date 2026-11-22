package mcjty.rftoolsstorage.modules.scanner.items;

import net.minecraft.client.renderer.SubmitNodeCollector;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

public class StorageControlClientScreenModule implements IClientScreenModule<StorageControlScreenModule.ModuleDataStacks> {

    @Override
    public TransformMode getTransformMode(ItemStack moduleItem) {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleItem) {
        return 114;
    }

    @Override
    public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {

    }

    @Override
    public void render(GuiGraphicsExtractor graphics, SubmitNodeCollector collector, IModuleRenderHelper helper, Font font,
                       int currentY, StorageControlScreenModule.ModuleDataStacks screenData, ModuleRenderInfo info) {
        if (screenData == null) {
            return;
        }

        List<ItemStack> stacks = StorageControlModuleItem.data(info.moduleStack).stacks();
        for (int i = 0; i < stacks.size() && i < 9; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            int column = i % 3;
            int row = i / 3;
            int x = 9 + column * 40;
            int y = currentY + row * 35;
            if (info.hitx >= x - 1 && info.hitx <= x + 29 && info.hity >= row * 35 - 7 && info.hity <= row * 35 + 22) {
                RenderHelper.drawFlatButtonBox(graphics, x - 2, y - 2, x + 29, y + 23, 0xffffffff, 0xff333333, 0xffffffff);
            }
            graphics.item(stack, x, y);
            int count = screenData.getAmount(i);
            String amount = count < 10000 ? Integer.toString(count)
                    : count < 1000000 ? count / 1000 + "k"
                    : count < 1000000000 ? count / 1000000 + "m" : count / 1000000000 + "g";
            graphics.text(font, amount, x + 26 - font.width(amount), y + 17, 0xffffffff, false);
            if (stack.isBarVisible()) {
                graphics.fill(x + 2, y + 13, x + 15, y + 15, 0xff000000);
                graphics.fill(x + 2, y + 13, x + 2 + stack.getBarWidth(), y + 14, 0xff000000 | stack.getBarColor());
            }
        }

        boolean insertStackActive = info.hitx >= 0 && info.hitx < 60 && info.hity > 98 && info.hity <= 120;
        boolean insertAllActive = info.hitx >= 60 && info.hitx <= 120 && info.hity > 98 && info.hity <= 120;
        graphics.text(font, "Insert Stack", 5, currentY + 103, insertStackActive ? 0xffffffff : 0xff888888, false);
        graphics.text(font, "Insert All", 65, currentY + 103, insertAllActive ? 0xffffffff : 0xff888888, false);
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
