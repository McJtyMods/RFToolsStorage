package mcjty.rftoolsstorage.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandList implements Command<CommandSourceStack> {

    private static final CommandList CMD = new CommandList();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("list")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        for (StorageEntry storage : StorageHolder.get(context.getSource().getLevel()).getStorages()) {
            String createdBy = storage.getCreatedBy();
            String uuid = storage.getUuid().toString();
            String createdByColor;
            if (createdBy == null || createdBy.isEmpty()) {
                createdByColor = String.valueOf(ChatFormatting.GRAY);
                createdBy  = "(Unknown creator)";
            } else {
                createdByColor = String.valueOf(ChatFormatting.YELLOW);
                createdBy = "(" + createdBy + ")";
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date creationTime = new Date(storage.getCreationTime());
            Date updateTime = new Date(storage.getUpdateTime());
            String createTimeF = dateFormat.format(creationTime);
            String updateTimeF = dateFormat.format(updateTime);

            String output = String.format(ChatFormatting.GREEN + "%s: %s%s\n" + ChatFormatting.WHITE + "Create " +
                    ChatFormatting.YELLOW + "%s" + ChatFormatting.WHITE + ", Update " +
                    ChatFormatting.YELLOW + "%s",
                    uuid, createdByColor, createdBy, createTimeF, updateTimeF);
            context.getSource().sendSuccess(new TextComponent(output), false);

        }
        return 0;
    }
}
