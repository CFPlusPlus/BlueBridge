package de.mark225.bluebridge.griefprevention.addon.listener;

import de.mark225.bluebridge.core.config.BlueBridgeConfig;
import de.mark225.bluebridge.griefprevention.BlueBridgeGP;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimExtendEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimModifiedEvent;
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
    public void onClaimModified(ClaimModifiedEvent e) {
        if (BlueBridgeConfig.debug())
            BlueBridgeGP.getInstance().getLogger().log(Level.INFO, "Claim modified " + e.getFrom().getID());
        if (e.isCancelled()) return;
        scheduleUpdate(e.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClaimExtend(ClaimExtendEvent e) {
        if (BlueBridgeConfig.debug())
            BlueBridgeGP.getInstance().getLogger().log(Level.INFO, "Claim extended " + e.getClaim().getID());
        if (e.isCancelled()) return;
        Claim claim = e.getClaim();
        while (claim.parent != null)
            claim = claim.parent;
        scheduleUpdate(e.getClaim());
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
