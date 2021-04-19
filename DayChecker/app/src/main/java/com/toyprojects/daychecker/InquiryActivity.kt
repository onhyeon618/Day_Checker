package com.toyprojects.daychecker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.toyprojects.daychecker.databinding.ActivitySettingsBinding

class InquiryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button);
        topAppBar.setTitle(R.string.inquiries)

        topAppBar.setNavigationOnClickListener {
            Toast.makeText(this, "Back pressed", Toast.LENGTH_SHORT).show()
            // finish()
        }
    }
}