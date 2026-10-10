package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CrosshairRepository
import com.example.service.CrosshairService
import com.example.ui.components.ColorTab
import com.example.ui.components.CrosshairPreviewBox
import com.example.ui.components.PositionTab
import com.example.ui.components.PresetsTab
import com.example.ui.components.SettingsTab
import com.example.ui.components.StyleTab
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CrosshairMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrosshairMainScreen() {
    val context = LocalContext.current
    val repository = remember { CrosshairRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val config by repository.configFlow.collectAsStateWithLifecycle()
    val presets by repository.presetsFlow.collectAsStateWithLifecycle()
    val isServiceRunning by repository.isServiceRunning.collectAsStateWithLifecycle()
    val isDragCalibrationMode by repository.isDragCalibrationMode.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // 观察生命周期，当从系统设置页面返回时自动刷新权限状态
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Android 13+ 通知权限请求
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // 通知权限回调
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun toggleCrosshairService(enable: Boolean) {
        if (enable) {
            if (!Settings.canDrawOverlays(context)) {
                showPermissionDialog = true
                return
            }
            CrosshairService.startService(context)
            scope.launch {
                snackbarHostState.showSnackbar("准星悬浮窗已开启，已在屏幕中央就绪！")
            }
        } else {
            CrosshairService.stopService(context)
            scope.launch {
                snackbarHostState.showSnackbar("准星悬浮窗已关闭")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isServiceRunning) Color(0xFF00FF66) else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "极简准星",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (isServiceRunning) "已开启" else "已暂停",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isServiceRunning) Color(0xFF00FF66) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Switch(
                            checked = isServiceRunning,
                            onCheckedChange = { toggleCrosshairService(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("main_toggle_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val tabs = listOf(
                    Triple("样式", Icons.Default.Adjust, "nav_tab_style"),
                    Triple("色彩", Icons.Default.Palette, "nav_tab_color"),
                    Triple("校准", Icons.Default.ControlCamera, "nav_tab_position"),
                    Triple("方案", Icons.Default.Bookmark, "nav_tab_presets"),
                    Triple("设置", Icons.Default.Tune, "nav_tab_settings")
                )

                tabs.forEachIndexed { index, (label, icon, tag) ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { toggleCrosshairService(!isServiceRunning) },
                containerColor = if (isServiceRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isServiceRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("fab_toggle_service")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isServiceRunning) "关闭准星" else "开启准星"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isServiceRunning) "关闭悬浮" else "开启悬浮",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 顶部交互式实时靶场预览盒
            CrosshairPreviewBox(
                config = config,
                isServiceRunning = isServiceRunning,
                onResetCenter = {
                    repository.resetPosition()
                    scope.launch {
                        snackbarHostState.showSnackbar("准星已重置回正中心")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 主功能标签页内容
            when (selectedTabIndex) {
                0 -> StyleTab(
                    config = config,
                    onUpdateConfig = { repository.updateConfig(it) }
                )
                1 -> ColorTab(
                    config = config,
                    onUpdateConfig = { repository.updateConfig(it) }
                )
                2 -> PositionTab(
                    config = config,
                    isDragCalibrationMode = isDragCalibrationMode,
                    onToggleDragMode = { repository.setDragCalibrationMode(it) },
                    onResetPosition = { repository.resetPosition() },
                    onUpdateConfig = { repository.updateConfig(it) }
                )
                3 -> PresetsTab(
                    presets = presets,
                    currentConfig = config,
                    onApplyPreset = { preset ->
                        repository.setConfig(preset.config)
                        scope.launch {
                            snackbarHostState.showSnackbar("已应用「${preset.name}」方案")
                        }
                    },
                    onSaveCurrentAsPreset = { name ->
                        val ok = repository.saveCustomPreset(name)
                        if (ok) {
                            scope.launch {
                                snackbarHostState.showSnackbar("方案「$name」保存成功")
                            }
                        }
                    },
                    onDeletePreset = { id ->
                        repository.deleteCustomPreset(id)
                        scope.launch {
                            snackbarHostState.showSnackbar("方案已删除")
                        }
                    }
                )
                4 -> SettingsTab(
                    config = config,
                    hasOverlayPermission = hasOverlayPermission,
                    onRefreshPermission = {
                        hasOverlayPermission = Settings.canDrawOverlays(context)
                    },
                    onUpdateConfig = { repository.updateConfig(it) }
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // 预留底部浮动按钮高度
        }
    }

    // 悬浮窗未授权引导弹窗
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "需要悬浮窗权限",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "开启准星前，需要系统授予「在其他应用上层显示」权限，以便在FPS游戏等全屏界面实时显示准星。\n\n请在跳转后的设置页面中，找到「极简准星」并开启开关。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.testTag("dialog_grant_overlay_permission")
                ) {
                    Text("前往设置开启")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("稍后再说")
                }
            }
        )
    }
}
