package com.example.saengsaengtalk.fragmentBaedal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import com.example.saengsaengtalk.databinding.FragBaedalPostBinding
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class FragmentBaedalMenu :Fragment() {
    var postNum: String? = null
    var storeId: String? = null

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
            storeId = it.getString("storeId")
        }

        Log.d("배달 메뉴", "게시물 번호: ${postNum}")
        Log.d("배달 메뉴", "스토어 id: ${storeId}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        return binding.root
    }

    fun refreshView() {
        var sectionMenu = mutableListOf<BaedalMenuSection>()
        var menuMap = mutableMapOf<String, MutableList<BaedalMenu>>()


        val assetManager = resources.assets
        val jsonString = assetManager.open("nene.json").bufferedReader().use { it.readText() }

        val jObject = JSONObject(jsonString)
        val jArray = jObject.getJSONArray("nene")

        for (i in 0 until jArray.length()) {
            val obj = jArray.getJSONObject(i)
            val id = obj.getInt("id")
            val section = obj.getString("section")
            val name = obj.getString("name")
            val radios = obj.getJSONArray("radio")
            val combos = obj.getJSONArray("combo")

            /* debug */

            Log.d("id: ${id} ", "${section}\t${name}")

            try {
                for (j in 0 until radios.length()) {
                    val radio = radios.getJSONObject(j)
                    val radio_area = radio.getString("area")
                    val radio_name = radio.getString("name")
                    val radio_price = radio.getString("price")
                    Log.d("id: ${id}  ${radio_area}", "${radio_name}\t${radio_price}")
                }
            } catch (e: JSONException) {
                Log.d("id: ${id} ", "라디오버튼 없음")
            }

            try {
                for (k in 0 until combos.length()) {
                    val combo = combos.getJSONObject(k)
                    val combo_area = combo.getString("area")
                    val combo_name = combo.getString("name")
                    val combo_price = combo.getString("price")
                    Log.d("id: ${id}  ${combo_area}", "${combo_name}\t${combo_price}")
                }
            } catch (e: JSONException) {
                Log.d("id: ${id} ", "콤보박스 없음")
            }

            /* 실사용 코드 */

            var min_price = 2147483647
            var max_price = 0
            for (i in 0 until radios.length()) {
                if (radios.getJSONObject(i).getString("area") == "가격") {
                    val radio_price = radios.getJSONObject(i).getString("price").toInt()
                    if (min_price > radio_price) min_price = radio_price
                    if (max_price < radio_price) max_price = radio_price
                }
            }
            val dec = DecimalFormat("#,###")
            val price =
                if (min_price == max_price) "${dec.format(min_price)}원"
                else "${dec.format(min_price)}~${dec.format(max_price)}원"

            var temp = BaedalMenu(name, price)
            if (section in menuMap.keys)
                menuMap[section]!!.add(temp)
            else
                menuMap[section] = mutableListOf(temp)

        }
        for ((key, value) in menuMap) {
            sectionMenu.add(BaedalMenuSection(key, value))
        }

        binding.rvMenu.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)

        val adapter = BaedalMenuSectionAdapter(requireContext(), sectionMenu)
        binding.rvMenu.adapter = adapter
        /*adapter.setItemClickListener(object: BaedalMenuSectionAdapter.OnItemClickListener{
            override fun onClick(v: View, menuName: String) {
                Log.d("제일 바깥", menuName)
                //setFrag(FragmentBaedalPost(), mapOf("postNum" to baedalList[position].postNum.toString()))
            }
        })*/
        adapter.addListener(object: BaedalMenuSectionAdapter.OnItemClickListener{
            override fun onClick(menuName: String) {
                //println(menuName)
                setFrag(FragmentBaedalMenuDetail(), mapOf("menuName" to menuName))
            }
        })

        adapter.notifyDataSetChanged()
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String?> = mapOf("none" to null)) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}