package com.blurr.voice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blurr.voice.data.TaskHistoryItem
import com.blurr.voice.utilities.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class MomentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: MomentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_moments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.task_history_recycler_view)
        emptyState = view.findViewById(R.id.empty_state)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MomentsAdapter(emptyList())
        recyclerView.adapter = adapter

        // Load task history
        loadTaskHistory()
    }

    private fun loadTaskHistory() {
        val raw = requireContext().getSharedPreferences("jarvis_task_history", android.content.Context.MODE_PRIVATE)
            .getString("tasks", "[]") ?: "[]"
        val items = mutableListOf<TaskHistoryItem>()
        val array = JSONArray(raw)
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val startedAt = item.optLong("startedAt", 0L).takeIf { it > 0 }?.let { java.util.Date(it) }
            val completedAt = item.optLong("completedAt", 0L).takeIf { it > 0 }?.let { java.util.Date(it) }
            items.add(TaskHistoryItem(
                task = item.optString("task"),
                status = item.optString("status"),
                startedAt = startedAt,
                completedAt = completedAt,
                success = if (item.has("success") && !item.isNull("success")) item.optBoolean("success") else null,
                errorMessage = item.optString("errorMessage").ifEmpty { null }
            ))
        }
        val sorted = items.sortedByDescending { it.startedAt ?: java.util.Date(0) }
        if (sorted.isNotEmpty()) showTaskHistory(sorted) else showEmptyState()
    }

    private fun showTaskHistory(taskHistory: List<TaskHistoryItem>) {
        adapter = MomentsAdapter(taskHistory)
        recyclerView.adapter = adapter
        recyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }
}
