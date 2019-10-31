package mcjty.rftoolsstorage.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageHolder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandList implements Command<CommandSource> {

    private static final CommandList CMD = new CommandList();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("list")
                .requires(cs -> cs.hasPermissionLevel(2))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        for (StorageEntry storage : StorageHolder.get().getStorages()) {
            String createdBy = storage.getCreatedBy();
            String uuid = storage.getUuid().toString();
            String createdByColor;
            if (createdBy == null || createdBy.isEmpty()) {
                createdByColor = String.valueOf(TextFormatting.GRAY);
                createdBy  = "(Unknown creator)";
            } else {
                createdByColor = String.valueOf(TextFormatting.YELLOW);
                createdBy = "(" + createdBy + ")";
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date creationTime = new Date(storage.getCreationTime());
            Date updateTime = new Date(storage.getUpdateTime());
            String createTimeF = dateFormat.format(creationTime);
            String updateTimeF = dateFormat.format(updateTime);

            String output = String.format(TextFormatting.GREEN + "%s: %s%s\n" + TextFormatting.WHITE + "Create " +
                    TextFormatting.YELLOW + "%s" + TextFormatting.WHITE + ", Update " +
                    TextFormatting.YELLOW + "%s",
                    uuid, createdByColor, createdBy, createTimeF, updateTimeF);
            context.getSource().sendFeedback(new StringTextComponent(output), false);

        }
        return 0;
    }
}
