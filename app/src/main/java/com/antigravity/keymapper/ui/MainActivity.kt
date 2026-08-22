package com.antigravity.keymapper.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.antigravity.keymapper.R
import com.antigravity.keymapper.databinding.ActivityMainBinding
import com.antigravity.keymapper.injection.AccessibilityTouchService
import com.antigravity.keymapper.injection.AdbSocketEngine
import com.antigravity.keymapper.service.FloatingOverlayService
import com.antigravity.keymapper.storage.KeymapStorage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storage: KeymapStorage
    private var adbEngineTest: AdbSocketEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = KeymapStorage(this)
        adbEngineTest = AdbSocketEngine()

        setupPermissions()
        setupGameSelection()
        setupProfileSelection()
        setupSensitivityControls()
        setupAdvancedFeatures()
        setupVisionControls()
        setupActionButtons()

        checkAndShowPermissionOnboarding()
    }

    private fun setupGameSelection() {
        val targetGames = storage.getTargetGames()
        binding.cbFreeFire.isChecked = targetGames.contains("com.dts.freefireth") || targetGames.contains("com.dts.freefiremax")
        binding.cbPubg.isChecked = targetGames.contains("com.vng.pubgmobile") || targetGames.contains("com.tencent.ig")
        binding.cbLienQuan.isChecked = targetGames.contains("com.garena.game.kgvn") || targetGames.contains("com.riotgames.league.wildriftvn")
        binding.cbCod.isChecked = targetGames.contains("com.vng.codmvn")

        binding.cbFreeFire.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                storage.addTargetGame("com.dts.freefireth")
                storage.addTargetGame("com.dts.freefiremax")
            } else {
                storage.removeTargetGame("com.dts.freefireth")
                storage.removeTargetGame("com.dts.freefiremax")
            }
        }

        binding.cbPubg.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                storage.addTargetGame("com.vng.pubgmobile")
                storage.addTargetGame("com.tencent.ig")
            } else {
                storage.removeTargetGame("com.vng.pubgmobile")
                storage.removeTargetGame("com.tencent.ig")
            }
        }

        binding.cbLienQuan.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                storage.addTargetGame("com.garena.game.kgvn")
                storage.addTargetGame("com.riotgames.league.wildriftvn")
            } else {
                storage.removeTargetGame("com.garena.game.kgvn")
                storage.removeTargetGame("com.riotgames.league.wildriftvn")
            }
        }

        binding.cbCod.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                storage.addTargetGame("com.vng.codmvn")
            } else {
                storage.removeTargetGame("com.vng.codmvn")
            }
        }

        binding.btnAddCustomGame.setOnClickListener {
            showInstalledAppsPicker()
        }
    }

    private fun showInstalledAppsPicker() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("game") }
            .sortedBy { pm.getApplicationLabel(it).toString() }

        val appNames = installedApps.map { pm.getApplicationLabel(it).toString() }.toTypedArray()
        val appPackages = installedApps.map { it.packageName }.toTypedArray()
        val checkedItems = BooleanArray(installedApps.size) { i -> storage.isGameTarget(appPackages[i]) }

        AlertDialog.Builder(this)
            .setTitle("Chọn Game Để Tự Động Hiện Keymap")
            .setMultiChoiceItems(appNames, checkedItems) { _, which, isChecked ->
                val pkg = appPackages[which]
                if (isChecked) {
                    storage.addTargetGame(pkg)
                } else {
                    storage.removeTargetGame(pkg)
                }
            }
            .setPositiveButton("Xong") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Đã cập nhật danh sách game kích hoạt keymap", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun checkAndShowPermissionOnboarding() {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true
        val hasAccessibility = AccessibilityTouchService.isServiceRunning()
        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!hasOverlay || !hasAccessibility || !hasNotification) {
            showPermissionOnboardingDialog()
        }
    }

    private fun showPermissionOnboardingDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permission_onboarding, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnOverlay = dialogView.findViewById<android.widget.Button>(R.id.btnGrantOverlay)
        val btnAccessibility = dialogView.findViewById<android.widget.Button>(R.id.btnGrantAccessibility)
        val btnNotification = dialogView.findViewById<android.widget.Button>(R.id.btnGrantNotification)
        val layoutNotification = dialogView.findViewById<android.view.View>(R.id.layoutNotificationPerm)
        val btnDone = dialogView.findViewById<android.widget.Button>(R.id.btnDonePermissions)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            layoutNotification.visibility = View.GONE
        }

        fun refreshDialogButtons() {
            val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true
            val hasAccessibility = AccessibilityTouchService.isServiceRunning()
            val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (hasOverlay) {
                btnOverlay.text = getString(R.string.btn_granted)
                btnOverlay.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_green))
            }
            if (hasAccessibility) {
                btnAccessibility.text = getString(R.string.btn_granted)
                btnAccessibility.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_green))
            }
            if (hasNotification) {
                btnNotification.text = getString(R.string.btn_granted)
                btnNotification.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_green))
            }
        }

        btnOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        btnAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        btnNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        btnDone.setOnClickListener {
            dialog.dismiss()
            refreshStatus()
        }

        refreshDialogButtons()
        dialog.show()
    }

    private fun setupVisionControls() {
        binding.switchVision.setOnCheckedChangeListener { _, isChecked ->
            binding.rgVisionMode.visibility = if (isChecked) View.VISIBLE else View.GONE
            Toast.makeText(this, if (isChecked) "Đã bật Nhận Diện Hình Ảnh / Màu Sắc (Visual AI)" else "Đã tắt Visual AI", Toast.LENGTH_SHORT).show()
        }

        binding.rgVisionMode.setOnCheckedChangeListener { _, checkedId ->
            val modeName = when (checkedId) {
                R.id.rbAutoFire -> "Tự động Bắn khi vào tâm"
                R.id.rbAimAssist -> "Kéo tâm về mục tiêu"
                else -> "Bấm trực tiếp vào mục tiêu"
            }
            Toast.makeText(this, "Chế độ Visual AI: $modeName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdvancedFeatures() {
        binding.switchHumanize.setOnCheckedChangeListener { _, isChecked ->
            adbEngineTest?.enableHumanize = isChecked
            Toast.makeText(this, if (isChecked) "Đã bật Cảm ứng sinh học (Anti-AI)" else "Đã tắt Humanize", Toast.LENGTH_SHORT).show()
        }

        binding.switchRecoil.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Đã bật Ghìm tâm súng cơ học" else "Đã tắt Ghìm tâm", Toast.LENGTH_SHORT).show()
        }

        binding.switchGyro.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Đã bật Mô phỏng Con quay hồi chuyển (Gyro)" else "Đã tắt Gyro", Toast.LENGTH_SHORT).show()
        }

        binding.switchAutoProfile.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Đã bật Tự động đổi cấu hình theo game" else "Đã tắt Tự động đổi cấu hình", Toast.LENGTH_SHORT).show()
        }

        // Export JSON to local phone storage
        binding.btnExportJson.setOnClickListener {
            val exportFile = java.io.File(getExternalFilesDir(null), "Tx_Keymap_Config.json")
            val success = storage.exportToJsonFile(exportFile)
            if (success) {
                Toast.makeText(this, "Đã xuất cấu hình thành công:\n${exportFile.absolutePath}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Lỗi khi xuất file cấu hình", Toast.LENGTH_SHORT).show()
            }
        }

        // Import JSON from local phone storage
        binding.btnImportJson.setOnClickListener {
            val importFile = java.io.File(getExternalFilesDir(null), "Tx_Keymap_Config.json")
            if (!importFile.exists()) {
                Toast.makeText(this, "Không tìm thấy file ${importFile.name} trong bộ nhớ app", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val success = storage.importFromJsonFile(importFile)
            if (success) {
                refreshStatus()
                setupProfileSelection()
                Toast.makeText(this, "Đã nhập cấu hình .JSON thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Lỗi khi đọc file cấu hình .JSON", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun setupPermissions() {
        binding.switchOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
            }
        }

        binding.switchAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    private fun setupProfileSelection() {
        val activeId = storage.getActiveProfileId()
        when (activeId) {
            "moba_default" -> binding.rbMoba.isChecked = true
            "custom" -> binding.rbCustom.isChecked = true
            else -> binding.rbFps.isChecked = true
        }

        binding.rgProfiles.setOnCheckedChangeListener { _, checkedId ->
            val profileId = when (checkedId) {
                R.id.rbMoba -> "moba_default"
                R.id.rbCustom -> "custom"
                else -> "fps_default"
            }
            storage.setActiveProfileId(profileId)
            Toast.makeText(this, "Đã chọn hồ sơ: $profileId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSensitivityControls() {
        val sensX = (storage.getSensitivityX() * 50).toInt().coerceIn(1, 100)
        val sensY = (storage.getSensitivityY() * 50).toInt().coerceIn(1, 100)
        val globalSpeed = (storage.getGlobalSensitivity() * 100).toInt().coerceIn(10, 300)
        val scalePercent = (storage.getButtonScale() * 100).toInt().coerceIn(50, 200)
        val opacityPercent = (storage.getButtonOpacity() * 100).toInt().coerceIn(5, 100)

        binding.sbSensX.progress = sensX
        binding.sbSensY.progress = sensY
        binding.sbGlobalSpeed.progress = (globalSpeed - 50).coerceIn(0, 200)
        binding.sbButtonScale.progress = (scalePercent - 50).coerceIn(0, 150)
        binding.sbButtonOpacity.progress = opacityPercent

        binding.tvSensX.text = "Độ nhạy trục ngang (X): $sensX%"
        binding.tvSensY.text = "Độ nhạy trục dọc (Y): $sensY%"
        binding.tvGlobalSpeed.text = "Tốc độ chuột tổng thể: $globalSpeed%"
        binding.tvButtonScale.text = "Kích thước nút bấm & Icon: $scalePercent%"
        binding.tvButtonOpacity.text = "Độ mờ / Độ trong suốt nút (Full-View): $opacityPercent%"

        binding.sbGlobalSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = progress + 50
                binding.tvGlobalSpeed.text = "Tốc độ chuột tổng thể: $speed%"
                storage.setGlobalSensitivity(speed / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbButtonScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = progress + 50
                binding.tvButtonScale.text = "Kích thước nút bấm & Icon: $scale%"
                storage.setButtonScale(scale / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbButtonOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val opacity = progress.coerceAtLeast(5)
                binding.tvButtonOpacity.text = "Độ mờ / Độ trong suốt nút (Full-View): $opacity%"
                storage.setButtonOpacity(opacity / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbSensX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSensX.text = "Độ nhạy trục ngang (X): $progress%"
                storage.setSensitivityX(progress / 50.0f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbSensY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSensY.text = "Độ nhạy trục dọc (Y): $progress%"
                storage.setSensitivityY(progress / 50.0f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupActionButtons() {
        binding.btnToggleService.setOnClickListener {
            if (FloatingOverlayService.isRunning) {
                val intent = Intent(this, FloatingOverlayService::class.java).apply {
                    action = FloatingOverlayService.ACTION_STOP
                }
                stopService(intent)
                updateServiceButton(false)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Vui lòng cấp quyền Cửa sổ nổi trước", Toast.LENGTH_LONG).show()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                    return@setOnClickListener
                }

                val intent = Intent(this, FloatingOverlayService::class.java).apply {
                    action = FloatingOverlayService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                updateServiceButton(true)
                Toast.makeText(this, "Đã khởi chạy Keymapper! Mở game để bắt đầu.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAdbGuide.setOnClickListener {
            showAdbGuideDialog()
        }
    }

    private fun refreshStatus() {
        // Overlay permission
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        binding.switchOverlay.isChecked = hasOverlay

        // Accessibility
        binding.switchAccessibility.isChecked = AccessibilityTouchService.isServiceRunning()

        // ADB Status
        val isAdbActive = adbEngineTest?.isConnected() == true
        if (isAdbActive) {
            binding.tvAdbStatus.text = getString(R.string.adb_status_active)
            binding.tvAdbStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
        } else {
            binding.tvAdbStatus.text = getString(R.string.adb_status_inactive)
            binding.tvAdbStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_orange))
        }

        updateServiceButton(FloatingOverlayService.isRunning)
    }

    private fun updateServiceButton(running: Boolean) {
        if (running) {
            binding.btnToggleService.text = getString(R.string.btn_stop_service)
            binding.btnToggleService.setBackgroundResource(R.drawable.btn_neon_secondary)
        } else {
            binding.btnToggleService.text = getString(R.string.btn_start_service)
            binding.btnToggleService.setBackgroundResource(R.drawable.btn_neon_primary)
        }
    }

    private fun showAdbGuideDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_adb_guide, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.findViewById<View>(R.id.btnCloseGuide).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        adbEngineTest?.release()
    }
}
