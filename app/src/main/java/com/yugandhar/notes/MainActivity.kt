package com.yugandhar.notes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var db: NoteDatabase
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = NoteDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.recyclerViewNotes)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddNote)

        noteAdapter = NoteAdapter(mutableListOf()) { note ->
            showDeleteConfirmation(note)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = noteAdapter

        fabAdd.setOnClickListener {
            showAddNoteDialog()
        }

        loadNotes()
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            val notes = db.noteDao().getAllNotes()
            noteAdapter.updateNotes(notes)
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etNoteTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etNoteDescription)

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.noteDao().insert(Note(title = title, description = description))
                        loadNotes()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun showDeleteConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this?")
            .setPositiveButton("Yes") { dialog, _ ->
                lifecycleScope.launch {
                    db.noteDao().delete(note)
                    loadNotes()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
