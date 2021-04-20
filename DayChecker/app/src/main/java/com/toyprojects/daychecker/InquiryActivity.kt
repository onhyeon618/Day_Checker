package com.toyprojects.daychecker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.toyprojects.daychecker.databinding.ActivityInquiryBinding
import com.toyprojects.daychecker.databinding.ActivitySettingsBinding

class InquiryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button);
        topAppBar.setTitle(R.string.inquiries)

        topAppBar.setNavigationOnClickListener {
            // 이후 작성 내용 있으면 Alert 띄우도록 수정 필요
             finish()
        }
    }
}