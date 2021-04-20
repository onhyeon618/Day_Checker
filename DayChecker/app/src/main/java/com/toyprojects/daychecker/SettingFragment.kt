import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.toyprojects.daychecker.R

class SettingFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        val pwdUsagePreference: SwitchPreferenceCompat? = findPreference("password_usage")
        val pwdResetPreference: Preference? = findPreference("reset_password")

        // if "비밀번호 사용하기" option was checked, show "비밀번호 변경하기" option; if not don't
        if (pwdUsagePreference != null) {
            pwdResetPreference?.isVisible = pwdUsagePreference.isChecked
        }

        // set ChangeListener on the switch of "비밀번호 사용하기" option
        pwdUsagePreference?.onPreferenceChangeListener=object :Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                val enabled = newValue == true
                // set visibility of "비밀번호 변경하기" option accordingly
                pwdResetPreference?.isVisible = enabled
                return true
            }
        }
    }
}