// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package org.cytrus.cytrus_emu.utils

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import org.cytrus.cytrus_emu.fragments.CytrusDirectoryDialogFragment
import org.cytrus.cytrus_emu.fragments.CopyDirProgressDialog
import org.cytrus.cytrus_emu.model.SetupCallback
import org.cytrus.cytrus_emu.viewmodel.HomeViewModel

/**
 * Cytrus directory initialization ui flow controller.
 */
class CytrusDirectoryHelper(private val fragmentActivity: FragmentActivity) {
    fun showCytrusDirectoryDialog(result: Uri, callback: SetupCallback? = null) {
        val cytrusDirectoryDialog = CytrusDirectoryDialogFragment.newInstance(
            fragmentActivity,
            result.toString(),
            CytrusDirectoryDialogFragment.Listener { moveData: Boolean, path: Uri ->
                val previous = PermissionsHandler.cytrusDirectory
                // Do noting if user select the previous path.
                if (path == previous) {
                    return@Listener
                }

                val takeFlags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                fragmentActivity.contentResolver.takePersistableUriPermission(
                    path,
                    takeFlags
                )
                if (!moveData || previous.toString().isEmpty()) {
                    initializeCytrusDirectory(path)
                    callback?.onStepCompleted()
                    val viewModel = ViewModelProvider(fragmentActivity)[HomeViewModel::class.java]
                    viewModel.setUserDir(fragmentActivity, path.path!!)
                    viewModel.setPickingUserDir(false)
                    return@Listener
                }

                // If user check move data, show copy progress dialog.
                CopyDirProgressDialog.newInstance(fragmentActivity, previous, path, callback)
                    ?.show(fragmentActivity.supportFragmentManager, CopyDirProgressDialog.TAG)
            })
        cytrusDirectoryDialog.show(
            fragmentActivity.supportFragmentManager,
            CytrusDirectoryDialogFragment.TAG
        )
    }

    companion object {
        fun initializeCytrusDirectory(path: Uri) {
            PermissionsHandler.setCytrusDirectory(path.toString())
            DirectoryInitialization.resetCytrusDirectoryState()
            DirectoryInitialization.start()
        }
    }
}
