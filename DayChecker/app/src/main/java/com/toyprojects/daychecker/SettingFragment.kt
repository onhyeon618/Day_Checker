import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.toyprojects.daychecker.AppLockState
import com.toyprojects.daychecker.LoginActivity
import com.toyprojects.daychecker.R

class SettingFragment: PreferenceFragmentCompat() {
    private var beforeChange = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        val pwdUsagePreference: SwitchPreferenceCompat? = findPreference("password_usage")
        val pwdResetPreference: Preference? = findPreference("reset_password")

        // save current state for later use
        if (pwdUsagePreference != null) {
            beforeChange = pwdUsagePreference.isChecked
            pwdResetPreference?.isVisible = pwdUsagePreference.isChecked
        }

        val intent = Intent(activity, LoginActivity::class.java)

        // set ChangeListener on the switch of "비밀번호 사용하기" option
        pwdUsagePreference?.onPreferenceChangeListener=
            Preference.OnPreferenceChangeListener { preference, newValue ->
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

                true
            }

        // onClickListener for "비밀번호 변경하기" option
        pwdResetPreference?.onPreferenceClickListener= Preference.OnPreferenceClickListener {
            intent.putExtra(AppLockState.varName, AppLockState.CHANGE_PWD)
            startActivityForResult(intent, AppLockState.CHANGE_PWD)
            true
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
        else {
            // when activity is closed with back button (=action canceled)
            pwdUsagePreference?.isChecked = beforeChange
            pwdResetPreference?.isVisible = beforeChange
        }
    }
}