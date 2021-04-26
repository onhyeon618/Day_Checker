package com.toyprojects.daychecker

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.toyprojects.daychecker.databinding.ActivityInquiryBinding

class InquiryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInquiryBinding

    private var isTypeSelected = false
    private var isEmailValid = false
    
    private var inquiryType = ""
    private var userEmail = ""
    private var inquiryContext = ""

    private var clickable = true

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button);
        topAppBar.setBackgroundColor(255)
        topAppBar.setTitle(R.string.inquiries)

        topAppBar.setNavigationOnClickListener {
            if (isEmailValid || inquiryContext.isNotEmpty()) { showEndMsg() }
            else { finish() }
        }

        // Set texts for inquiry-type spinner
        val typeList = listOf("어플 이용 문의", "오류 관련 문의", "데이터 삭제/백업 문의", "기타 문의")
        val spnAdapter = ArrayAdapter<String>(this, R.layout.dropdown_item, typeList)
        binding.spnInquiryType.setAdapter(spnAdapter)

        // check is type selected & which type selected
        binding.spnInquiryType.setOnItemClickListener { _, _, position, _ ->
            isTypeSelected = true
            inquiryType = typeList.get(position)
        }

        // check email input validation
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        binding.inputLayoutInquiryEmail.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // remove error message
                    binding.inputLayoutInquiryEmail.error = null
                    binding.inputLayoutInquiryEmail.isErrorEnabled = false
                } else {
                    userEmail = binding.txtInquiryEmail.text.toString().trim()
                    // check input pattern
                    if (userEmail.matches(emailPattern.toRegex())) {
                        isEmailValid = true
                    } else {
                        isEmailValid = false
                        binding.inputLayoutInquiryEmail.error = "이메일 형식이 잘못되었습니다."
                    }
                }
            }

        // get inquiry context data
        binding.inputLayoutInquiryContext.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    inquiryContext = binding.txtInquiryContext.text.toString().trim()
                }
            }

        // remove error color when checked
        binding.checkAgreement.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) view.setBackgroundColor(Color.parseColor("#00FF0000"))
        }

        binding.button.setOnClickListener(btnSendListener)

    }

    private val btnSendListener = View.OnClickListener {
        // prevent multiple click
        clickable = false

        binding.spnInquiryType.clearFocus()
        binding.inputLayoutInquiryEmail.clearFocus()
        binding.inputLayoutInquiryContext.clearFocus()

        // check internet connection first
        if (!isInternetConnected(this)) {
            Toast.makeText(this, "인터넷 연결을 먼저 확인해주세요.", Toast.LENGTH_SHORT).show()
            clickable = true
        }
        // check each input
        else if (!isTypeSelected) {
            binding.spnInquiryType.requestFocus()
            Toast.makeText(this, "문의 유형을 선택하세요.", Toast.LENGTH_SHORT).show()
            clickable = true
        }
        else if (userEmail.isEmpty()) {
            binding.inputLayoutInquiryEmail.requestFocus()
            Toast.makeText(this, "이메일 주소를 작성하세요.", Toast.LENGTH_SHORT).show()
            clickable = true
        }
        else if (!isEmailValid) {
            binding.inputLayoutInquiryEmail.requestFocus()
            Toast.makeText(this, "잘못된 이메일 형식입니다.", Toast.LENGTH_SHORT).show()
            clickable = true
        }
        else if (inquiryContext.isEmpty()) {
            binding.inputLayoutInquiryContext.requestFocus()
            Toast.makeText(this, "문의 내용을 작성하세요.", Toast.LENGTH_SHORT).show()
            clickable = true
        }
        // check is checkbox chekced
        else if (!binding.checkAgreement.isChecked) {
            binding.checkAgreement.setBackgroundColor(Color.parseColor("#FFA7A7"))
            Toast.makeText(this, "정보 수집 동의가 필요합니다.", Toast.LENGTH_SHORT).show()
            clickable = true
        }
        // if all valid, send data to Firebase
        else {
            val finalInquiry = hashMapOf(
                "inquiryType" to inquiryType,
                "userEmail" to userEmail,
                "inquiryContext" to inquiryContext.replace("\n", "\\\\n"),
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("UserInquiry")
                .add(finalInquiry)
                .addOnSuccessListener {
                    Toast.makeText(this, "문의가 접수되었습니다. 감사합니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "문의 접수에 실패했습니다. 인터넷 연결을 확인하시고 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // function checking internet connection
    private fun isInternetConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = cm.activeNetwork ?: return false
            val actNetwork = cm.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val nwInfo = cm.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }

    // show AlertDialog when user tries to cancel it with text written
    private fun showEndMsg() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("작성하신 내용이 삭제됩니다. 계속하시겠습니까?")
                .setPositiveButton("삭제") { _, _ -> finish() }
                .setNegativeButton("취소", null)
                .setCancelable(true)

        builder.create().show()
    }

    override fun onBackPressed() {
        if (isEmailValid || inquiryContext.isNotEmpty()) { showEndMsg() }
        else { finish() }
    }
}