package com.peatral.anisync;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.peatral.anisync.graphql.Sync;

import java.util.Locale;

@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService
        extends TileService {

    /**
     * Called when the tile is added to the Quick Settings.
     *
     * @return TileService constant indicating tile state
     */
    @Override
    public void onTileAdded() {
        Sync.getInstance().setTileService(this);
        Log.d("QS", "Tile added");
    }

    /**
     * Called when this tile begins listening for events.
     */
    @Override
    public void onStartListening() {
        Sync.getInstance().setTileService(this);
        Log.d("QS", "Start listening");
        updateTile();
    }

    /**
     * Called when the user taps the tile.
     */
    @Override
    public void onClick() {
        Log.d("QS", "Tile tapped");

        Sync.getInstance().setTileService(this);
        if (!Sync.getInstance().isSyncing()) Sync.getInstance().sync();
        //else Sync.getInstance().reset(Sync.ID_STOPPED);
        updateTile();
    }

    /**
     * Called when this tile moves out of the listening state.
     */
    @Override
    public void onStopListening() {
        Log.d("QS", "Stop Listening");
    }

    /**
     * Called when the user removes this tile from Quick Settings.
     */
    @Override
    public void onTileRemoved() {
        Log.d("QS", "Tile removed");
        Sync.getInstance().setTileService(null);
    }

    // Changes the appearance of the tile.
    public void updateTile() {

        Tile tile = this.getQsTile();
        boolean isActive = getServiceStatus();

        Icon newIcon;
        String newLabel;
        int newState;

        // Change the tile to match the service status.
        if (isActive) {

            newLabel = String.format(Locale.US,
                    "%s %s",
                    getString(R.string.tile_label),
                    getString(R.string.service_active));

            newIcon = Icon.createWithResource(getApplicationContext(),
                    R.drawable.ic_fab_sync);

            newState = Tile.STATE_ACTIVE;

        } else {
            newLabel = String.format(Locale.US,
                    "%s %s",
                    getString(R.string.tile_label),
                    getString(R.string.service_inactive));

            newIcon =
                    Icon.createWithResource(getApplicationContext(),
                            R.drawable.ic_fab_sync);

            newState = Tile.STATE_INACTIVE;
        }

        // Change the UI of the tile.
        tile.setLabel(newLabel);
        tile.setIcon(newIcon);
        tile.setState(newState);

        // Need to call updateTile for the tile to pick up changes.
        tile.updateTile();
    }

    // Access storage to see how many times the tile
    // has been tapped.
    private boolean getServiceStatus() {
        return Sync.getInstance().isSyncing();
    }
}