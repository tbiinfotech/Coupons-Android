package com.coupals.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coupals.Constant
import com.coupals.R
import com.coupals.databinding.FragmentVisitorProfileBinding
import com.coupals.model.UserActivePost
import com.coupals.model.UserDetailsResponse
import com.coupals.prefs.SharedPrefModule
import com.coupals.ui.activity.HomeActivity
import com.coupals.ui.adapter.RvUserActivePostAdapter
import com.coupals.ui.adapter.RvUserTrophyAdapter
import com.coupals.ui.base.BaseFragment
import com.coupals.ui.network.Apis
import com.coupals.ui.network.NetworkResponceListener
import com.coupals.utills.CommonMethod
import com.coupals.utills.CustomDialog
import com.erpcrebit.screenviewer.utils.CommonUtils
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

class VisitorProfileFragment : BaseFragment(),NetworkResponceListener {

    private lateinit var binding: FragmentVisitorProfileBinding
    var  userId:String=""
    var  token:String=""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVisitorProfileBinding.inflate(inflater, container, false)

        binding.rvUserTropy.layoutManager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
        val adapter = RvUserTrophyAdapter(context!!)
        binding.rvUserTropy.adapter = adapter


        getBundleData()

        return binding.root
    }

    private fun getBundleData() {

        val bundle =arguments
        if (bundle!=null) {
           userId       =   bundle.getString("userId").toString()
            token       =   bundle.getString("token").toString()
            getUserData(userId,token)
        }
    }

    private fun getUserData(userId: String, token: String) {
        if (CommonUtils().isNetworkAvailable(context!!)) {
            try {
                showProgressDialog(context as HomeActivity)
                val paramObject = JSONObject()
                paramObject.put(Constant.USER_ID, userId)
                paramObject.put(Constant.TOKEN, token)

                Log.d("iouyytt",paramObject.toString())
                makeHttpCall(this, Apis.USER_PROFILE_DETAIL, getRetrofitInterface()!!.getAnotherUserDetails(paramObject.toString()))

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        else {
            CustomDialog.commonDialog(context!!, getString(R.string.internet_error))
        }
    }

    override fun onSuccess(url: String?, responce: String?) {
        if (Apis.USER_PROFILE_DETAIL == url) {

            hideProgressDialog()
            val userDetailsResponse: UserDetailsResponse = Gson().fromJson(responce, UserDetailsResponse::class.java)

            if (userDetailsResponse.status_code ==Constant.SUCESS_CODE) {

                binding.usernameTV.setText(userDetailsResponse.data[0].username)
                binding.totalTradeTV.setText(userDetailsResponse.data[0].total_trade.toString())
                binding.availableCouponsTV.setText(userDetailsResponse.data[0].available_coupons.toString())
                CommonMethod.ShowGlideCircular(context,Apis.IMAGE_BASE_URL+userDetailsResponse.data[0].profile_pic,binding.civProfile)

                val strDate= CommonMethod.getDateFromTimeStamp3( userDetailsResponse.data[0].created_at)
                binding.joiningDateTV.setText(strDate)

                binding.trophyHeadingTV.setText(userDetailsResponse.data[0].username+" Trophys")
                binding.activePostHeadingTV.setText(userDetailsResponse.data[0].username+" Active Posts")


                (context as HomeActivity).binding.tvHomeTitle.text=userDetailsResponse.data[0].username+" Profile"

                if(userDetailsResponse.data[0].user_active_post.size>0) {
                    binding.activePostTV.visibility=View.GONE
                    binding.rvActivePost.visibility=View.VISIBLE

                    binding.rvActivePost.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
                    val adapter1 = RvUserActivePostAdapter(this@VisitorProfileFragment, context!!, userDetailsResponse.data[0].user_active_post as ArrayList<UserActivePost>, userDetailsResponse.data[0].username, userDetailsResponse.data[0].profile_pic)
                    binding.rvActivePost.adapter = adapter1
                }else{
                    binding.activePostTV.visibility=View.VISIBLE
                    binding.rvActivePost.visibility=View.GONE
                }

            } else if(userDetailsResponse.status_code==Constant.STATUS_CODE){
                hideProgressDialog()
                CustomDialog.commonDialog(requireContext(), userDetailsResponse.msg)
            }
            else if(userDetailsResponse.status_code==Constant.INVAID_token_CODE){
                hideProgressDialog()
                Toast.makeText(requireContext(),userDetailsResponse.msg, Toast.LENGTH_SHORT).show()
                CommonMethod.moveToLogin(requireContext())
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
        (context as HomeActivity).binding.llHomeNavigSearch.visibility=View.GONE
        (context as HomeActivity).binding.tvHomeTitle.visibility=View.VISIBLE

    }
}
