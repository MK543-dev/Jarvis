package com.blurr.voice

import android.os.Bundle

class ProPurchaseActivity : BaseNavigationActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pro_purchase)
    }

    override fun getContentLayoutId(): Int = R.layout.activity_pro_purchase
    override fun getCurrentNavItem(): NavItem = NavItem.HOME
}