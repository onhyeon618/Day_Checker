package com.toyprojects.daychecker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.toyprojects.daychecker.database.RecordDB
import com.toyprojects.daychecker.databinding.ActivityDataExportBinding
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.time.LocalDateTime

class DataExportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataExportBinding
    private lateinit var recordDB: RecordDB

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var userEmail = ""
    private var isEmailValid = false
    private var userPwd = ""
    private var isPwdValid = false

    private var clickable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button);
        topAppBar.setBackgroundColor(255)
        topAppBar.setTitle(R.string.data_export)

        topAppBar.setNavigationOnClickListener {
            finish()
        }

        // remove error color when checked
        binding.checkDataAgreement.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) view.setBackgroundColor(Color.parseColor("#00FF0000"))
        }

        // check email input validation
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        binding.inputLayoutExportEmail.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.inputLayoutExportEmail.error = null
                    binding.inputLayoutExportEmail.isErrorEnabled = false
                } else {
                    userEmail = binding.txtExportEmail.text.toString().trim()
                    if (userEmail.matches(emailPattern.toRegex())) {
                        isEmailValid = true
                    } else {
                        isEmailValid = false
                        binding.inputLayoutExportEmail.error = "이메일 형식이 잘못되었습니다."
                    }
                }
            }

        // check password input validation
        binding.inputLayoutExportPwd.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.inputLayoutExportPwd.error = null
                    binding.inputLayoutExportPwd.isErrorEnabled = false
                } else {
                    userPwd = binding.txtExportPwd.text.toString().trim()
                    if (userPwd.length >= 8 && userPwd.length <= 15) {
                        isPwdValid = true
                    } else {
                        isPwdValid = false
                        binding.inputLayoutExportPwd.error = "8~15자 이내로 입력하세요."
                    }
                }
            }

        binding.btnExportData.setOnClickListener(userDataExportListener)
    }

    private val userDataExportListener = View.OnClickListener {
        if (clickable) {
            clickable = false

            binding.txtExportEmail.clearFocus()
            binding.txtExportPwd.clearFocus()

            // check input validations
            if (!binding.checkDataAgreement.isChecked) {
                binding.checkDataAgreement.setBackgroundColor(Color.parseColor("#FFA7A7"))
                Toast.makeText(this, "정보 수집 동의가 필요합니다.", Toast.LENGTH_SHORT).show()
                clickable = true
            } else if (userEmail.isEmpty()) {
                binding.inputLayoutExportEmail.requestFocus()
                Toast.makeText(this, "이메일 주소를 작성하세요.", Toast.LENGTH_SHORT).show()
                clickable = true
            } else if (!isEmailValid) {
                binding.inputLayoutExportEmail.requestFocus()
                Toast.makeText(this, "잘못된 이메일 형식입니다.", Toast.LENGTH_SHORT).show()
                clickable = true
            } else if (userPwd.isEmpty()) {
                binding.inputLayoutExportPwd.requestFocus()
                Toast.makeText(this, "비밀번호를 작성하세요.", Toast.LENGTH_SHORT).show()
                clickable = true
            } else if (!isPwdValid) {
                binding.inputLayoutExportPwd.requestFocus()
                Toast.makeText(this, "비밀번호는 8~15자 이내로 입력하세요.", Toast.LENGTH_SHORT).show()
                clickable = true
            }
            else {
                recordDB = Room.databaseBuilder(
                    this,
                    RecordDB::class.java, "dayCheckRecord"
                ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build()

                exportDB()
                uploadDBtoStorage()
            }
        }
    }

    private fun exportDB() = runBlocking {
        recordDB.recordDao().checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))
    }

    private fun uploadDBtoStorage() {
        val fileName = "$userEmail+${LocalDateTime.now()}"

        val stream = FileInputStream(File(recordDB.openHelper.writableDatabase.path))
        val fileReference = storage.reference.child("user_db_backups/$fileName")

        // upload database file to firebase
        fileReference.putStream(stream).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            fileReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // get uploaded file's download url
                val downloadUri = task.result
                uploadDBtoFirestore(downloadUri.toString())
            } else {
                Toast.makeText(this, "오류가 발생했습니다. 인터넷 연결 확인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadDBtoFirestore(uploadedPath: String) {
        val exportItem = hashMapOf(
            "userPwd" to userPwd,   // 비밀번호에 해시 적용 필요
            "storagePath" to uploadedPath,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // upload certification information to firebase
        // set user email as document name to prevent multiple backup documents exists
        db.collection("UserDataBackup")
            .document(userEmail)
            .set(exportItem)
            .addOnSuccessListener {
                Toast.makeText(this, "데이터 백업이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "오류가 발생했습니다. 인터넷 연결 확인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
    }
}