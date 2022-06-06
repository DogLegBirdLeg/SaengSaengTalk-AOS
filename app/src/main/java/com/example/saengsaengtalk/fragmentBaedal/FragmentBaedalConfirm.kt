package com.example.saengsaengtalk.fragmentBaedal

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalConfirmBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

class FragmentBaedalConfirm :Fragment() {
    var postNum: String? = null
    var storeName: String? = null
    var baedalFee = 0
    var member = 1
    var opt: JSONArray? = null
    var optInfo: JSONArray? = null

    var orderPrice = 0
    var countChanged = mutableMapOf<String, Int>()
    val dec = DecimalFormat("#,###")

    var menu = mutableListOf<BaedalConfirmMenu>()

    private var mBinding: FragBaedalConfirmBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postNum = it.getString("postNum")
            storeName = it.getString("storeName")
            baedalFee = it.getString("baedalFee")!!.toInt()
            member = it.getString("member")!!.toInt()
            opt = JSONArray(it.getString("opt"))
            optInfo = JSONArray(it.getString("info"))
        }
        println("스토어이름: ${storeName}")
        println("메뉴: ${opt}")

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        var bundle = bundleOf("countChanged" to JSONObject(countChanged as Map<*, *>).toString(),
            "optInfo" to optInfo.toString())
        binding.btnPrevious.setOnClickListener {
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("fromConfirmFrag", bundle)
            onBackPressed()
        }
        binding.tvStoreName.text = storeName

        binding.rvMenuSelected.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSelected.setHasFixedSize(true)

        for (i in 0 until opt!!.length()) {
            val obj = opt!!.getJSONObject(i)
            val menuName = obj.getString("menuName")
            val price = obj.getInt("price")
            val count = obj.getInt("count")

            val strings = mutableListOf<BaedalConfirm>()
            val stringArray = obj.getJSONArray("optString")
            for (j in 0 until stringArray.length()) {
                strings.add(BaedalConfirm(stringArray.getString(j)))
            }

            menu.add(BaedalConfirmMenu(menuName, price, count, strings))
            orderPrice += price * count
        }
        val adapter = BaedalConfirmMenuAdapter(requireContext(), menu)
        binding.rvMenuSelected.adapter = adapter

        adapter.setItemClickListener(object: BaedalConfirmMenuAdapter.OnItemClickListener {
            override fun onChange(position: Int, price:Int, change: String) {
                var correction = 0
                var tempObj = JSONObject()
                var tempInfo = JSONArray()

                if (change == "remove") {
                    menu[position].count = 0
                    orderPrice -= price
                }
                else if (change == "sub") {
                    menu[position].count -= 1
                    orderPrice -= price
                }
                else {
                    menu[position].count += 1
                    orderPrice += price
                }

                bindSetText(orderPrice)
                countChanged[(position + correction).toString()] = menu[position].count

                for (i in 0 until optInfo!!.length()) {
                    if (i == position) {
                        val opt = optInfo!!.getJSONObject(i)
                        tempObj = JSONObject(mapOf("menuName" to opt.getString("menuName"),
                            "id" to opt.getInt("id"), "radio" to opt.getJSONArray("radio"),
                            "combo" to opt.getJSONArray("combo"), "price" to opt.getInt("price"),
                            "count" to menu[position].count))
                        tempInfo.put(tempObj)
                    } else tempInfo.put(optInfo!!.getJSONObject(i))
                }
                optInfo = tempInfo

                bundle = bundleOf("countChanged" to JSONObject(countChanged as Map<*, *>).toString(),
                    "optInfo" to optInfo.toString())

                var confirmAble = false
                for (i in menu) {
                    if (i.count > 0) {
                        confirmAble = true
                        break
                    }
                }
                if (!confirmAble) {
                    binding.btnConfirm.setEnabled(false)
                    binding.btnConfirm.setBackgroundResource(R.drawable.btn_baedal_confirm_false)
                }
            }
        })

        binding.lytAddMenu.setOnClickListener {
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("fromConfirmFrag", bundle)
            onBackPressed()
        }

        bindSetText(orderPrice)

        binding.btnConfirm.setOnClickListener {
            setFrag(FragmentBaedalPost(), mapOf("postNum" to postNum!!))
        }
    }

    fun bindSetText(orderPrice: Int) {
        binding.tvOrderPrice.text = "${dec.format(orderPrice)}원"
        binding.tvBaedalFee.text = "${dec.format(baedalFee/(member + 1))}원"
        binding.tvTotalPrice.text = "${dec.format(orderPrice + baedalFee/(member + 1))}원"
        binding.btnConfirm.text = "${dec.format(orderPrice + baedalFee/(member + 1))}원 주문하기"
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, 2)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}