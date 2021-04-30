package com.toyprojects.daychecker
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.toyprojects.daychecker.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var calledState = 0

    // handle input
    private var inputPwd = StringBuilder()
    private var inputLength = 0

    // will be used when setting new password (has to be compared)
    private var instantPwd = ""
    // will be used when changing password (if true, current pwd need to be input first)
    private var changePwd = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change the text on screen depend on the state
        calledState = intent.getIntExtra(AppLockState.varName, 0)
        when(calledState) {
            AppLockState.ENABLE_PWD -> binding.textPwdInfo.text = getString(R.string.pwd_new_input)
            AppLockState.REMOVE_PWD -> binding.textPwdInfo.text = getString(R.string.pwd_insert_current)
            AppLockState.CHANGE_PWD -> binding.textPwdInfo.text = getString(R.string.pwd_insert_current)
            AppLockState.START_APP -> binding.textPwdInfo.text = getString(R.string.pwd_insert_current)
        }

        // ArrayList to set onClickListeners easily
        val buttonArray = arrayListOf(binding.btnNumpad1, binding.btnNumpad2, binding.btnNumpad3, binding.btnNumpad4,
                                      binding.btnNumpad4, binding.btnNumpad5, binding.btnNumpad6, binding.btnNumpad7,
                                      binding.btnNumpad8, binding.btnNumpad9, binding.btnNumpad0, binding.btnNumpadErase)

        for (button in buttonArray){
            button.setOnClickListener(btnListener)
        }
    }

    private val btnListener = View.OnClickListener { view ->
        // ArrayLists of widgets for password input view
        val barArray = arrayListOf(binding.passwordBar1, binding.passwordBar2, binding.passwordBar3, binding.passwordBar4)
        val heartArray = arrayListOf(binding.heartIcon1, binding.heartIcon2, binding.heartIcon3, binding.heartIcon4)

        var enterin = -2
        when(view?.id) {
            R.id.btnNumpad1 -> enterin = 1
            R.id.btnNumpad2 -> enterin = 2
            R.id.btnNumpad3 -> enterin = 3
            R.id.btnNumpad4 -> enterin = 4
            R.id.btnNumpad5 -> enterin = 5
            R.id.btnNumpad6 -> enterin = 6
            R.id.btnNumpad7 -> enterin = 7
            R.id.btnNumpad8 -> enterin = 8
            R.id.btnNumpad9 -> enterin = 9
            R.id.btnNumpad0 -> enterin = 0
            R.id.btnNumpadErase -> enterin = -1
        }

        // if input is a number
        if (enterin != -1) {
            inputPwd.append(enterin.toString())
            inputLength += 1
            barArray.get(inputLength - 1).visibility = View.INVISIBLE   // hide bar-image
            heartArray.get(inputLength - 1).visibility = View.VISIBLE   // show heart-image

            // when input length reached four
            if (inputLength == 4) {
                handleInputByState(calledState)
            }
        }
        // if input is "erase"
        else {
            if (inputLength > 0) {
                inputLength -= 1
                inputPwd.deleteCharAt(inputLength)
                barArray.get(inputLength).visibility = View.VISIBLE
                heartArray.get(inputLength).visibility = View.INVISIBLE
            }
        }
    }

    private fun handleInputByState(state: Int) {
        // 일단 마지막 하트까지는 다 보여준 뒤에 다음 내용을 진행하고 싶은데...
        // finish() 할 때는 되는데 clearInput() 할 때만 마지막 하트가 안 보인다. 확인 필요.

        when(state) {
            // 1. Enable password(set new one): Request input again, compare, if same allow pwd usage
            AppLockState.ENABLE_PWD -> {
                // if it was the first input
                if (instantPwd.isEmpty()) {
                    // save current input instantly
                    instantPwd = inputPwd.toString()
                    // reset input
                    clearInput()

                    binding.textPwdInfo.text = getString(R.string.pwd_input_again)
                }
                else {
                    // if re-input is same as the before
                    if (instantPwd == inputPwd.toString()) {
                        App.prefs.setBoolean("pwd_usage", true)
                        App.prefs.setString("current_pwd", instantPwd)
                        setResult(RESULT_OK)
                        finish()
                    }
                    else {
                        instantPwd = ""
                        clearInput()
                        binding.textPwdInfo.text = getString(R.string.pwd_wrong_input)
                    }
                }
            }

            // 2. Disable password(delete): Request for input, if correct disable and delete pwd
            AppLockState.REMOVE_PWD -> {
                // if input is correct password
                if (inputPwd.toString() == App.prefs.getString("current_pwd", "")) {
                    App.prefs.setBoolean("pwd_usage", false)
                    App.prefs.setString("current_pwd", "")
                    setResult(RESULT_OK)
                    finish()
                }
                else {
                    clearInput()
                    binding.textPwdInfo.text = getString(R.string.pwd_wrong_input)
                }
            }

            // 3. Change password: Request for current pwd, if correct continue as #1 steps
            AppLockState.CHANGE_PWD -> {
                // current pwd need to be input first
                if (changePwd) {
                    if (inputPwd.toString() == App.prefs.getString("current_pwd", "")) {
                        changePwd = false
                        clearInput()
                        binding.textPwdInfo.text = getString(R.string.pwd_new_input)
                    }
                    else {
                        clearInput()
                        binding.textPwdInfo.text = getString(R.string.pwd_wrong_input)
                    }
                }
                // now change password
                else {
                    if (instantPwd.isEmpty()) {
                        instantPwd = inputPwd.toString()
                        clearInput()
                        binding.textPwdInfo.text = getString(R.string.pwd_input_again)
                    }
                    else {
                        if (instantPwd == inputPwd.toString()) {
                            App.prefs.setBoolean("pwd_usage", true)
                            App.prefs.setString("current_pwd", instantPwd)
                            setResult(RESULT_OK)
                            finish()
                        }
                        else {
                            instantPwd = ""
                            clearInput()
                            binding.textPwdInfo.text = getString(R.string.pwd_wrong_input)
                        }
                    }
                }
            }

            // 4. Screen loaded when opening the app -> ?
            AppLockState.START_APP -> {
                // if input is correct
                if (inputPwd.toString() == App.prefs.getString("current_pwd", "")) {
                    setResult(RESULT_OK)
                    finish()
                }
                else {
                    clearInput()
                    binding.textPwdInfo.text = getString(R.string.pwd_wrong_input)
                }
            }
        }
    }

    private fun clearInput() {
        val barArray = arrayListOf(binding.passwordBar1, binding.passwordBar2, binding.passwordBar3, binding.passwordBar4)
        val heartArray = arrayListOf(binding.heartIcon1, binding.heartIcon2, binding.heartIcon3, binding.heartIcon4)

        for (bar in barArray) { bar.visibility = View.VISIBLE }
        for (heart in heartArray) { heart.visibility = View.INVISIBLE }

        inputPwd.clear()
        inputLength = 0
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(AppLockState.varName, AppLockState.START_APP)
            }
            context.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        calledState = intent.getIntExtra(AppLockState.varName, 0)
        if (calledState == AppLockState.START_APP) {
            moveTaskToBack(true)
        }
        else {
            finish()
        }
    }
}