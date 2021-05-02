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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.toyprojects.daychecker.AppLockState
import com.toyprojects.daychecker.LoginActivity
import com.toyprojects.daychecker.R
import com.toyprojects.daychecker.database.RecordDB
import kotlinx.coroutines.runBlocking

class SettingFragment: PreferenceFragmentCompat() {
    private var beforeChange = false

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        val activity = context as Activity

        val pwdUsagePreference: SwitchPreferenceCompat? = findPreference("password_usage")
        val pwdResetPreference: Preference? = findPreference("reset_password")

        val dataResetPreference: Preference? = findPreference("reset_data")

        val supportDeveloperPreference: Preference? = findPreference("support_developer")

        MobileAds.initialize(activity)
        loadInterstitialAd()

        // save current state for later use
        if (pwdUsagePreference != null) {
            beforeChange = pwdUsagePreference.isChecked
            pwdResetPreference?.isVisible = pwdUsagePreference.isChecked
        }

        val intent = Intent(activity, LoginActivity::class.java)

        // set ChangeListener on the switch of "비밀번호 사용하기" option
        pwdUsagePreference?.onPreferenceChangeListener=
            Preference.OnPreferenceChangeListener { _, newValue ->
                val enabled = newValue == true

                // start LoginActivity with state option
                if (enabled) {
                    intent.putExtra(AppLockState.varName, AppLockState.ENABLE_PWD)
                    startActivityForResult(intent, AppLockState.ENABLE_PWD)
                }
                else {
                    intent.putExtra(AppLockState.varName, AppLockState.REMOVE_PWD)
                    startActivityForResult(intent, AppLockState.REMOVE_PWD)
                }

                // set false to let states stay as before
                false
            }

        // onClickListener for "비밀번호 변경하기" option
        pwdResetPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            intent.putExtra(AppLockState.varName, AppLockState.CHANGE_PWD)
            startActivityForResult(intent, AppLockState.CHANGE_PWD)
            true
        }

        dataResetPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            val builder = AlertDialog.Builder(activity)

            builder.setTitle("데이터 초기화")
            builder.setMessage("기기에 저장된 모든 기록을 삭제하시겠습니까? 백업하지 않은 기록은 모두 지워지며, 다시 복구할 수 없습니다.")
                .setPositiveButton("초기화") { _, _ ->
                    val roomdb = Room.databaseBuilder(
                        activity,
                        RecordDB::class.java, "dayCheckRecord"
                    ).build()

                    runBlocking {
                        roomdb.recordDao().deleteAll()
                    }

                    Toast.makeText(activity, "모든 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                    val main = Intent()
                    main.putExtra("dataReset", 3002)
                    activity.setResult(RESULT_OK, main)
                }
                .setNegativeButton("취소", null)
                .setCancelable(true)

            builder.create().show()

            true
        }

        supportDeveloperPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            // Show full-screen google Ad
            showInterstitialAd()
            true
        }
    }

    private fun loadInterstitialAd() {
        val activity = context as Activity
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity, getString(R.string.sample_unit_id), adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    mInterstitialAd = null
                }
                override fun onAdLoaded(p0: InterstitialAd) {
                    mInterstitialAd = p0
                }
            }
        )
    }

    private fun showInterstitialAd() {
        val activity = context as Activity
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitialAd()
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError?) {
                    Toast.makeText(activity, "오류가 발생했습니다. 잠시 후 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                    mInterstitialAd = null
                }
                override fun onAdShowedFullScreenContent() {
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Toast.makeText(activity, "오류가 발생했습니다. 잠시 후 다시 시도하세요.", Toast.LENGTH_SHORT).show()
            loadInterstitialAd()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val pwdUsagePreference: SwitchPreferenceCompat? = findPreference("password_usage")
        val pwdResetPreference: Preference? = findPreference("reset_password")

        if(resultCode == RESULT_OK) {
            when(requestCode) {
                AppLockState.ENABLE_PWD -> {
                    pwdUsagePreference?.isChecked = true
                    pwdResetPreference?.isVisible = true
                    Toast.makeText(activity, "비밀번호가 설정되었습니다.", Toast.LENGTH_SHORT).show()
                    beforeChange = true   // need to be changed in case user enter LoginActivity again directly from current state
                }
                AppLockState.REMOVE_PWD -> {
                    pwdUsagePreference?.isChecked = false
                    pwdResetPreference?.isVisible = false
                    Toast.makeText(activity, "비밀번호가 해제되었습니다.", Toast.LENGTH_SHORT).show()
                    beforeChange = false
                }
                AppLockState.CHANGE_PWD -> {
                    Toast.makeText(activity, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}