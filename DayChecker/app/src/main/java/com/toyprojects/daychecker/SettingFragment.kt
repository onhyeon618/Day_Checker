package com.toyprojects.daychecker

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.toyprojects.daychecker.database.RecordDB
import kotlinx.coroutines.runBlocking

class SettingFragment: PreferenceFragmentCompat() {
    private val currentVersion = BuildConfig.VERSION_NAME

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        val activity = context as Activity

        val pwdUsagePreference: SwitchPreferenceCompat? = findPreference("password_usage")
        val pwdResetPreference: Preference? = findPreference("reset_password")

        val dataExportPreference: Preference? = findPreference("export_data")
        val dataImportPreference: Preference? = findPreference("import_data")
        val dataResetPreference: Preference? = findPreference("reset_data")

        val appVersionPreference: Preference? = findPreference("app_version")
        val sendInquiryPreference: Preference? = findPreference("send_inquiry")
        val opensourceLicensePreference: Preference? = findPreference("opensource_license")

        // save current state for later use
        if (pwdUsagePreference != null) {
            pwdResetPreference?.isVisible = pwdUsagePreference.isChecked
        }

        // set ChangeListener on the switch of "비밀번호 사용하기" option
        pwdUsagePreference?.onPreferenceChangeListener=
            Preference.OnPreferenceChangeListener { _, newValue ->
                val enabled = newValue == true
                val intent = Intent(activity, LoginActivity::class.java)

                // start LoginActivity with state option
                if (enabled) {
                    intent.putExtra(AppLockState.varName, AppLockState.ENABLE_PWD)
                    startActivityForResult(intent, AppLockState.ENABLE_PWD)
                    activity.overridePendingTransition(R.anim.fadein, R.anim.no_transition)
                }
                else {
                    intent.putExtra(AppLockState.varName, AppLockState.REMOVE_PWD)
                    startActivityForResult(intent, AppLockState.REMOVE_PWD)
                    activity.overridePendingTransition(R.anim.fadein, R.anim.no_transition)
                }

                // set false to let states stay as before
                false
            }

        // onClickListener for "비밀번호 변경하기" option
        pwdResetPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.putExtra(AppLockState.varName, AppLockState.CHANGE_PWD)
            startActivityForResult(intent, AppLockState.CHANGE_PWD)
            activity.overridePendingTransition(R.anim.fadein, R.anim.no_transition)
            true
        }

        // "데이터 백업하기"
        dataExportPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            val intent = Intent(activity, DataExportActivity::class.java)
            intent.putExtra(DataBackupState.varName, DataBackupState.DATA_EXPORT)
            startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_up, R.anim.no_transition)
            true
        }

        // "데이터 복원하기"
        dataImportPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            val intent = Intent(activity, DataExportActivity::class.java)
            intent.putExtra(DataBackupState.varName, DataBackupState.DATA_IMPORT)
            startActivityForResult(intent, DataBackupState.DATA_IMPORT)
            activity.overridePendingTransition(R.anim.slide_up, R.anim.no_transition)
            true
        }

        // "데이터 초기화"
        dataResetPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            val builder = AlertDialog.Builder(activity)

            builder.setTitle(getString(R.string.data_reset))
            builder.setMessage(getString(R.string.db_reset_main))
                .setPositiveButton(getString(R.string.go_reset)) { _, _ ->
                    val roomdb = Room.databaseBuilder(
                        activity,
                        RecordDB::class.java, "dayCheckRecord"
                    ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build()

                    runBlocking {
                        roomdb.recordDao().deleteAll()
                    }

                    Toast.makeText(activity, getString(R.string.db_reset_complete), Toast.LENGTH_SHORT).show()

                    val main = Intent()
                    main.putExtra("dataReset", DataBackupState.DATA_RESET)
                    activity.setResult(RESULT_OK, main)
                }
                .setNegativeButton(getString(R.string.cancel_text), null)
                .setCancelable(true)

            builder.create().show()

            true
        }

        // "어플리케이션 버전"
        appVersionPreference?.summary = "v$currentVersion"
        // appVersionPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
        //     checkForUpdate()
        //     true
        // }

        // "문의 보내기"
        sendInquiryPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            startActivity(Intent(activity, InquiryActivity::class.java))
            activity.overridePendingTransition(R.anim.slide_up, R.anim.no_transition)
            true
        }

        // "오픈소스 라이선스"
        opensourceLicensePreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.opensource_license))
            startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
            activity.overridePendingTransition(R.anim.slide_up, R.anim.no_transition)
            true
        }
    }

    // 최종 테스트 후 적용 여부 결정 예정
    private fun checkForUpdate() {
        val activity = context as Activity

        val appUpdateManager = AppUpdateManagerFactory.create(activity)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Toast.makeText(activity, getString(R.string.new_version_available), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, getString(R.string.is_newest_version), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val activity = context as Activity

        val pwdUsagePreference: SwitchPreferenceCompat? = findPreference("password_usage")
        val pwdResetPreference: Preference? = findPreference("reset_password")

        if(resultCode == RESULT_OK) {
            when(requestCode) {
                AppLockState.ENABLE_PWD -> {
                    pwdUsagePreference?.isChecked = true
                    pwdResetPreference?.isVisible = true
                    Toast.makeText(activity, getString(R.string.pwd_is_enabled), Toast.LENGTH_SHORT).show()
                }
                AppLockState.REMOVE_PWD -> {
                    pwdUsagePreference?.isChecked = false
                    pwdResetPreference?.isVisible = false
                    Toast.makeText(activity, getString(R.string.pwd_is_disabled), Toast.LENGTH_SHORT).show()
                }
                AppLockState.CHANGE_PWD -> {
                    Toast.makeText(activity, getString(R.string.pwd_is_changed), Toast.LENGTH_SHORT).show()
                }
                DataBackupState.DATA_IMPORT -> {
                    val main = Intent()
                    main.putExtra("dataImport", DataBackupState.DATA_IMPORT)
                    activity.setResult(RESULT_OK, main)
                }
            }
        }
    }
}