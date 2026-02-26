package org.arun.multitool

import android.content.Context
import android.widget.Toast

class AndroidNotifier(private val context: Context): PlatformNotifier {
    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}