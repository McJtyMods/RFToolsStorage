package mcjty.rftoolsstorage.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
                Commands.literal(RFToolsStorage.MODID)
                        .then(CommandList.register(dispatcher))
                        .then(CommandRestore.register(dispatcher))
        );

        dispatcher.register(Commands.literal("rfstor").redirect(commands));
    }

}
