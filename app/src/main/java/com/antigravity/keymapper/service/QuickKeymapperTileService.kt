package com.antigravity.keymapper.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class QuickKeymapperTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        if (FloatingOverlayService.isRunning) {
            val stopIntent = Intent(this, FloatingOverlayService::class.java).apply {
                action = FloatingOverlayService.ACTION_STOP
            }
            startService(stopIntent)
        } else {
            val startIntent = Intent(this, FloatingOverlayService::class.java).apply {
                action = FloatingOverlayService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startIntent)
            } else {
                startService(startIntent)
            }
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        if (FloatingOverlayService.isRunning) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Tx: Đang Bật"
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Tx: Đã Tắt"
        }
        tile.updateTile()
    }
}
