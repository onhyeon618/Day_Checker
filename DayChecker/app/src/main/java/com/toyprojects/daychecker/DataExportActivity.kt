package com.toyprojects.daychecker

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.toyprojects.daychecker.database.RecordDB
import com.toyprojects.daychecker.databinding.ActivityDataExportBinding
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DataExportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataExportBinding
    private lateinit var recordDB: RecordDB

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var userEmail = ""
    private var isEmailValid = false
    private var userPwd = ""
    private var isPwdValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topAppBar = binding.toolbar
        topAppBar.setNavigationIcon(R.drawable.back_button);
        topAppBar.setBackgroundColor(255)

        topAppBar.setNavigationOnClickListener {
            finish()
        }

        // change each view text based on called state
        when (intent.getIntExtra(DataBackupState.varName, 0)) {
            DataBackupState.DATA_EXPORT -> {
                topAppBar.setTitle(R.string.data_export)

                binding.txtTitle.text = getString(R.string.db_export_main)
                binding.txtDataInformations.text = getString(R.string.db_export_alert)
                binding.checkDataAgreement.isVisible = true
                binding.txtEmailInfo.text = getString(R.string.db_export_email)
                binding.txtPwdInfo.text = getString(R.string.db_export_pwd)
                binding.txtExportPwd.hint = getString(R.string.db_export_pwd_hint)
                binding.btnExportData.text = getString(R.string.db_export_btn)

                binding.btnExportData.setOnClickListener(userDataExportListener)
            }
            DataBackupState.DATA_IMPORT -> {
                topAppBar.setTitle(R.string.data_import)

                binding.txtTitle.text = getString(R.string.db_import_main)
                binding.txtDataInformations.text = getString(R.string.db_import_alert)
                binding.checkDataAgreement.isVisible = false
                binding.txtEmailInfo.text = getString(R.string.db_import_email)
                binding.txtPwdInfo.text = getString(R.string.db_import_pwd)
                binding.txtExportPwd.hint = getString(R.string.db_import_pwd_hint)
                binding.btnExportData.text = getString(R.string.db_import_btn)
                
                binding.btnExportData.setOnClickListener(userDataImportListener)
            }
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
    }

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

    private val userDataExportListener = View.OnClickListener {
        binding.btnExportData.isClickable = false

        binding.txtExportEmail.clearFocus()
        binding.txtExportPwd.clearFocus()

        // check input validations
        if (!isInternetConnected(this)) {
            Toast.makeText(this, "인터넷 연결을 먼저 확인해주세요.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (!binding.checkDataAgreement.isChecked) {
            binding.checkDataAgreement.setBackgroundColor(Color.parseColor("#FFA7A7"))
            Toast.makeText(this, "정보 수집 동의가 필요합니다.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (userEmail.isEmpty()) {
            binding.inputLayoutExportEmail.requestFocus()
            Toast.makeText(this, "이메일 주소를 작성하세요.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (!isEmailValid) {
            binding.inputLayoutExportEmail.requestFocus()
            Toast.makeText(this, "잘못된 이메일 형식입니다.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (userPwd.isEmpty()) {
            binding.inputLayoutExportPwd.requestFocus()
            Toast.makeText(this, "비밀번호를 작성하세요.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (!isPwdValid) {
            binding.inputLayoutExportPwd.requestFocus()
            Toast.makeText(this, "비밀번호는 8~15자 이내로 입력하세요.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else {
            binding.progressDataExport.visibility = View.VISIBLE

            recordDB = Room.databaseBuilder(
                this,
                RecordDB::class.java, "dayCheckRecord"
            ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build()

            exportDB()
            uploadDBtoStorage()
        }
    }

    private val userDataImportListener = View.OnClickListener {
        binding.btnExportData.isClickable = false

        binding.txtExportPwd.clearFocus()
        binding.txtExportEmail.clearFocus()

        // check input validations
        if (!isInternetConnected(this)) {
            Toast.makeText(this, "인터넷 연결을 먼저 확인해주세요.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (userEmail.isEmpty()) {
            binding.inputLayoutExportEmail.requestFocus()
            Toast.makeText(this, "이메일 주소를 작성하세요.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (!isEmailValid) {
            binding.inputLayoutExportEmail.requestFocus()
            Toast.makeText(this, "잘못된 이메일 형식입니다.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else if (userPwd.isEmpty()) {
            binding.inputLayoutExportPwd.requestFocus()
            Toast.makeText(this, "비밀번호를 작성하세요.", Toast.LENGTH_SHORT).show()
            binding.btnExportData.isClickable = true
        } else {
            binding.progressDataExport.visibility = View.VISIBLE

            recordDB = Room.databaseBuilder(
                this,
                RecordDB::class.java, "dayCheckRecord"
            ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build()

            downloadFromFirebase()
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
                binding.btnExportData.isClickable = true
                binding.progressDataExport.visibility = View.GONE
                Toast.makeText(this, "오류가 발생했습니다. 인터넷 연결 확인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadDBtoFirestore(uploadedPath: String) {
        val exportItem = hashMapOf(
            "userPwd" to getEncrypt(userPwd),
            "storagePath" to uploadedPath,
            "backupDate" to LocalDate.now().toString()
        )

        // upload certification information to firebase
        // set user email as document name to prevent multiple backup documents exists
        db.collection("UserDataBackup")
            .document(userEmail)
            .set(exportItem)
            .addOnSuccessListener {
                binding.progressDataExport.visibility = View.GONE
                Toast.makeText(this, "데이터 백업이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                binding.btnExportData.isClickable = true
                binding.progressDataExport.visibility = View.GONE
                Toast.makeText(this, "오류가 발생했습니다. 인터넷 연결 확인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadFromFirebase() {
        val docRef = db.collection("UserDataBackup").document(userEmail)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    if (document.data?.get("userPwd").toString() != getEncrypt(userPwd)) {
                        binding.progressDataExport.visibility = View.GONE
                        Toast.makeText(this, "잘못된 비밀번호입니다.", Toast.LENGTH_SHORT).show()
                        binding.btnExportData.isClickable = true
                    } else if (ChronoUnit.DAYS.between(LocalDate.parse(document.data?.get("backupDate").toString(), DateTimeFormatter.ISO_DATE), LocalDate.now()) > 7) {
                        binding.progressDataExport.visibility = View.GONE
                        Toast.makeText(this, "해당 이메일로 백업된 파일이 없거나 유효한 기간이 지났습니다.", Toast.LENGTH_SHORT).show()
                        binding.btnExportData.isClickable = true
                    } else {
                        val fileReference = storage.getReferenceFromUrl(document.data?.get("storagePath").toString())
                        val localFile = File.createTempFile("dcdata", "", this.cacheDir)

                        fileReference.getFile(localFile).addOnSuccessListener {
                            // data is returned from server
                            // replace device database file with the one got from server
                            localFile.copyTo(File(recordDB.openHelper.writableDatabase.path), true)
                            localFile.delete()

                            binding.progressDataExport.visibility = View.GONE
                            Toast.makeText(this, "복원이 완료되었습니다!", Toast.LENGTH_SHORT).show()

                            setResult(RESULT_OK)
                            finish()
                        }.addOnFailureListener {
                            binding.progressDataExport.visibility = View.GONE
                            Toast.makeText(this, "오류가 발생했습니다. 인터넷 연결 확인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            binding.btnExportData.isClickable = true
                        }
                    }
                } else {
                    binding.progressDataExport.visibility = View.GONE
                    Toast.makeText(this, "해당 이메일로 백업된 파일이 없거나 유효한 기간이 지났습니다.", Toast.LENGTH_SHORT).show()
                    binding.btnExportData.isClickable = true
                }
            }
            .addOnFailureListener { exception ->
                binding.progressDataExport.visibility = View.GONE
                Toast.makeText(this, "오류가 발생했습니다. 인터넷 연결 확인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                binding.btnExportData.isClickable = true
            }
    }

    private fun getEncrypt(plaintext : String) : String{
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(plaintext.toByteArray())

        val no = BigInteger(1, digest)
        var hashtext: String = no.toString(16)

        while (hashtext.length < 128) {
            hashtext = "0$hashtext"
        }
        return hashtext
    }
}