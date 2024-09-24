// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.utils

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager
import org.cytrus.cytrus_emu.BuildConfig
import org.cytrus.cytrus_emu.CytrusApplication
import org.cytrus.cytrus_emu.NativeLibrary
import org.cytrus.cytrus_emu.utils.PermissionsHandler.hasWriteAccess
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A service that spawns its own thread in order to copy several binary and shader files
 * from the Cytrus APK to the external file system.
 */
object DirectoryInitialization {
    private const val SYS_DIR_VERSION = "sysDirectoryVersion"

    @Volatile
    private var directoryState: DirectoryInitializationState? = null
    var userPath: String? = null
    val internalUserPath
        get() = CytrusApplication.appContext.getExternalFilesDir(null)!!.canonicalPath
    private val isCytrusDirectoryInitializationRunning = AtomicBoolean(false)

    val context: Context get() = CytrusApplication.appContext

    @JvmStatic
    fun start(): DirectoryInitializationState? {
        if (!isCytrusDirectoryInitializationRunning.compareAndSet(false, true)) {
            return null
        }

        if (directoryState != DirectoryInitializationState.CYTRUS_DIRECTORIES_INITIALIZED) {
            directoryState = if (hasWriteAccess(context)) {
                if (setCytrusUserDirectory()) {
                    CytrusApplication.documentsTree.setRoot(Uri.parse(userPath))
                    NativeLibrary.createLogFile()
                    NativeLibrary.logUserDirectory(userPath.toString())
                    NativeLibrary.createConfigFile()
                    GpuDriverHelper.initializeDriverParameters()
                    DirectoryInitializationState.CYTRUS_DIRECTORIES_INITIALIZED
                } else {
                    DirectoryInitializationState.CANT_FIND_EXTERNAL_STORAGE
                }
            } else {
                DirectoryInitializationState.EXTERNAL_STORAGE_PERMISSION_NEEDED
            }
        }
        isCytrusDirectoryInitializationRunning.set(false)
        return directoryState
    }

    private fun deleteDirectoryRecursively(file: File) {
        if (file.isDirectory) {
            for (child in file.listFiles()!!) {
                deleteDirectoryRecursively(child)
            }
        }
        file.delete()
    }

    @JvmStatic
    fun areCytrusDirectoriesReady(): Boolean {
        return directoryState == DirectoryInitializationState.CYTRUS_DIRECTORIES_INITIALIZED
    }

    fun resetCytrusDirectoryState() {
        directoryState = null
        isCytrusDirectoryInitializationRunning.compareAndSet(true, false)
    }

    val userDirectory: String?
        get() {
            checkNotNull(directoryState) {
                "DirectoryInitialization has to run at least once!"
            }
            check(!isCytrusDirectoryInitializationRunning.get()) {
                "DirectoryInitialization has to finish running first!"
            }
            return userPath
        }

    fun setCytrusUserDirectory(): Boolean {
        val dataPath = PermissionsHandler.cytrusDirectory
        if (dataPath.toString().isNotEmpty()) {
            userPath = dataPath.toString()
            android.util.Log.d("[Cytrus Frontend]", "[DirectoryInitialization] User Dir: $userPath")
            return true
        }
        return false
    }

    private fun copyAsset(asset: String, output: File, overwrite: Boolean, context: Context) {
        Log.debug("[DirectoryInitialization] Copying File $asset to $output")
        try {
            if (!output.exists() || overwrite) {
                val inputStream = context.assets.open(asset)
                val outputStream = FileOutputStream(output)
                copyFile(inputStream, outputStream)
                inputStream.close()
                outputStream.close()
            }
        } catch (e: IOException) {
            Log.error("[DirectoryInitialization] Failed to copy asset file: $asset" + e.message)
        }
    }

    private fun copyAssetFolder(
        assetFolder: String,
        outputFolder: File,
        overwrite: Boolean,
        context: Context
    ) {
        Log.debug("[DirectoryInitialization] Copying Folder $assetFolder to $outputFolder")
        try {
            var createdFolder = false
            for (file in context.assets.list(assetFolder)!!) {
                if (!createdFolder) {
                    outputFolder.mkdir()
                    createdFolder = true
                }
                copyAssetFolder(
                    assetFolder + File.separator + file, File(outputFolder, file),
                    overwrite, context
                )
                copyAsset(
                    assetFolder + File.separator + file, File(outputFolder, file), overwrite,
                    context
                )
            }
        } catch (e: IOException) {
            Log.error(
                "[DirectoryInitialization] Failed to copy asset folder: $assetFolder" +
                        e.message
            )
        }
    }

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }

    enum class DirectoryInitializationState {
        CYTRUS_DIRECTORIES_INITIALIZED,
        EXTERNAL_STORAGE_PERMISSION_NEEDED,
        CANT_FIND_EXTERNAL_STORAGE
    }
}
