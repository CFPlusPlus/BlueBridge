package de.mark225.bluebridge.griefprevention.addon.listener;

import de.mark225.bluebridge.core.config.BlueBridgeConfig;
import de.mark225.bluebridge.griefprevention.BlueBridgeGP;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimExtendEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimResizeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.logging.Level;

public class GriefPreventionListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClaimCreated(ClaimCreatedEvent e) {
        if (BlueBridgeConfig.debug())
            BlueBridgeGP.getInstance().getLogger().log(Level.INFO, "Claim created " + e.getClaim().getID());
        if (e.isCancelled()) return;
        scheduleUpdate(e.getClaim());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClaimResize(ClaimResizeEvent e) {
        if (BlueBridgeConfig.debug())
            BlueBridgeGP.getInstance().getLogger().log(Level.INFO, "Claim resized " + e.getFrom().getID());
        if (e.isCancelled()) return;
        scheduleUpdate(e.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClaimExtend(ClaimExtendEvent e) {
        // Wenn das Event gecancelt ist, macht ein Update sowieso keinen Sinn
        if (e.isCancelled()) {
            return;
        }

        // "Neue" Claim-Variante nach der Erweiterung
        Claim updated = e.getTo(); // statt e.getClaim()

        if (BlueBridgeConfig.debug()) {
            BlueBridgeGP.getInstance().getLogger().log(
                    Level.INFO,
                    "Claim extended " + updated.getID()
            );
        }

        // Zum Root-Claim hochlaufen (wie vorher)
        Claim root = updated;
        while (root.parent != null) {
            root = root.parent;
        }

        // Root-Claim an deine Update-Logik weitergeben
        scheduleUpdate(root);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClaimDelete(ClaimDeletedEvent e) {
        if (BlueBridgeConfig.debug())
            BlueBridgeGP.getInstance().getLogger().log(Level.INFO, "Claim deleted " + e.getClaim().getID());
        delete(e.getClaim());
    }

    public void delete(Claim claim) {
        BlueBridgeGP.getInstance().getGPIntegration().removeClaim(claim);
    }

    public void scheduleUpdate(Claim claim) {
        // Auf Folia: Statt Bukkit.getScheduler() den GlobalRegionScheduler nutzen,
        // damit der Code auf der "Hauptwelt-Region" im nächsten Tick läuft.
        Bukkit.getGlobalRegionScheduler().runDelayed(BlueBridgeGP.getInstance(), scheduledTask -> {
            Claim toUpdate = claim;
            if (!toUpdate.inDataStore) {
                return; // Claim noch nicht im DataStore -> nichts tun
            }

            // Zum Root-Claim hochlaufen
            while (toUpdate.parent != null) {
                toUpdate = toUpdate.parent;
            }

            // Claim an BlueBridge/BlueMap weiterreichen
            BlueBridgeGP.getInstance().getGPIntegration().addOrUpdateClaim(toUpdate);
        }, 1L); // 1 Tick Delay, damit GP den Claim sicher gespeichert hat
    }

}
