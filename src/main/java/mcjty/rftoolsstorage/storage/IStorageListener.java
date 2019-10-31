package mcjty.rftoolsstorage.storage;

public interface IStorageListener {

    void onContentsChanged(int version, int slot);

}
