package mcjty.rftoolsstorage.modules.scanner;

import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbase.api.screens.data.IModuleDataFactory;
import mcjty.rftoolsstorage.modules.scanner.items.StorageControlScreenModule;

import java.util.*;

public class ScannerModuleRegistry implements IScreenModuleRegistry {

    private Map<String, IModuleDataFactory<?>> dataFactoryMap = new HashMap<>();
    private Map<String, Integer> idToIntMap = null;
    private Map<Integer, String> inttoIdMap = null;


    public void registerBuiltins() {
        dataFactoryMap.put(StorageControlScreenModule.ModuleDataStacks.ID, StorageControlScreenModule.ModuleDataStacks::new);
    }

    @Override
    public void registerModuleDataFactory(String id, IModuleDataFactory<?> dataFactory) {
        dataFactoryMap.put(id, dataFactory);
    }

    @Override
    public IModuleDataFactory<?> getModuleDataFactory(String id) {
        return dataFactoryMap.get(id);
    }

    public String getNormalId(int i) {
        createIdMap();
        return inttoIdMap.get(i);
    }

    public int getShortId(String id) {
        createIdMap();
        return idToIntMap.get(id);
    }

    private void createIdMap() {
        if (idToIntMap == null) {
            idToIntMap = new HashMap<>();
            inttoIdMap = new HashMap<>();
            List<String> strings = new ArrayList<>(dataFactoryMap.keySet());
            strings.sort(Comparator.<String>naturalOrder());
            int idx = 0;
            for (String s : strings) {
                idToIntMap.put(s, idx);
                inttoIdMap.put(idx, s);
                idx++;
            }
        }
    }
}
