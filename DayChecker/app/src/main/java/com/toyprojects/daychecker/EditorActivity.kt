package com.toyprojects.daychecker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.toyprojects.daychecker.databinding.ActivityRecordEditorBinding

class EditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRecordEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button)
        topAppBar.setTitle(R.string.records)

        topAppBar.setNavigationOnClickListener {
            Toast.makeText(this, "Back pressed", Toast.LENGTH_SHORT).show()
            // finish()
        }
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save_record -> {
                    Toast.makeText(this, "Save pressed", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

    }
}