package com.coupals.ui.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coupals.Constant
import com.coupals.R
import com.coupals.databinding.ActivePostCoupondetailsFragmentBinding
import com.coupals.model.*
import com.coupals.prefs.SharedPrefModule
import com.coupals.ui.activity.HomeActivity
import com.coupals.ui.adapter.RvActivePostCouponCategoryAdapter
import com.coupals.ui.base.BaseFragment
import com.coupals.ui.network.Apis
import com.coupals.ui.network.NetworkResponceListener
import com.coupals.utills.CommonMethod
import com.coupals.utills.CustomDialog
import com.erpcrebit.screenviewer.utils.CommonUtils
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList
import android.content.Intent
import android.net.Uri


class ActivePostCouponDetailFragment : BaseFragment(),NetworkResponceListener ,View.OnClickListener{

    private lateinit var binding: ActivePostCoupondetailsFragmentBinding
    var arrayListCoupon          =  ArrayList<TradeCouponDetail>()
    var arrayListCouponCategory  =  ArrayList<TradeCouponCategory>()
    private var tradeId:Int      =  0
    private var offerCount:Int   =  0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = ActivePostCoupondetailsFragmentBinding.inflate(inflater, container, false)

        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)


        getBundles()

        binding.llBack.setOnClickListener {
            CommonMethod.popFragment(context as HomeActivity)
        }

        binding.btnTradeCoupon.setOnClickListener {
            val bun = Bundle()
            bun.putString("type", "ActivePostCoupon")
            bun.putString("tradeId", tradeId.toString())
            val fragment = RecieveOfferOnMyPostFragment()
            fragment.arguments = bun
            CommonMethod.replaceFragment((context as HomeActivity), fragment)
        }

        binding.useCouponBT.setOnClickListener{
            binding.ivQrCode.visibility=View.VISIBLE
            binding.tvQrCode.visibility=View.VISIBLE
            binding.useCouponBT.visibility=View.GONE
        }

        binding.btnEdit.setOnClickListener {
            val bun = Bundle()
            bun.putString("tradeId", tradeId.toString())
            val fragment = EditActivePostFragment()
            fragment.arguments = bun
            CommonMethod.replaceFragment((context as HomeActivity), fragment)
        }

        binding.btnRemove.setOnClickListener{
            callTradeRemoveApi()
        }

        binding.btnVisitWebsite.setOnClickListener{
            val browser = Intent(Intent.ACTION_VIEW, Uri.parse(arrayListCoupon[0].company_website))
            startActivity(browser)
        }


        binding.detailsRL.setOnClickListener(this)
        binding.codeRL.setOnClickListener(this)
        binding.moreInfoRL.setOnClickListener(this)

        return binding.root
    }

    private fun getBundles() {
        val bundle =arguments
        if (bundle!=null){
            try {
                tradeId = bundle.getString("tradeId")!!.toInt()
                offerCount= bundle.getString("offerCount")!!.toInt()

                try {
                    if (CommonUtils().isNetworkAvailable(requireContext())) {
                        try {
                            showProgressDialog(context as HomeActivity)
                            val paramObject = JSONObject()
                            paramObject.put(Constant.USER_ID, SharedPrefModule(requireContext()).userId)
                            paramObject.put(Constant.TOKEN, SharedPrefModule(requireContext()).token)
                            paramObject.put(Constant.TRADE_COUPON_ID, tradeId)

                            makeHttpCall(this, Apis.GET_TRADE_DETAILS, getRetrofitInterface()!!.getTradeCouponDetail(paramObject.toString()))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        CustomDialog.commonDialog(requireContext(), getString(R.string.internet_error))
                    }
                }catch (e:Exception){

                }

            }catch (e:Exception){

            }
        }
    }

    private fun callTradeRemoveApi() {
        try {
            if (CommonUtils().isNetworkAvailable(requireContext())) {
                try {
                    showProgressDialog(context as HomeActivity)
                    val paramObject = JSONObject()
                    paramObject.put(Constant.USER_ID, SharedPrefModule(requireContext()).userId)
                    paramObject.put(Constant.TOKEN, SharedPrefModule(requireContext()).token)
                    paramObject.put(Constant.TRADE_POST_ID, tradeId)
                    makeHttpCall(this, Apis.CANCEL_TRADE, getRetrofitInterface()!!.cancelTrade(paramObject.toString()))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                CustomDialog.commonDialog(requireContext(), getString(R.string.internet_error))
            }
        }catch (e:Exception){

        }
    }

    override fun onSuccess(url: String?, responce: String?) {
        hideProgressDialog()
        if (Apis.GET_TRADE_DETAILS == url) {
            arrayListCoupon= ArrayList()
            try {

                val couponWithCatResponse: TradeCouponResponse = Gson().fromJson(responce, TradeCouponResponse::class.java)
                if (couponWithCatResponse.status_code==Constant.SUCESS_CODE){

                    if(couponWithCatResponse.data[0].description.isNotEmpty()){
                        arrayListCoupon.addAll(couponWithCatResponse.data[0].coupon_detail)

                        binding.tvTitle.text    =   arrayListCoupon[0].company_name
                        binding.tvDetails.text  =   arrayListCoupon[0].coupon_promotion
                        binding.tvNotes.text    =   arrayListCoupon[0].coupon_note
                        binding.tvQrCode.text   =   arrayListCoupon[0].coupon_barcode


                        if(arrayListCoupon[0].coupon_geolocation.equals("")){
                            binding.locationLL.visibility   =   View.GONE
                            binding.locationTV.text         =   arrayListCoupon[0].coupon_geolocation
                        }else{
                            binding.locationLL.visibility   =   View.VISIBLE
                            binding.locationTV.text         =   arrayListCoupon[0].coupon_geolocation
                        }
                        if(arrayListCoupon[0].company_address.equals("")) {
                            binding.addressLL.visibility    =   View.GONE
                            binding.addressTV.text = arrayListCoupon[0].company_address
                        }else{
                            binding.addressLL.visibility    =   View.VISIBLE
                            binding.addressTV.text = arrayListCoupon[0].company_address
                        }

                        if(arrayListCoupon[0].coupon_additional_detail.equals("")) {
                            binding.addtionalDetailsLL.visibility=View.GONE
                            binding.additionalDetailsTV.text = arrayListCoupon[0].coupon_additional_detail
                        }else{
                            binding.addtionalDetailsLL.visibility=View.VISIBLE
                            binding.additionalDetailsTV.text = arrayListCoupon[0].coupon_additional_detail
                        }

                        if(arrayListCoupon[0].coupon_discount_type.equals(Constant.MONEY_OFF)){
                            binding.tvPercentage.text = "$"+arrayListCoupon[0].coupon_discount

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.PERCENTAGE_OFF)){
                            binding.tvPercentage.text = arrayListCoupon[0].coupon_discount+"%"

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.BOGO)){
                            binding.tvPercentage.text = getString(R.string.bogo)

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.B2G1)){
                            binding.tvPercentage.text = getString(R.string.B2G1)

                        }else if(arrayListCoupon[0].coupon_discount_type.equals(Constant.BOGOX)){
                            binding.tvPercentage.text = getString(R.string.BOGOX)
                        }

                        if(arrayListCoupon[0].rarity.equals("Gold")){
                            binding.rarityIV.setImageResource(R.drawable.goldsq)
                        }else if(arrayListCoupon[0].rarity.equals("Silver")){
                            binding.rarityIV.setImageResource(R.drawable.silversq)
                        }else if(arrayListCoupon[0].rarity.equals("Bronze")){
                            binding.rarityIV.setImageResource(R.drawable.bronzesq)
                        }

                        if ( arrayListCoupon[0].company_background_color.isNotEmpty()) {
                            binding.llLogoBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor( arrayListCoupon[0].company_background_color))
                        }
                        if ( arrayListCoupon[0].company_border_color.isNotEmpty()) {
                            binding.llBorderBackground.backgroundTintList = ColorStateList.valueOf(Color.parseColor( arrayListCoupon[0].company_border_color))
                        }

                        if(!arrayListCoupon[0].coupon_image.equals("")) {
                            CommonMethod.ShowGlide(context,Apis.COUPON_IMAGE_BASE_URL + arrayListCoupon[0].coupon_image,binding.ivlogo)
                        }else{
                            CommonMethod.ShowGlide(context,Apis.COMPANY_IMAGE_BASE_URL + arrayListCoupon[0].company_image,binding.ivlogo)
                        }

                        CommonMethod.ShowGlide(context,Apis.BARCODE_IMAGE_BASE_URL +  arrayListCoupon[0].coupon_barcode_image,binding.ivQrCode)

                        if (arrayListCoupon[0].coupon_valid_upto.isNotEmpty()) {
                            binding.tvCouponDate.text = CommonMethod.parseCouponDate2( arrayListCoupon[0].coupon_valid_upto)
                            if(arrayListCoupon[0].company_background_color.equals("#ffffff")) {
                                binding.tvCouponDate.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                            }else{
                                binding.tvCouponDate.setTextColor(ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color)))
                            }

                        }

                        Log.d("sbnckjarr",arrayListCoupon[0].company_background_color)
                        binding.tabCouponDetail.setSelectedTabIndicatorColor(Color.parseColor(arrayListCoupon[0].company_background_color))

                        if (arrayListCoupon[0].categories.isNotEmpty()){
                            arrayListCouponCategory.addAll(arrayListCoupon[0].categories)
                            val adapter1 = RvActivePostCouponCategoryAdapter(requireContext(),arrayListCouponCategory)
                            binding.rvCategory.adapter = adapter1
                        }
                        binding.btnTradeCoupon.text="See offers("+offerCount+")"

                        if(arrayListCoupon[0].company_background_color.equals("#ffffff")) {
                            binding.detailsView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                        }else{
                            binding.detailsView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color))
                        }
                    }
                }
                else if (couponWithCatResponse.status_code == Constant.STATUS_CODE) {
                    CustomDialog.commonDialog(requireContext(), couponWithCatResponse.msg)
                } else if (couponWithCatResponse.status_code == Constant.INVAID_token_CODE) {
                    Toast.makeText(requireContext(), couponWithCatResponse.msg, Toast.LENGTH_SHORT).show()
                    CommonMethod.moveToLogin(requireContext())
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        else if(Apis.CANCEL_TRADE==url){
            val commonResponse: CommanResponseForAll = Gson().fromJson(responce, CommanResponseForAll::class.java)
            if (commonResponse.status_code==Constant.SUCESS_CODE){
                Toast.makeText(context,commonResponse.msg,Toast.LENGTH_SHORT).show()
                CommonMethod.popFragment(context as HomeActivity)

            } else if (commonResponse.status_code == Constant.STATUS_CODE) {
                CustomDialog.commonDialog(requireContext(), commonResponse.msg)
            } else if (commonResponse.status_code == Constant.INVAID_token_CODE) {
                Toast.makeText(requireContext(), commonResponse.msg, Toast.LENGTH_SHORT).show()
                CommonMethod.moveToLogin(requireContext())
            }
        }
    }

    override fun onFailure(url: String?, throwable: Throwable?) {
        CustomDialog.commonDialog(requireContext(), resources.getString(R.string.server_error))
        hideProgressDialog()
    }

    override fun onResume() {
        super.onResume()
        (context as HomeActivity).binding.llTop.visibility=View.GONE
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.detailsRL->{
                if(arrayListCoupon[0].company_background_color.equals("#ffffff")) {
                    binding.detailsView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                }else{
                    binding.detailsView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color))
                }
                binding.detailsView.visibility=View.VISIBLE
                binding.codeView.visibility=View.GONE
                binding.moreInfoView.visibility=View.GONE

                binding.detailsTV.setTextColor(resources.getColor(R.color.black))
                binding.codeTV.setTextColor(resources.getColor(R.color.gray))
                binding.moreInfoTV.setTextColor(resources.getColor(R.color.gray))



                binding.rlFirstTab.visibility   =   View.VISIBLE
                binding.rlSecondTab.visibility  =   View.GONE
                binding.llThirdTab.visibility   =   View.GONE

            }
            R.id.codeRL->{

                if(arrayListCoupon[0].company_background_color.equals("#ffffff")) {
                    binding.codeView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                }else{
                    binding.codeView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color))
                }
                binding.detailsView.visibility=View.GONE
                binding.codeView.visibility=View.VISIBLE
                binding.moreInfoView.visibility=View.GONE

                binding.detailsTV.setTextColor(resources.getColor(R.color.gray))
                binding.codeTV.setTextColor(resources.getColor(R.color.black))
                binding.moreInfoTV.setTextColor(resources.getColor(R.color.gray))


                binding.rlFirstTab.visibility   =   View.GONE
                binding.rlSecondTab.visibility  =   View.VISIBLE
                binding.llThirdTab.visibility   =   View.GONE

                binding.useCouponDisableBT.visibility=View.VISIBLE
                binding.useCouponBT.visibility=View.GONE

            }
            R.id.moreInfoRL->{
                if(arrayListCoupon[0].company_background_color.equals("#ffffff")) {
                    binding.moreInfoView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                }else{
                    binding.moreInfoView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(arrayListCoupon[0].company_background_color))
                }
                binding.detailsView.visibility=View.GONE
                binding.codeView.visibility=View.GONE
                binding.moreInfoView.visibility=View.VISIBLE

                binding.detailsTV.setTextColor(resources.getColor(R.color.gray))
                binding.codeTV.setTextColor(resources.getColor(R.color.gray))
                binding.moreInfoTV.setTextColor(resources.getColor(R.color.black))


                binding.rlFirstTab.visibility   =   View.GONE
                binding.rlSecondTab.visibility  =   View.GONE
                binding.llThirdTab.visibility   =   View.VISIBLE
            }
        }
    }
}