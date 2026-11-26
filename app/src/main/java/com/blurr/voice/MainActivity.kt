package com.blurr.voice

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : BaseNavigationActivity() {

    companion object {
        const val ACTION_WAKE_WORD_FAILED = "com.blurr.voice.WAKE_WORD_FAILED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_content)

        findViewById<View>(R.id.tasks_left_tag)?.visibility = View.GONE
        findViewById<View>(R.id.pro_upgrade_banner)?.visibility = View.GONE
        findViewById<View>(R.id.pro_subscription_tag)?.visibility = View.GONE
        findViewById<View>(R.id.increase_limits_link)?.visibility = View.GONE
        findViewById<TextView>(R.id.unlocked_status_textview)?.text = "Unlocked"

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            Toast.makeText(this, "Jarvis is not launchable", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.delta_symbol)?.setOnClickListener {
            if (launchIntent != null && !ConversationalAgentService.isRunning) {
                ContextCompat.startForegroundService(this, Intent(this, ConversationalAgentService::class.java))
            }
        }

        findViewById<TextView>(R.id.btn_manage_permissions)?.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
        findViewById<TextView>(R.id.btn_set_default_assistant)?.setOnClickListener {
            startActivity(Intent(this, RoleRequestActivity::class.java))
        }
        findViewById<TextView>(R.id.disclaimer_link)?.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }
    }

    override fun getContentLayoutId(): Int = R.layout.activity_main_content

    override fun getCurrentNavItem(): NavItem = NavItem.HOME
}