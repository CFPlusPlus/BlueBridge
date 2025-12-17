package de.mark225.bluebridge.core.addon;

import de.mark225.bluebridge.core.region.RegionSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ActiveAddonEventHandler {
    private static final Object LOCK = new Object();

    private static List<RegionSnapshot> addedOrUpdated = new ArrayList<>();
    private static List<RegionSnapshot> deleted = new ArrayList<>();

    public static void addOrUpdate(RegionSnapshot region) {
        synchronized (LOCK) {
            addedOrUpdated.add(region);
        }
    }

    public static void delete(RegionSnapshot region) {
        synchronized (LOCK) {
            deleted.add(region);
        }
    }

    public static void collectAndReset(BiConsumer<List<RegionSnapshot>, List<RegionSnapshot>> callback) {
        final List<RegionSnapshot> added;
        final List<RegionSnapshot> del;

        synchronized (LOCK) {
            // Listen “swappen”, damit neue Events in frische Listen gehen
            added = addedOrUpdated;
            del = deleted;
            addedOrUpdated = new ArrayList<>();
            deleted = new ArrayList<>();
        }

        // callback außerhalb des Locks ausführen
        callback.accept(added, del);
    }
}
