# RFToolsStorage

Adds Modular Storage, the Storage Scanner and tablet, and the Crafting Manager to RFTools Base.

This branch targets Minecraft 26.2 on NeoForge and requires Java 25. Run `./gradlew build` to build against the published McJtyLib and RFToolsBase artifacts. From the matching MultiWorkspace, run `./gradlew :RFToolsStorage:build` to build with the included library projects, or `./gradlew :workspace:runClient` to playtest the workspace sources together. Run `./gradlew runClientData` to regenerate recipes, tags, loot tables, and item model definitions.

When upgrading a 1.21.1 world, keep `data/RFToolsStorageHolder.dat` in the world directory. Storage imports this file on first access and saves the result to `data/rftoolsstorage/storage_holder.dat`. The original file is retained.
