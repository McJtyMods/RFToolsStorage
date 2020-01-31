package mcjty.rftoolsstorage.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.modularstorage.ModularTypeModule;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularAmountOverlay;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, RFToolsStorage.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        generateModularStorage();
    }

    private void generateModularStorage() {
        BlockModelBuilder main = models().getBuilder("block/storage/modular_storage")
                .parent(models().getExistingFile(new ResourceLocation(RFToolsBase.MODID, "block/rftoolsblock")))
                .texture("front", modLoc("block/machinemodularstorage"));

        BlockModelBuilder overlayNone = models().getBuilder("block/storage/overlaynone")
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).cullface(Direction.NORTH).texture("#overlay").end().end()
                .texture("overlay", modLoc("block/overlaynone"));
        BlockModelBuilder overlayOre = models().getBuilder("block/storage/overlayore")
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).cullface(Direction.NORTH).texture("#overlay").end().end()
                .texture("overlay", modLoc("block/overlayore"));
        BlockModelBuilder overlayGeneric = models().getBuilder("block/storage/overlaygeneric")
                .element().from(0, 0, 0).to(16, 16, 16).face(Direction.NORTH).cullface(Direction.NORTH).texture("#overlay").end().end()
                .texture("overlay", modLoc("block/overlaygeneric"));

        BlockModelBuilder overlayAmount[] = new BlockModelBuilder[8];
        BlockModelBuilder overlayAmountR[] = new BlockModelBuilder[8];
        for (int i = 0; i < 8; i++) {
            overlayAmount[i] = models().getBuilder("block/storage/overlayamount" + i)
                    .element().from(12, 5, -0.2f).to(13, 13, 16)
                    .face(Direction.NORTH).cullface(Direction.NORTH).texture("#overlaya").uvs(14-i*2, 0, 15-i*2, 8).end()
                    .end()
                    .texture("overlaya", modLoc("block/overlayamount"));
            overlayAmountR[i] = models().getBuilder("block/storage/overlayamount_remote" + i)
                    .element().from(12, 5, -0.2f).to(13, 13, 16)
                    .face(Direction.NORTH).cullface(Direction.NORTH).texture("#overlaya").uvs(14-i*2, 0, 15-i*2, 8).end()
                    .end()
                    .texture("overlaya", modLoc("block/overlayamountremote"));
        }
        BlockModelBuilder overlayEmpty = models().getBuilder("block/storage/overlayamount_empty")
                .element().from(12, 5, -0.2f).to(13, 13, 16)
                .face(Direction.NORTH).cullface(Direction.NORTH).texture("#overlaya").uvs(0, 0, 1, 8).end()
                .end()
                .texture("overlaya", modLoc("block/overlayamount"));

        getMultipartBuilder(ModularStorageSetup.MODULAR_STORAGE.get())
                .part().modelFile(main).addModel().condition(BlockStateProperties.FACING, Direction.NORTH).end()
                .part().modelFile(main).rotationY(180).addModel().condition(BlockStateProperties.FACING, Direction.SOUTH).end()
                .part().modelFile(main).rotationY(270).addModel().condition(BlockStateProperties.FACING, Direction.WEST).end()
                .part().modelFile(main).rotationY(90).addModel().condition(BlockStateProperties.FACING, Direction.EAST).end()
                .part().modelFile(main).rotationX(-90).addModel().condition(BlockStateProperties.FACING, Direction.UP).end()
                .part().modelFile(main).rotationX(90).addModel().condition(BlockStateProperties.FACING, Direction.DOWN).end()

                .part().modelFile(overlayNone).addModel().condition(ModularStorageBlock.TYPEMODULE, ModularTypeModule.TYPE_NONE).end()
                .part().modelFile(overlayOre).addModel().condition(ModularStorageBlock.TYPEMODULE, ModularTypeModule.TYPE_ORE).end()
                .part().modelFile(overlayGeneric).addModel().condition(ModularStorageBlock.TYPEMODULE, ModularTypeModule.TYPE_GENERIC).end()

                .part().modelFile(overlayEmpty).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_EMPTY).end()
                .part().modelFile(overlayAmount[0]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G0).end()
                .part().modelFile(overlayAmount[1]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G1).end()
                .part().modelFile(overlayAmount[2]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G2).end()
                .part().modelFile(overlayAmount[3]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G3).end()
                .part().modelFile(overlayAmount[4]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G4).end()
                .part().modelFile(overlayAmount[5]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G5).end()
                .part().modelFile(overlayAmount[6]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G6).end()
                .part().modelFile(overlayAmount[7]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_G7).end()
                .part().modelFile(overlayAmountR[0]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R0).end()
                .part().modelFile(overlayAmountR[1]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R1).end()
                .part().modelFile(overlayAmountR[2]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R2).end()
                .part().modelFile(overlayAmountR[3]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R3).end()
                .part().modelFile(overlayAmountR[4]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R4).end()
                .part().modelFile(overlayAmountR[5]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R5).end()
                .part().modelFile(overlayAmountR[6]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R6).end()
                .part().modelFile(overlayAmountR[7]).addModel().condition(ModularStorageBlock.AMOUNT, ModularAmountOverlay.AMOUNT_R7).end()
        ;
    }
}
