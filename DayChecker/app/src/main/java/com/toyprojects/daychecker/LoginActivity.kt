package com.toyprojects.daychecker
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.toyprojects.daychecker.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var inputPwd = StringBuilder()
    private var inputLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            if (inputLength < 4) {   // 아직 입력 완료했을 때의 코드가 없어서 필요한 조건; 이후 삭제 예정
                inputPwd.append(enterin.toString())
                inputLength += 1
                barArray.get(inputLength - 1).visibility = View.INVISIBLE   // hide bar-image
                heartArray.get(inputLength - 1).visibility = View.VISIBLE   // show heart-image
            }
//            // when input length reached four
//            if (inputLength == 4) {
//                // 1. 입력값이 비밀번호와 동일하면 창을 종료하고 메인화면 진입
//                // 2. 입력값이 비밀번호와 다른 경우 입력을 리셋, 틀렸음을 알려주고 다시 입력 대기
//            }
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
}