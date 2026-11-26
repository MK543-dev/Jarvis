package com.blurr.voice

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MemoriesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memories)
        findViewById<TextView>(R.id.emptyStateText)?.text = "Memories are stored locally."
    }
}