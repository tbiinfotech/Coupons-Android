package com.coupals.ui.fragment

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.coupals.Constant
import com.coupals.R
import com.coupals.databinding.FragmentActivePostUpdateBinding
import com.coupals.model.*
import com.coupals.prefs.SharedPrefModule
import com.coupals.ui.activity.HomeActivity
import com.coupals.ui.base.BaseFragment
import com.coupals.ui.network.Apis
import com.coupals.ui.network.NetworkResponceListener
import com.coupals.utills.CommonMethod
import com.coupals.utills.CustomDialog
import com.erpcrebit.screenviewer.utils.CommonUtils
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class EditActivePostFragment : BaseFragment(),View.OnClickListener, NetworkResponceListener {

    private lateinit var binding: FragmentActivePostUpdateBinding
    var arrayListCoupon          = ArrayList<TradeCouponDetail>()
    lateinit var strCouponId:String
    private var tradeId:Int = 0
    var coinBuyOptionStatus="0"
    var coinSelected="0"
    private val TAG = "CreatePostFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentActivePostUpdateBinding.inflate(inflater, container, false)

        binding.llCoinsType.setOnClickListener {
            commonCoinDialog(context!!)
        }

        binding.btnSubmit.setOnClickListener {
            submitPost()
        }

        binding.mainLL.setOnClickListener {
            CommonMethod.hideKeyboard(context as HomeActivity)
        }
        binding.cbCoin.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                coinBuyOptionStatus="1"
            }else{
                coinBuyOptionStatus="0"
            }
        }
        getBundles()
        return binding.root
    }

    private fun getBundles() {
        val bundle =arguments
        if (bundle!=null){
            try {
                tradeId = bundle.getString("tradeId")!!.toInt()
                try {
                    if (CommonUtils().isNetworkAvailable(context!!)) {
                        try {
                            showProgressDialog(context as HomeActivity)
                            val paramObject = JSONObject()
                            paramObject.put(Constant.USER_ID, SharedPrefModule(context!!).userId)
                            paramObject.put(Constant.TOKEN, SharedPrefModule(context!!).token)
                            paramObject.put(Constant.TRADE_COUPON_ID, tradeId)
                            makeHttpCall(this, Apis.GET_TRADE_DETAILS, getRetrofitInterface()!!.getTradeCouponDetail(paramObject.toString()))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        CustomDialog.commonDialog(context!!, getString(R.string.internet_error))
                    }
                }catch (e:Exception){
                }
            }catch (e:Exception){
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (context as HomeActivity).binding.llTop.visibility=View.VISIBLE
        (context as HomeActivity).binding.llHomeBack.visibility=View.VISIBLE
        (context as HomeActivity).binding.tvHomeTitle.visibility=View.VISIBLE
        (context as HomeActivity).binding.tvHomeTitle.text="My offers"
        (context as HomeActivity).binding.llHomeNavigSearch.visibility=View.GONE
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onClick(v: View?) {
    }

    override fun onSuccess(url: String?, responce: String?) {
        hideProgressDialog()
        if (Apis.EDIT_ACTIVE_POST == url) {

            try {

                val createPostResponse: CreatePostResponse = Gson().fromJson(responce, CreatePostResponse::class.java)
                if (createPostResponse.status_code== Constant.SUCESS_CODE){
                    val bun = Bundle()
                    bun.putString("type", "ActivePostCoupon")
                    bun.putString("tradeId", createPostResponse.data[0].id.toString())
                    val fragment = UpdatePostFragment()
                    fragment.arguments = bun
                    CommonMethod.replaceFragment((context as HomeActivity), fragment)
//                    (context as HomeActivity).binding.navigationView.selectedItemId = R.id.navigation_four
//                    CommonMethod.replaceFragment(context as HomeActivity,MyCouponFragment())
                    Toast.makeText(context!!, "Post updated.", Toast.LENGTH_SHORT).show()
                }
                else if(createPostResponse.status_code==Constant.STATUS_CODE){
                    CustomDialog.commonDialog(context!!, createPostResponse.msg)
                }
                else if(createPostResponse.status_code==Constant.INVAID_token_CODE){
                    Toast.makeText(requireContext(),createPostResponse.msg, Toast.LENGTH_SHORT).show()
                    CommonMethod.moveToLogin(context!!)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        else  if (Apis.GET_TRADE_DETAILS == url) {
            arrayListCoupon= ArrayList()
            try {
                if (TextUtils.isEmpty(responce)) {
                    return
                }
                val couponWithCatResponse: TradeCouponResponse = Gson().fromJson(responce, TradeCouponResponse::class.java)
                if (couponWithCatResponse.status_code==Constant.SUCESS_CODE){

                    if(couponWithCatResponse.data.isNotEmpty()){
                        arrayListCoupon.addAll(couponWithCatResponse.data[0].coupon_detail)

                        strCouponId                 = arrayListCoupon[0].coupon_id.toString()
                        binding.tvTitle.text        = arrayListCoupon[0].company_name
                        binding.tvDescription.text  = arrayListCoupon[0].coupon_promotion

                        if(arrayListCoupon[0].coupon_discount_type.equals(Constant.MONEY_OFF)){
                            binding.tvPercentage.text ="$"+arrayListCoupon[0].coupon_discount

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.PERCENTAGE_OFF)){
                            binding.tvPercentage.text = arrayListCoupon[0].coupon_discount+"%"

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.BOGO)){
                            binding.tvPercentage.text = getString(R.string.bogo)

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.B2G1)){
                            binding.tvPercentage.text = getString(R.string.B2G1)

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.BOGOX)){
                            binding.tvPercentage.text = getString(R.string.BOGOX)
                        }


                        if (arrayListCoupon[0].coupon_valid_upto.isNotEmpty()) {
                            binding.tvCouponDate.text = CommonMethod.parseCouponDate2(arrayListCoupon[0].coupon_valid_upto)
                            binding.tvCouponDate.setTextColor(ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color)))
                        }

                        if (arrayListCoupon[0].rarity.isNotEmpty()) {
                            if(arrayListCoupon[0].rarity.equals("Gold")){
                                binding.rarityIV.setImageResource(R.drawable.gold)
                            }else if(arrayListCoupon[0].rarity.equals("Silver")){
                                binding.rarityIV.setImageResource(R.drawable.silver)
                            }else if(arrayListCoupon[0].rarity.equals("Bronze")){
                                binding.rarityIV.setImageResource(R.drawable.bronze)
                            }
                        }

                        if (arrayListCoupon[0].company_background_color.isNotEmpty()) {
                            binding.llLogoBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color))
                        }

                        if (arrayListCoupon[0].company_background_color.isNotEmpty()) {
                            binding.llMainBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color))
                        }

                        if(!arrayListCoupon[0].coupon_image.equals("")){
                            CommonMethod.ShowGlide(context,Apis.COUPON_IMAGE_BASE_URL +  arrayListCoupon[0].coupon_image,binding.ivLogo)
                        }else{
                            CommonMethod.ShowGlide(context,Apis.COMPANY_IMAGE_BASE_URL +  arrayListCoupon[0].company_image,binding.ivLogo)
                        }

                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onFailure(url: String?, throwable: Throwable?) {
        CustomDialog.commonDialog(context!!, resources.getString(R.string.server_error))
        hideProgressDialog()
    }

    private fun submitPost(){
        CommonMethod.hideKeyboard(context as HomeActivity)

        val strDescription    =binding.etDescription.text.toString()

        if (strDescription.isEmpty()){
            CustomDialog.commonDialog(context!!,"Please enter post description")
            return
        }

        if (coinBuyOptionStatus == "1"){
            if (coinSelected.equals("0")){
                CustomDialog.commonDialog(context!!,"Please select coins.")
                return
            }
        }

        if (CommonUtils().isNetworkAvailable(context!!)) {
            try {
                showProgressDialog(context as HomeActivity)
                val paramObject = JSONObject()
                paramObject.put(Constant.USER_ID, SharedPrefModule(context!!).userId)
                paramObject.put(Constant.TOKEN, SharedPrefModule(context!!).token)
                paramObject.put(Constant.TRADE_COUPON_ID, tradeId)
                paramObject.put(Constant.DESCRIPTION, strDescription)
                paramObject.put(Constant.COIN_BUY_OPTION, coinBuyOptionStatus)
                paramObject.put(Constant.COIN, coinSelected)

                Log.d("editttt",paramObject.toString())

                makeHttpCall(this, Apis.EDIT_ACTIVE_POST, getRetrofitInterface()!!.editActivePost(paramObject.toString()))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            CustomDialog.commonDialog(context!!, getString(R.string.internet_error))
        }
    }

    fun commonCoinDialog(context: Context) {
        try {
            val dialogCart = Dialog(context)
            dialogCart.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogCart.setContentView(R.layout.dialog_coin_selected)
            dialogCart.setCanceledOnTouchOutside(false)
            dialogCart.setCancelable(false)

            dialogCart.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogCart.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dialogCart.show()

            val  etCoins=dialogCart.findViewById<AppCompatEditText>(R.id.etCoins)

            dialogCart.findViewById<AppCompatTextView>(R.id.tvOk).setOnClickListener {

                coinSelected=etCoins.text.toString()
                if (coinSelected.isEmpty()){
                    CustomDialog.commonDialog(context!!,"Please enter coin numbers.")
                }else{
                    binding.coinsTV.text="#"+coinSelected
                    dialogCart.dismiss()
                }
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}