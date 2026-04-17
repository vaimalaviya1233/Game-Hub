package com.fedeveloper95.games.services

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fedeveloper95.games.elements.UI.ControllerBottomSheet
import com.fedeveloper95.games.elements.ui.GameHubTheme

class BluetoothControllerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
            val prefs = context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("test_controller_features", false)) return

            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val deviceName = try {
                device?.name ?: "Controller"
            } catch (e: SecurityException) {
                "Controller"
            }

            val isController = deviceName.contains("Xbox", true) ||
                    deviceName.contains("Wireless Controller", true) ||
                    deviceName.contains("DualSense", true) ||
                    deviceName.contains("PlayStation", true) ||
                    deviceName.contains("Joy-Con", true) ||
                    deviceName.contains("Pro Controller", true) ||
                    deviceName.contains("Gamepad", true)

            if (isController && Settings.canDrawOverlays(context)) {
                val overlayIntent = Intent(context, ControllerOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("CONTROLLER_NAME", deviceName)
                }
                context.startActivity(overlayIntent)
            }
        }
    }
}

class ControllerOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val controllerName = intent.getStringExtra("CONTROLLER_NAME") ?: "Controller"

        setContent {
            GameHubTheme {
                ControllerBottomSheet(
                    controllerName = controllerName,
                    onDismiss = { finish() },
                    onSettingsClick = {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}