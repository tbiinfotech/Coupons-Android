package com.coupals.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.coupals.Constant
import com.coupals.R
import com.coupals.databinding.FragmentUpdatePostBinding
import com.coupals.model.TradeCouponData
import com.coupals.model.TradeCouponResponse
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
import org.json.JSONException
import org.json.JSONObject

class UpdatePostFragment : BaseFragment(), NetworkResponceListener {

    private lateinit var binding: FragmentUpdatePostBinding
    private var type:String=""
    private var tradeId:Int = 0
    var arrayLisTradeCoupon= ArrayList<TradeCouponData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentUpdatePostBinding.inflate(inflater, container, false)
        getBundles()

        binding.tabTradeSearch.addTab(binding.tabTradeSearch.newTab().setText("All"))
        binding.tabTradeSearch.addTab(binding.tabTradeSearch.newTab().setText("Food"))
        binding.tabTradeSearch.addTab(binding.tabTradeSearch.newTab().setText("Clothing"))
        binding.tabTradeSearch.addTab(binding.tabTradeSearch.newTab().setText("Technology"))
        binding.tabTradeSearch.addTab(binding.tabTradeSearch.newTab().setText("Outdoors"))
        binding.tabTradeSearch.addTab(binding.tabTradeSearch.newTab().setText("Home Decor"))
        binding.tabTradeSearch.addTab(binding.tabTradeSearch.newTab().setText("Others"))

        binding.tabTradeSearch.getTabAt(0)!!.select()
        val root: View = binding.tabTradeSearch.getChildAt(0)

        if (root is LinearLayout) {
            root.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            val drawable = GradientDrawable()
            drawable.setColor(ContextCompat.getColor(context!!, R.color.gray))
            drawable.setSize(2, 1)
            root.dividerPadding = 15
            root.dividerDrawable = drawable
        }

        binding.btnSubmit.setOnClickListener {
            if (type == "tradeCoupon"){

                val bun = Bundle()
                bun.putString("tradeId", tradeId.toString())
                val fragment = MyCouponFragment()
                fragment.arguments = bun
                CommonMethod.replaceFragment((context as HomeActivity), fragment)
            }else{
                val bun = Bundle()
                bun.putString("tradeId", tradeId.toString())
                val fragment = TradeFragment()
                fragment.arguments = bun
                CommonMethod.replaceFragment((context as HomeActivity), fragment)
            }

        }
        return binding.root
    }

    private fun getBundles() {
        val bundle =arguments
        if (bundle!=null){
            try {
                type    = bundle.getString("type").toString()
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

    override fun onSuccess(url: String?, responce: String?) {
        hideProgressDialog()
        if (Apis.GET_TRADE_DETAILS == url) {
            hideProgressDialog()
            try {
                if (TextUtils.isEmpty(responce)) {
                    return
                }
                val tradeCouponResponse: TradeCouponResponse = Gson().fromJson(responce, TradeCouponResponse::class.java)
                if (tradeCouponResponse.status_code==Constant.SUCESS_CODE){

                    if(tradeCouponResponse.data[0].coupon_detail.isNotEmpty()){
                        arrayLisTradeCoupon.addAll(tradeCouponResponse.data)

                        binding.tvTitle.text             = arrayLisTradeCoupon[0].coupon_detail[0].company_name
                        binding.tvDescription.text       = arrayLisTradeCoupon[0].coupon_detail[0].coupon_promotion
                        binding.tvPercentage.text        = arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount
                        binding.tvTradeDescription.text  = arrayLisTradeCoupon[0].description

                        if(arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount_type.equals(Constant.MONEY_OFF)){
                            binding.tvPercentage.text ="$"+arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount

                        }else if(arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount_type.equals(Constant.PERCENTAGE_OFF)){
                            binding.tvPercentage.text = arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount+"%"

                        }else if(arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount_type.equals(Constant.BOGO)){
                            binding.tvPercentage.text = getString(R.string.bogo)

                        }else if(arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount_type.equals(Constant.B2G1)){
                            binding.tvPercentage.text = getString(R.string.B2G1)

                        }else if(arrayLisTradeCoupon[0].coupon_detail[0].coupon_discount_type.equals(Constant.BOGOX)){
                            binding.tvPercentage.text = getString(R.string.BOGOX)
                        }

                        if (arrayLisTradeCoupon[0].coupon_detail[0].rarity.isNotEmpty()) {
                            if(arrayLisTradeCoupon[0].coupon_detail[0].rarity.equals("Gold")){
                                binding.rarityIV.setImageResource(R.drawable.gold)
                            }else if(arrayLisTradeCoupon[0].coupon_detail[0].rarity.equals("Silver")){
                                binding.rarityIV.setImageResource(R.drawable.silver)
                            }else if(arrayLisTradeCoupon[0].coupon_detail[0].rarity.equals("Bronze")){
                                binding.rarityIV.setImageResource(R.drawable.bronze)
                            }
                        }


                        if (arrayLisTradeCoupon[0].coupon_detail[0].coupon_valid_upto.isNotEmpty()) {
                            binding.tvDate.text = CommonMethod.parseCouponDate2(arrayLisTradeCoupon[0].coupon_detail[0].coupon_valid_upto)
                            binding.tvDate.setTextColor(ColorStateList.valueOf(Color.parseColor(arrayLisTradeCoupon[0].coupon_detail[0].company_background_color)))

                        }

                        if (arrayLisTradeCoupon[0].coupon_detail[0].company_background_color.isNotEmpty()) {
                            binding.llLogoBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayLisTradeCoupon[0].coupon_detail[0].company_background_color))
                        }

                        if (arrayLisTradeCoupon[0].coupon_detail[0].company_background_color.isNotEmpty()) {
                            binding.llMainBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayLisTradeCoupon[0].coupon_detail[0].company_background_color))
                        }

                        if(!arrayLisTradeCoupon[0].coupon_detail[0].coupon_image.equals("")) {
                            CommonMethod.ShowGlide(context,Apis.COUPON_IMAGE_BASE_URL + arrayLisTradeCoupon[0].coupon_detail[0].coupon_image,binding.ivLogo)
                        }else{
                            CommonMethod.ShowGlide(context,Apis.COMPANY_IMAGE_BASE_URL +arrayLisTradeCoupon[0].coupon_detail[0].company_image,binding.ivLogo)

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


    override fun onResume() {
        super.onResume()
        (context as HomeActivity).binding.llTop.visibility=View.VISIBLE
        (context as HomeActivity).binding.llHomeBack.visibility=View.VISIBLE
        (context as HomeActivity).binding.tvHomeTitle.visibility=View.VISIBLE
        (context as HomeActivity).binding.llHomeNavigSearch.visibility=View.GONE
        (context as HomeActivity).binding.tvHomeTitle.text="Update your post"
    }


}
