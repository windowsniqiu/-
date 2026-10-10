package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.CrosshairRepository
import com.example.model.CrosshairConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 悬浮窗前台常驻服务
 * 确保在大型游戏（PUBG、COD、原神等）后台运行时不被杀掉，且零多余耗电
 */
class CrosshairService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: CrosshairOverlayView? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: CrosshairRepository

    override fun onCreate() {
        super.onCreate()
        repository = CrosshairRepository.getInstance(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForegroundServiceWithNotification()

        if (Settings.canDrawOverlays(this)) {
            initOverlayView()
            observeConfig()
        } else {
            stopSelf()
        }
    }

    private fun initOverlayView() {
        if (overlayView != null) return

        overlayView = CrosshairOverlayView(
            context = this,
            onDragOffsetChanged = { dx, dy ->
                repository.updateConfig {
                    it.copy(
                        offsetX = it.offsetX + dx,
                        offsetY = it.offsetY + dy
                    )
                }
            },
            onDragFinished = {
                // 拖动结束
            }
        )

        val density = resources.displayMetrics.density
        val config = repository.configFlow.value
        val initialSize = calculateWindowSize(config, density)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        if (!repository.isDragCalibrationMode.value && config.isTouchPassThrough) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        windowLayoutParams = WindowManager.LayoutParams(
            initialSize,
            initialSize,
            layoutFlag,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = config.offsetX
            y = config.offsetY
        }

        try {
            windowManager.addView(overlayView, windowLayoutParams)
            repository.setServiceRunning(true)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun calculateWindowSize(config: CrosshairConfig, density: Float): Int {
        val baseSize = (config.sizeDp * density * 2.4f).toInt()
        val minSize = (110 * density).toInt()
        return baseSize.coerceAtLeast(minSize)
    }

    private fun observeConfig() {
        serviceScope.launch {
            combine(
                repository.configFlow,
                repository.isDragCalibrationMode
            ) { config, isDragMode ->
                Pair(config, isDragMode)
            }.collect { (config, isDragMode) ->
                updateOverlay(config, isDragMode)
            }
        }
    }

    private fun updateOverlay(config: CrosshairConfig, isDragMode: Boolean) {
        val view = overlayView ?: return
        val params = windowLayoutParams ?: return
        val density = resources.displayMetrics.density

        val newSize = calculateWindowSize(config, density)
        params.width = newSize
        params.height = newSize
        params.x = config.offsetX
        params.y = config.offsetY

        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        if (!isDragMode && config.isTouchPassThrough) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        params.flags = flags

        view.updateConfig(config, isDragMode)
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            // View might be unattached
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_RESET_CENTER -> {
                repository.resetPosition()
            }
            ACTION_TOGGLE_DRAG -> {
                val current = repository.isDragCalibrationMode.value
                repository.setDragCalibrationMode(!current)
            }
            ACTION_START -> {
                if (overlayView == null && Settings.canDrawOverlays(this)) {
                    initOverlayView()
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, CrosshairService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resetIntent = Intent(this, CrosshairService::class.java).apply {
            action = ACTION_RESET_CENTER
        }
        val resetPendingIntent = PendingIntent.getService(
            this,
            2,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("极简准星 • 运行中")
            .setContentText("实时悬浮显示中 • 极低内存占用 • 游戏流畅")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "一键居中", resetPendingIntent)
            .addAction(0, "退出关闭", stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        overlayView = null
        repository.setServiceRunning(false)
        repository.setDragCalibrationMode(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "channel_crosshair_floating"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.crosshair.ACTION_START"
        const val ACTION_STOP = "com.example.crosshair.ACTION_STOP"
        const val ACTION_RESET_CENTER = "com.example.crosshair.ACTION_RESET_CENTER"
        const val ACTION_TOGGLE_DRAG = "com.example.crosshair.ACTION_TOGGLE_DRAG"

        fun startService(context: Context) {
            val intent = Intent(context, CrosshairService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, CrosshairService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
