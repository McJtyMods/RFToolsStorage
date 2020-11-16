package mcjty.rftoolsstorage.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> commands = dispatcher.register(
                Commands.literal(RFToolsStorage.MODID)
                        .then(CommandList.register(dispatcher))
                        .then(CommandRestore.register(dispatcher))
        );

        dispatcher.register(Commands.literal("rfstor").redirect(commands));
    }

}
