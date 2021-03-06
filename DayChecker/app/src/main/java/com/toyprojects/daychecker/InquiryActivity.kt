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
            else {
                finish()
                overridePendingTransition(R.anim.no_transition, R.anim.slide_down)
            }
        }

        // Set texts for inquiry-type spinner
        val typeList = listOf(getString(R.string.inquiry_type1), getString(R.string.inquiry_type2), getString(R.string.inquiry_type3), getString(R.string.inquiry_type4))
        val spnAdapter = ArrayAdapter<String>(this, R.layout.dropdown_item, typeList)
        binding.spnInquiryType.setAdapter(spnAdapter)

        // check is type selected & which type selected
        binding.spnInquiryType.setOnItemClickListener { _, _, position, _ ->
            isTypeSelected = true
            inquiryType = typeList[position]
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
                        binding.inputLayoutInquiryEmail.error = getString(R.string.wrong_email_format)
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

        binding.btnSendInquiry.setOnClickListener(btnSendListener)

    }

    private val btnSendListener = View.OnClickListener {
        // prevent multiple click
        binding.btnSendInquiry.isClickable = false

        binding.spnInquiryType.clearFocus()
        binding.inputLayoutInquiryEmail.clearFocus()
        binding.inputLayoutInquiryContext.clearFocus()

        // check internet connection first
        if (!isInternetConnected(this)) {
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show()
            binding.btnSendInquiry.isClickable = true
        }
        // check each input
        else if (!isTypeSelected) {
            binding.spnInquiryType.requestFocus()
            Toast.makeText(this, getString(R.string.select_inquiry_type), Toast.LENGTH_SHORT).show()
            binding.btnSendInquiry.isClickable = true
        } else if (userEmail.isEmpty()) {
            binding.inputLayoutInquiryEmail.requestFocus()
            Toast.makeText(this, getString(R.string.no_email_input), Toast.LENGTH_SHORT).show()
            binding.btnSendInquiry.isClickable = true
        } else if (!isEmailValid) {
            binding.inputLayoutInquiryEmail.requestFocus()
            Toast.makeText(this, getString(R.string.wrong_email_format), Toast.LENGTH_SHORT).show()
            binding.btnSendInquiry.isClickable = true
        } else if (inquiryContext.isEmpty()) {
            binding.inputLayoutInquiryContext.requestFocus()
            Toast.makeText(this, getString(R.string.no_inquiry_input), Toast.LENGTH_SHORT).show()
            binding.btnSendInquiry.isClickable = true
        }
        // check is checkbox chekced
        else if (!binding.checkAgreement.isChecked) {
            binding.checkAgreement.setBackgroundColor(Color.parseColor("#FFA7A7"))
            Toast.makeText(this, getString(R.string.agree_to_terms), Toast.LENGTH_SHORT).show()
            binding.btnSendInquiry.isClickable = true
        }
        // if all valid, send data to Firebase
        else {
            // show progressbar
            binding.progressInquiry.visibility = View.VISIBLE

            val finalInquiry = hashMapOf(
                    "inquiryType" to inquiryType,
                    "userEmail" to userEmail,
                    "inquiryContext" to inquiryContext.replace("\n", "\\\\n"),
                    "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("UserInquiry")
                    .add(finalInquiry)
                    .addOnSuccessListener {
                        binding.progressInquiry.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.inquiry_complete), Toast.LENGTH_SHORT).show()
                        finish()
                        overridePendingTransition(R.anim.no_transition, R.anim.slide_down)
                    }
                    .addOnFailureListener {
                        binding.progressInquiry.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.inquiry_error), Toast.LENGTH_SHORT).show()
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

        builder.setMessage(getString(R.string.cancel_inquiry_confirm))
                .setPositiveButton(getString(R.string.delete_record)) { _, _ ->
                    finish()
                    overridePendingTransition(R.anim.no_transition, R.anim.slide_down)
                }
                .setNegativeButton(getString(R.string.cancel_text), null)
                .setCancelable(true)

        builder.create().show()
    }

    override fun onBackPressed() {
        if (isEmailValid || inquiryContext.isNotEmpty()) { showEndMsg() }
        else {
            finish()
            overridePendingTransition(R.anim.no_transition, R.anim.slide_down)
        }
    }
}