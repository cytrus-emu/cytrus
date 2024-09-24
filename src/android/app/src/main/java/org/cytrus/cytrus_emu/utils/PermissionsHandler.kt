// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import androidx.documentfile.provider.DocumentFile
import org.cytrus.cytrus_emu.CytrusApplication

object PermissionsHandler {
    const val CYTRUS_DIRECTORY = "CYTRUS_DIRECTORY"
    val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(CytrusApplication.appContext)

    fun hasWriteAccess(context: Context): Boolean {
        try {
            if (cytrusDirectory.toString().isEmpty()) {
                return false
            }

            val uri = cytrusDirectory
            val takeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            val root = DocumentFile.fromTreeUri(context, uri)
            if (root != null && root.exists()) {
                return true
            }

            context.contentResolver.releasePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            Log.error("[PermissionsHandler]: Cannot check cytrus data directory permission, error: " + e.message)
        }
        return false
    }

    val cytrusDirectory: Uri
        get() {
            val directoryString = preferences.getString(CYTRUS_DIRECTORY, "")
            return Uri.parse(directoryString)
        }

    fun setCytrusDirectory(uriString: String?) =
        preferences.edit().putString(CYTRUS_DIRECTORY, uriString).apply()
}
