package com.blurr.voice

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blurr.voice.data.UserMemory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.Date
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

class MemoriesFragment : Fragment() {

    private lateinit var memoriesRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var addMemoryFab: FloatingActionButton
    private lateinit var memoriesAdapter: MemoriesAdapter

    private val memoriesPrefs by lazy { requireContext().getSharedPreferences("jarvis_memories", android.content.Context.MODE_PRIVATE) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        loadMemories()
    }

    private fun setupViews(view: View) {
        // Setup views
        memoriesRecyclerView = view.findViewById(R.id.memoriesRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        addMemoryFab = view.findViewById(R.id.addMemoryFab)

        // Setup privacy card click listener
        val privacyCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.privacyCard)
        privacyCard.setOnClickListener {
            val intent = Intent(requireContext(), PrivacyActivity::class.java)
            startActivity(intent)
        }

        // Setup FAB click listener
        addMemoryFab.setOnClickListener {
            showAddEditMemoryDialog(null)
        }
    }

    private fun setupRecyclerView() {
        memoriesAdapter = MemoriesAdapter(
            memories = emptyList(),
            onEditClick = { memory ->
                showAddEditMemoryDialog(memory)
            },
            onDeleteClick = { memory ->
                showDeleteConfirmationDialog(memory)
            }
        )

        memoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memoriesAdapter
        }

        // Setup swipe to delete
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val memory = memoriesAdapter.getMemoryAt(position)
                if (memory != null) {
                    showDeleteConfirmationDialog(memory)
                }
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(memoriesRecyclerView)
    }

    private fun loadMemories() {
        updateUI(getStoredMemories())
    }

    private fun getStoredMemories(): List<UserMemory> {
        val result = mutableListOf<UserMemory>()
        val raw = memoriesPrefs.getString("memories", "[]") ?: "[]"
        val array = JSONArray(raw)
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            result.add(UserMemory(
                id = item.optString("id"),
                text = item.optString("text"),
                source = item.optString("source", "User"),
                createdAt = java.util.Date(item.optLong("createdAt", System.currentTimeMillis()))
            ))
        }
        return result.sortedByDescending { it.createdAt }
    }

    private fun saveStoredMemories(memories: List<UserMemory>) {
        val array = JSONArray()
        memories.forEach { memory ->
            array.put(JSONObject()
                .put("id", memory.id)
                .put("text", memory.text)
                .put("source", memory.source)
                .put("createdAt", memory.createdAt.time))
        }
        memoriesPrefs.edit().putString("memories", array.toString()).apply()
    }

    private fun updateUI(memories: List<UserMemory>) {
        if (memories.isEmpty()) {
            memoriesRecyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "No memories yet.\nTap the + button to add your first memory!"
        } else {
            memoriesRecyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            memoriesAdapter.updateMemories(memories)
        }
    }

    private fun showAddEditMemoryDialog(existingMemory: UserMemory?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_memory, null)
        val memoryEditText = dialogView.findViewById<EditText>(R.id.memoryEditText)
        val saveButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.cancelButton)

        if (existingMemory != null) {
            memoryEditText.setText(existingMemory.text)
            saveButton.text = "Update"
        }

        // Enable/disable save button based on text input
        memoryEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveButton.isEnabled = !s.isNullOrBlank()
            }
        })

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        saveButton.setOnClickListener {
            val memoryText = memoryEditText.text.toString().trim()
            if (memoryText.isNotEmpty()) {
                if (existingMemory != null) {
                    updateMemory(existingMemory, memoryText)
                } else {
                    addMemory(memoryText)
                }
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addMemory(memoryText: String) {
        val memories = getStoredMemories().toMutableList()
        memories.add(0, UserMemory(
            id = UUID.randomUUID().toString(),
            text = memoryText,
            source = "User",
            createdAt = java.util.Date()
        ))
        saveStoredMemories(memories)
        loadMemories()
        Toast.makeText(requireContext(), "Memory added", Toast.LENGTH_SHORT).show()
    }

    private fun updateMemory(oldMemory: UserMemory, newText: String) {
        val memories = getStoredMemories().map {
            if (it.id == oldMemory.id) it.copy(text = newText) else it
        }
        saveStoredMemories(memories)
        loadMemories()
        Toast.makeText(requireContext(), "Memory updated", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog(memory: UserMemory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Memory")
            .setMessage("Are you sure you want to delete this memory?\n\n\"${memory.text}\"")
            .setPositiveButton("Delete") { _, _ ->
                deleteMemory(memory)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Restore the swiped item
                memoriesAdapter.notifyDataSetChanged()
            }
            .setCancelable(false)
            .show()
    }

    private fun deleteMemory(memory: UserMemory) {
        saveStoredMemories(getStoredMemories().filterNot { it.id == memory.id })
        loadMemories()
        showSnackbar("Memory deleted")
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
