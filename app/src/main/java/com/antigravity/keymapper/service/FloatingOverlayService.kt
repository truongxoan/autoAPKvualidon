package com.antigravity.keymapper.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.antigravity.keymapper.R
import com.antigravity.keymapper.injection.AccessibilityTouchService
import com.antigravity.keymapper.injection.AdbSocketEngine
import com.antigravity.keymapper.injection.TouchInjectionEngine
import com.antigravity.keymapper.input.KeyInputDispatcher
import com.antigravity.keymapper.model.KeyMapping
import com.antigravity.keymapper.model.KeyType
import com.antigravity.keymapper.model.KeymapProfile
import com.antigravity.keymapper.storage.KeymapStorage
import com.antigravity.keymapper.ui.KeymapOverlayView
import com.antigravity.keymapper.ui.MainActivity

class FloatingOverlayService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "keymapper_overlay_channel"

        var isRunning = false
            private set
        var instance: FloatingOverlayService? = null
            private set
    }

    private lateinit var windowManager: WindowManager
    private lateinit var storage: KeymapStorage
    private lateinit var inputDispatcher: KeyInputDispatcher
    private lateinit var activeProfile: KeymapProfile

    // Engines
    private var adbEngine: AdbSocketEngine? = null
    private var activeEngine: TouchInjectionEngine? = null

    // Overlay Views
    private var floatingMenuView: View? = null
    private var keymapOverlayView: KeymapOverlayView? = null
    private var menuParams: WindowManager.LayoutParams? = null
    private var overlayParams: WindowManager.LayoutParams? = null

    private var isEditMode = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        storage = KeymapStorage(this)

        // Initialize Engines (Prefer ADB, fallback to Accessibility)
        adbEngine = AdbSocketEngine()
        activeEngine = if (adbEngine?.isConnected() == true) {
            adbEngine
        } else {
            AccessibilityTouchService.instance ?: adbEngine
        }

        inputDispatcher = KeyInputDispatcher(activeEngine ?: adbEngine!!)
        updateScreenMetrics()

        val profileId = storage.getActiveProfileId()
        activeProfile = storage.loadProfile(profileId)
        inputDispatcher.setProfile(activeProfile)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        initFloatingMenu()
        initKeymapOverlay()
    }

    private fun updateScreenMetrics() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        inputDispatcher.setScreenMetrics(metrics.widthPixels, metrics.heightPixels, metrics.density)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Keymapper Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Dịch vụ gán phím chuột và bàn phím đang hoạt động"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AntiGravity Keymapper Đang Chạy")
            .setContentText("Bấm để mở cài đặt hoặc kéo biểu tượng nổi để chỉnh phím")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun initFloatingMenu() {
        floatingMenuView = LayoutInflater.from(this).inflate(R.layout.layout_floating_menu, null)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        val btnBall = floatingMenuView!!.findViewById<ImageButton>(R.id.btnFloatingBall)
        val layoutExpanded = floatingMenuView!!.findViewById<LinearLayout>(R.id.layoutExpandedMenu)
        val btnToggleMode = floatingMenuView!!.findViewById<Button>(R.id.btnToggleMode)
        val btnAddKey = floatingMenuView!!.findViewById<Button>(R.id.btnAddKey)
        val btnMacro = floatingMenuView!!.findViewById<Button>(R.id.btnMacro)
        val btnStealth = floatingMenuView!!.findViewById<Button>(R.id.btnStealth)
        val btnSaveMap = floatingMenuView!!.findViewById<Button>(R.id.btnSaveMap)
        val btnClose = floatingMenuView!!.findViewById<Button>(R.id.btnCloseFloating)

        // Dragging Floating Ball
        btnBall.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = menuParams!!.x
                        initialY = menuParams!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }
                        menuParams!!.x = initialX + dx
                        menuParams!!.y = initialY + dy
                        windowManager.updateViewLayout(floatingMenuView, menuParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            val isExpanded = layoutExpanded.visibility == View.VISIBLE
                            layoutExpanded.visibility = if (isExpanded) View.GONE else View.VISIBLE
                        }
                        return true
                    }
                }
                return false
            }
        })

        btnToggleMode.setOnClickListener {
            isEditMode = !isEditMode
            btnToggleMode.text = if (isEditMode) "Chơi Game" else "Chỉnh Phím"
            keymapOverlayView?.isEditMode = isEditMode
            updateOverlayFocusable(isEditMode)
        }

        btnAddKey.setOnClickListener {
            showAddKeyDialog()
        }

        // Macro Record & Playback Loop
        btnMacro.setOnClickListener {
            val macroManager = inputDispatcher.macroManager
            if (!macroManager.isRecording && !macroManager.isPlaying) {
                macroManager.startRecording("Macro Vòng Lặp", isInfinite = true)
                btnMacro.text = "⏹ Dừng Rec"
                btnMacro.setTextColor(android.graphics.Color.parseColor("#FF0055"))
            } else if (macroManager.isRecording) {
                val recorded = macroManager.stopRecording()
                if (recorded != null && recorded.steps.isNotEmpty()) {
                    btnMacro.text = "▶ Lặp Vô Hạn"
                    btnMacro.setTextColor(android.graphics.Color.parseColor("#00E676"))
                } else {
                    btnMacro.text = "⏺ Record"
                    btnMacro.setTextColor(android.graphics.Color.parseColor("#FF9100"))
                }
            } else if (macroManager.isPlaying) {
                macroManager.stopPlayback()
                btnMacro.text = "▶ Lặp Vô Hạn"
                btnMacro.setTextColor(android.graphics.Color.parseColor("#00E676"))
            } else {
                val metrics = resources.displayMetrics
                macroManager.playMacro(screenWidth = metrics.widthPixels, screenHeight = metrics.heightPixels)
                btnMacro.text = "⏹ Dừng Loop"
                btnMacro.setTextColor(android.graphics.Color.parseColor("#FF0055"))
            }
        }

        // Stealth / Ghost Mode toggle
        btnStealth.setOnClickListener {
            setStealthMode(true)
        }

        inputDispatcher.onStealthModeChanged = { isStealth ->
            setStealthMode(isStealth)
        }

        btnSaveMap.setOnClickListener {
            storage.saveProfile(activeProfile)
            layoutExpanded.visibility = View.GONE
        }

        btnClose.setOnClickListener {
            layoutExpanded.visibility = View.GONE
        }

        windowManager.addView(floatingMenuView, menuParams)
    }

    private fun setStealthMode(isStealth: Boolean) {
        floatingMenuView?.visibility = if (isStealth) View.GONE else View.VISIBLE
        keymapOverlayView?.alpha = if (isStealth) 0f else 1f
    }

    private fun initKeymapOverlay() {
        keymapOverlayView = KeymapOverlayView(this).apply {
            profile = activeProfile
            isEditMode = this@FloatingOverlayService.isEditMode
            buttonOpacity = storage.getButtonOpacity()
            buttonScale = storage.getButtonScale()
            onKeySelectedListener = { key ->
                showEditKeyDialog(key)
            }
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Default: FLAG_NOT_TOUCHABLE and FLAG_NOT_FOCUSABLE so it NEVER blocks user touch on screen!
        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        // Capture Keys and Pointer only when focused
        keymapOverlayView?.isFocusable = false
        keymapOverlayView?.isFocusableInTouchMode = false
        keymapOverlayView?.setOnKeyListener { _, _, event ->
            inputDispatcher.handleKeyEvent(event)
        }
        keymapOverlayView?.setOnGenericMotionListener { _, event ->
            inputDispatcher.handleMotionEvent(event)
        }

        // Initially hide until a target game is active
        keymapOverlayView?.visibility = View.GONE
        floatingMenuView?.visibility = View.GONE

        windowManager.addView(keymapOverlayView, overlayParams)
    }

    fun handleGlobalKeyEvent(event: KeyEvent): Boolean {
        return inputDispatcher.handleKeyEvent(event)
    }

    fun onForegroundAppChanged(packageName: String) {
        val isTarget = storage.isGameTarget(packageName)
        if (isTarget) {
            val autoProfile = com.antigravity.keymapper.service.AutoGameDetector.checkAndGetProfile(packageName)
            if (autoProfile != null) {
                switchProfile(autoProfile)
            }
        }
        // Always keep overlay visible while service is active (unless stealth mode)
        if (!inputDispatcher.isStealthMode) {
            keymapOverlayView?.visibility = View.VISIBLE
            floatingMenuView?.visibility = View.VISIBLE
        }
    }

    private fun updateOverlayFocusable(editMode: Boolean) {
        val params = overlayParams ?: return
        if (editMode) {
            // Edit Mode: Allow touching the overlay to drag buttons, but not blocking system keys
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            keymapOverlayView?.isFocusable = false
        } else {
            // Gaming / Passthrough Mode: Touch passes completely through to the game!
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            keymapOverlayView?.isFocusable = false
        }
        windowManager.updateViewLayout(keymapOverlayView, params)
    }

    private fun showAddKeyDialog() {
        val newKey = KeyMapping(
            label = "Phím Mới",
            type = KeyType.SINGLE_KEY,
            keyCode = KeyEvent.KEYCODE_SPACE,
            xPercent = 0.5f,
            yPercent = 0.5f,
            radiusDp = 28f
        )
        activeProfile.keys.add(newKey)
        keymapOverlayView?.invalidate()
        showEditKeyDialog(newKey)
    }

    private fun showEditKeyDialog(key: KeyMapping) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_key_edit, null)
        val dialogType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setType(dialogType)

        val etLabel = dialogView.findViewById<android.widget.EditText>(R.id.etKeyLabel)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgKeyType)
        val tvPrompt = dialogView.findViewById<TextView>(R.id.tvPressKeyPrompt)
        val cbContinuous = dialogView.findViewById<CheckBox>(R.id.cbContinuous)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDeleteKey)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmKey)

        etLabel.setText(key.label)
        cbContinuous.isChecked = key.isContinuous

        when (key.type) {
            KeyType.WASD_JOYSTICK -> dialogView.findViewById<RadioButton>(R.id.rbWasd).isChecked = true
            KeyType.MOUSE_FIRE -> dialogView.findViewById<RadioButton>(R.id.rbFire).isChecked = true
            KeyType.MOUSE_AIM -> dialogView.findViewById<RadioButton>(R.id.rbAim).isChecked = true
            KeyType.FPS_CAMERA -> dialogView.findViewById<RadioButton>(R.id.rbFpsCamera).isChecked = true
            KeyType.MOUSE_SCROLL_UP -> dialogView.findViewById<RadioButton>(R.id.rbScrollUp).isChecked = true
            KeyType.MOUSE_SCROLL_DOWN -> dialogView.findViewById<RadioButton>(R.id.rbScrollDown).isChecked = true
            KeyType.MOUSE_MIDDLE -> dialogView.findViewById<RadioButton>(R.id.rbScrollMiddle).isChecked = true
            KeyType.MACRO_TRIGGER -> dialogView.findViewById<RadioButton>(R.id.rbMacroTrigger).isChecked = true
            else -> dialogView.findViewById<RadioButton>(R.id.rbSingleKey).isChecked = true
        }

        var selectedKeyCode = key.keyCode

        dialogView.isFocusableInTouchMode = true
        dialogView.requestFocus()
        dialogView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                selectedKeyCode = keyCode
                val keyName = KeyEvent.keyCodeToString(keyCode).replace("KEYCODE_", "")
                tvPrompt.text = "✓ Đã chọn phím: $keyName"
                tvPrompt.setTextColor(android.graphics.Color.parseColor("#00E676"))
                if (etLabel.text.isNullOrBlank() || etLabel.text.toString() == "Phím Mới") {
                    etLabel.setText(keyName)
                }
                return@setOnKeyListener true
            }
            false
        }

        btnDelete.setOnClickListener {
            activeProfile.keys.remove(key)
            storage.saveProfile(activeProfile)
            keymapOverlayView?.invalidate()
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            key.label = etLabel.text.toString().ifBlank { "Nút" }
            key.isContinuous = cbContinuous.isChecked
            key.type = when (rgType.checkedRadioButtonId) {
                R.id.rbWasd -> KeyType.WASD_JOYSTICK
                R.id.rbFire -> KeyType.MOUSE_FIRE
                R.id.rbAim -> KeyType.MOUSE_AIM
                R.id.rbFpsCamera -> KeyType.FPS_CAMERA
                R.id.rbScrollUp -> KeyType.MOUSE_SCROLL_UP
                R.id.rbScrollDown -> KeyType.MOUSE_SCROLL_DOWN
                R.id.rbScrollMiddle -> KeyType.MOUSE_MIDDLE
                R.id.rbMacroTrigger -> {
                    key.keyCode = selectedKeyCode
                    KeyType.MACRO_TRIGGER
                }
                else -> {
                    key.keyCode = selectedKeyCode
                    KeyType.SINGLE_KEY
                }
            }

            storage.saveProfile(activeProfile)
            inputDispatcher.setProfile(activeProfile)
            keymapOverlayView?.invalidate()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun switchProfile(profileId: String) {
        if (activeProfile.id == profileId) return
        activeProfile = storage.loadProfile(profileId)
        storage.setActiveProfileId(profileId)
        inputDispatcher.setProfile(activeProfile)
        keymapOverlayView?.profile = activeProfile
        keymapOverlayView?.invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        instance = null
        inputDispatcher.reset()
        adbEngine?.release()
        try {
            floatingMenuView?.let { windowManager.removeView(it) }
            keymapOverlayView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
