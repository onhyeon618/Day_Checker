package com.toyprojects.daychecker

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.toyprojects.daychecker.databinding.ActivityInquiryBinding

class InquiryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button);
        topAppBar.setBackgroundColor(255)
        topAppBar.setTitle(R.string.inquiries)

        topAppBar.setNavigationOnClickListener {
            // 이후 작성 내용 있으면 Alert 띄우도록 수정 필요
             finish()
        }

        // Set texts for inquiry-type spinner
        val typeList = listOf("문의종류1", "문의종류2", "문의종류3", "기타")
        val spnAdapter = ArrayAdapter<String>(this, R.layout.dropdown_item, typeList)
        binding.spnInquiryType.setAdapter(spnAdapter)
    }
}