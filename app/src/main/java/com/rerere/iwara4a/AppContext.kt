package com.rerere.iwara4a

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.rerere.iwara4a.dao.AppDatabase
import com.rerere.iwara4a.util.createNotificationChannel
import com.rerere.iwara4a.util.debug
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory
import xyz.doikki.videoplayer.player.VideoViewConfig
import xyz.doikki.videoplayer.player.VideoViewManager
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class AppContext : Application() {
    companion object {
        lateinit var instance: Application
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val database by lazy {
            Room.databaseBuilder(
                instance,
                AppDatabase::class.java,
                "iwaradb"
            ).build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val startTime = System.currentTimeMillis()
        instance = this

        // 通知渠道
        createNotificationChannel("download", "download")

        // 初始化DKPlayer
        VideoViewManager.setConfig(
            VideoViewConfig.newBuilder()
                .setPlayerFactory(ExoMediaPlayerFactory.create())
                .build()
        )

        // 初始化日志框架
        XLog.init(
            LogConfiguration.Builder()
                .tag("iwara4a")
                .logLevel(
                    if (BuildConfig.DEBUG) LogLevel.ALL
                    else LogLevel.WARN
                )
                .enableThreadInfo()
                .build(),
            FilePrinter.Builder(filesDir.absolutePath + "/log")
                .fileNameGenerator(DateFileNameGenerator())
                .backupStrategy(NeverBackupStrategy())
                .cleanStrategy(FileLastModifiedCleanStrategy(TimeUnit.DAYS.toMillis(3)))
                .build()
        )

        XLog.i("APP初始化完成")
        debug {
            println("Boot Time: ${System.currentTimeMillis() - startTime}")
        }
    }
}

/**
 * 使用顶层函数直接获取 SharedPreference
 *
 * @param name SharedPreference名字
 * @return SharedPreferences实例
 */
fun sharedPreferencesOf(name: String): SharedPreferences =
    AppContext.instance.getSharedPreferences(name, Context.MODE_PRIVATE)