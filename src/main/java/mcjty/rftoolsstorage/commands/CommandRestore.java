package mcjty.rftoolsstorage.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageHolder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CommandRestore implements Command<CommandSource> {

    private static final CommandRestore CMD = new CommandRestore();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("restore")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("uuid", StringArgumentType.word())
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String uuidString = context.getArgument("uuid", String.class);

        ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
        if (!(stack.getItem() instanceof StorageModuleItem)) {
            context.getSource().sendSuccess(
                    new StringTextComponent("Keep a storage module in your main hand!").withStyle(style -> style.applyFormat(TextFormatting.RED)), true);
            return 0;
        }

        int maxSize = StorageModuleItem.getSize(stack);

        StorageHolder holder = StorageHolder.get(context.getSource().getLevel());
        StorageEntry foundEntry = null;
        for (StorageEntry storage : holder.getStorages()) {
            if (storage.getUuid().toString().startsWith(uuidString)) {
                if (foundEntry != null) {
                    context.getSource().sendSuccess(
                            new StringTextComponent("Multiple storage entries match this UUID part!").withStyle(style -> style.applyFormat(TextFormatting.RED)), true);
                    return 0;
                }
                foundEntry = storage;
            }
        }

        if (foundEntry != null) {
            if (foundEntry.getStacks().size() != maxSize) {
                context.getSource().sendSuccess(
                        new StringTextComponent("Wrong foundEntry module tier! " + foundEntry.getStacks().size() + " stacks are required!").withStyle(style -> style.applyFormat(TextFormatting.RED)), true);
            } else {
                stack.getOrCreateTag().putUUID("uuid", foundEntry.getUuid());
                context.getSource().getPlayerOrException().inventoryMenu.broadcastChanges();
            }
        } else {
            context.getSource().sendSuccess(
                    new StringTextComponent("No storage found with UUID " + uuidString).withStyle(style -> style.applyFormat(TextFormatting.RED)), true);
        }
        return 0;
    }
}
