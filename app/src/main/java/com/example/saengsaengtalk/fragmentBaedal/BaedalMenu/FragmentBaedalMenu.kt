package com.example.saengsaengtalk.fragmentBaedal.BaedalMenu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.Menu
import com.example.saengsaengtalk.APIS.OrderingModel
import com.example.saengsaengtalk.APIS.StoreInfo
import com.example.saengsaengtalk.APIS.UserOrder
import com.example.saengsaengtalk.LoopingDialog
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragBaedalMenuBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import com.example.saengsaengtalk.fragmentBaedal.BaedalOpt.FragmentBaedalOpt
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentBaedalMenu :Fragment() {
    var isPosting = false
    var postId = ""
    var currentMember = "0"
    var isUpdating = false
    var storeName = ""
    var storeId = "0"
    var baedalFee = ""

    var orders = JSONArray()                            // 주문 리스트
    val sections = mutableListOf<Section>()        // 스토어 메뉴 전체. 현재화면 구성에 사용

    private var mBinding: FragBaedalMenuBinding? = null
    private val binding get() = mBinding!!
    val api= APIS.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isPosting = it.getString("isPosting")!!.toBoolean()
            if (!isPosting) {
                postId = it.getString("postId")!!
                currentMember = it.getString("currentMember")!!
                isUpdating = it.getString("isUpdating")!!.toBoolean()
            }
            storeName = it.getString("storeName")!!
            storeId = it.getString("storeId")!!
            baedalFee = it.getString("baedalFee")!!
            //orders = JSONArray(it.getString("orders"))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    fun refreshView() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        getMenuData()
        if (isUpdating) getOrdersObject()

        /** Option frag에서 메뉴 선택 후 담기 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("order", this) { requestKey, bundle ->
            val orderString = bundle.getString("orderString")
            orders.put(JSONObject(orderString))

            setCartBtn()
        }

        /** Confirm frag에서 뒤로가기(메뉴 더담기) 시 작동 */
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("changeOrder", this) { requestKey, bundle ->
            val ordersString = bundle.getString("ordersString")
            orders = JSONArray(ordersString)

            setCartBtn()
        }

        binding.lytCart.setOnClickListener {cartOnClick()}
        binding.btnCart.setOnClickListener {cartOnClick()}
        setCartBtn()
    }

    fun getMenuData() {
        val loopingDialog = looping()
        api.getStoreInfo(storeId).enqueue(object : Callback<StoreInfo> {
            override fun onResponse(call: Call<StoreInfo>, response: Response<StoreInfo>) {
                if (response.code() == 200) {
                    val storeInfo = response.body()
                    setSections(storeInfo!!.menus)
                    mappingAdapter()
                } else {
                    Log.e("baedalMenu Fragment - getSectionMenu", response.toString())
                    makeToast("메뉴정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<StoreInfo>, t: Throwable) {
                Log.e("baedalMenu Fragment - getSectionMenu", t.message.toString())
                makeToast("메뉴정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                looping(false, loopingDialog)
            }
        })
    }

    fun setSections(menus: List<Menu>) {
        Log.d("메뉴 길이", menus.size.toString())
        for (menu in menus) {
            var flag = 0
            Log.d("섹션", menu.section)
            Log.d("이름", menu.name)
            Log.d("가격", menu.price.toString())
            Log.d("섹션 리스트 길이", sections.size.toString())
            for (i in sections.indices) {
                if (sections[i].name == menu.section) {
                    sections[i].menus.add(menu)
                    flag = 1
                    break
                }
            }
            if (flag == 0) {
                sections.add(Section(menu.section, mutableListOf(menu)))
            }
        }
    }

    fun mappingAdapter() {
        binding.tvStoreName.text = storeName

        binding.rvMenuSection.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSection.setHasFixedSize(true)

        val adapter = BaedalMenuSectionAdapter(requireContext(), sections)
        binding.rvMenuSection.adapter = adapter

        /** 이중 어댑터안의 메뉴 이름을 선택할 경우 해당 메뉴의 옵션을 보여주는 프래그먼트로 이동 */
        adapter.addListener(object : BaedalMenuSectionAdapter.OnItemClickListener {
            override fun onClick(sectionName: String, menuName: String) {
                loop@ for (section in sections) {
                    if (sectionName == section.name) {
                        for (menu in section.menus) {
                            if (menuName == menu.name) {
                                setFrag(FragmentBaedalOpt(), mapOf(
                                    "menuName" to menu.name,
                                    "menuPrice" to menu.price.toString(),
                                    "storeId" to storeId
                                ))
                                break@loop
                            }
                        }
                    }
                }
            }
        })
    }

    fun setCartBtn() {
        binding.tvCartCount.text = orders.length().toString()

        if (orders.length() > 0) {
            binding.lytCart.setBackgroundResource(R.drawable.btn_baedal_cart)
            binding.lytCartCount.visibility = View.VISIBLE
            binding.btnCart.setEnabled(true)
            binding.lytCart.setEnabled(true)
        } else {
            binding.lytCart.setBackgroundResource(R.drawable.btn_baedal_cart_empty)
            binding.lytCartCount.visibility = View.INVISIBLE
            binding.btnCart.setEnabled(false)
            binding.lytCart.setEnabled(false)
        }
    }

    fun cartOnClick(){
        val map = mutableMapOf(
            "isPosting" to isPosting.toString(),
            "postId" to postId,
            "currentMember" to currentMember,
            "isUpdating" to isUpdating.toString(),
            "storeId" to storeId,
            "storeName" to storeName,
            "baedalFee" to baedalFee,
            "orders" to orders.toString()
        )

        setFrag(FragmentBaedalConfirm(), map)
    }

    fun getOrdersObject(){
        val loopingDialog = looping()
        api.getOrders(postId).enqueue(object : Callback<UserOrder?> {
            override fun onResponse(call: Call<UserOrder?>, response: Response<UserOrder?>) {
                if (response.code() == 200) {
                    var ordersString = ""
                    if (response.body() != null) ordersString = apiModelToJson(response.body()!!)
                    orders = JSONArray(ordersString)
                    setCartBtn()
                } else {
                    Log.e("baedalMenu Fragment - getOrders", response.toString())
                    makeToast("주문정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                    onBackPressed()
                }
                looping(false, loopingDialog)
            }

            override fun onFailure(call: Call<UserOrder?>, t: Throwable) {
                Log.e("baedalMenu Fragment - getOrders", t.message.toString())
                makeToast("주문정보를 불러오지 못 했습니다.\n다시 시도해 주세요.")
                looping(false, loopingDialog)
                onBackPressed()
            }
        })
    }

    /** api로 받은 데이터를 가공하기 편리하게 JSON 형식으로 변환 */
    fun apiModelToJson(userOrders: UserOrder): String{
        val orders = JSONArray()
        for (order in userOrders.orders) {
            val orderObject = JSONObject()
            orderObject.put("count", order.quantity)
            orderObject.put("menuName", order.menu_name)
            orderObject.put("menuPrice", order.menu_price)
            orderObject.put("sumPrice", order.sum_price)

            val groups = JSONArray()
            for (group in order.groups) {
                val groupObject = JSONObject()
                groupObject.put("groupId", group._id)
                groupObject.put("groupName", group.name)
                val options = JSONArray()
                for (option in group.options) {
                    val optionObject = JSONObject()
                    optionObject.put("optionId", option._id)
                    optionObject.put("optionName", option.name)
                    optionObject.put("optionPrice", option.price)
                    options.put(optionObject)
                    groupObject.put("options",options)
                }
                groups.put(groupObject)
                orderObject.put("groups", groups)
            }
            orders.put(orderObject)
        }
        return orders.toString()
    }

    fun looping(loopStart: Boolean = true, loopingDialog: LoopingDialog? = null): LoopingDialog? {
        val mActivity = activity as MainActivity
        return mActivity.looping(loopStart, loopingDialog)
    }

    fun makeToast(message: String){
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments)
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}