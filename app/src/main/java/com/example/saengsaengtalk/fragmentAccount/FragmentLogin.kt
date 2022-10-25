package com.example.saengsaengtalk.fragmentAccount

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.saengsaengtalk.APIS.LoginModel
import com.example.saengsaengtalk.APIS.LoginResult
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class FragmentLogin :Fragment() {

    private var mBinding: FragLoginBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragLoginBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnPrevious.setOnClickListener {
            onBackPressed()
        }

        /** 로그인 */
        binding.btnLogin.setOnClickListener {
            /** 토큰 테스트 */
            /*val tk = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTY2Mzk4OTg4OTgyNiwibmlja19uYW1lIjoiYm9uZyJ9.FULK5UjhV7UnoRa8lUP7MrW0wccROJf9GUp7bac1tvo"
            val chunks = tk.split(".")
            val decoder = Base64.getUrlDecoder();
            val decode1 = decoder.decode(chunks[0])
            val decode2 = decoder.decode(chunks[1])
            val decode3 = decoder.decode(chunks[2])*/
            /** 토큰 테스트 */
            api.login(LoginModel(binding.etId.text.toString(), binding.etPw.text.toString())).enqueue(object: Callback<LoginResult> {
                override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                    Log.d("로그인", response.toString())
                    Log.d("로그인", response.body().toString())
                    Log.d("로그인응답 헤더", response.headers().toString())
                    val user_id = response.body()!!.user_id
                    println("유저 ID: ${response.body()!!.user_id}")
                    MainActivity.prefs.setString("Authentication", response.headers().get("Authentication").toString())
                    //MainActivity.prefs.setString("userId", response.headers().get("user_id").toString())
                    MainActivity.prefs.setString("userId", user_id.toString())
                }

                override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                    // 실패
                    Log.d("로그인",t.message.toString())
                    Log.d("로그인","fail")
                }
            })
        }

        binding.tvFindAccount.setOnClickListener {
            setFrag(FragmentFindAccount())
        }
        binding.tvSignUp.setOnClickListener { setFrag(FragmentSignUp())}
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}