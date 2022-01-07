package com.coupals.ui.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.coupals.Constant
import com.coupals.R
import com.coupals.databinding.ActivityHomeBinding
import com.coupals.databinding.DialogDrawerItemsBinding
import com.coupals.model.UserDetailsResponse
import com.coupals.prefs.SharedPrefModule
import com.coupals.ui.base.BaseActivity
import com.coupals.ui.fragment.*
import com.coupals.ui.network.Apis
import com.coupals.ui.network.NetworkResponceListener
import com.coupals.utills.CommonMethod
import com.coupals.utills.CustomDialog
import com.erpcrebit.screenviewer.utils.CommonUtils
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import render.animations.Bounce
import render.animations.Render

class HomeActivity : BaseActivity(),View.OnClickListener ,NetworkResponceListener{

    lateinit var binding                        : ActivityHomeBinding
    private lateinit var llDrawerNotification   : LinearLayoutCompat
    private lateinit var llInbox                : LinearLayoutCompat
    private lateinit var llHelp                 : LinearLayoutCompat
    private lateinit var llMyProfile            : LinearLayoutCompat
    private lateinit var llMyTrades             : LinearLayoutCompat
    private lateinit var llStore                : LinearLayoutCompat
    private lateinit var llSetting              : LinearLayoutCompat
    private lateinit var llLogout               : LinearLayoutCompat
    private lateinit var llSearch               : LinearLayoutCompat
    private lateinit var rlNotifications        : LinearLayoutCompat
    var booleanDrawerDialog                     : Boolean=true
    lateinit var drawerBinding                  : DialogDrawerItemsBinding
    lateinit var userDetail                     : UserDetailsResponse
    lateinit var strUserResponse                : String


    companion object {
        private const val LOCATION_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        drawerBinding=binding.drawerItems
        setContentView(binding.root)
        checkPermission(LOCATION_PERMISSION_CODE)

        llDrawerNotification    =binding.llInclude.findViewById(R.id.llNotification)
        llInbox                 =binding.llInclude.findViewById(R.id.llInbox)
        llMyProfile             =binding.llInclude.findViewById(R.id.llMyProfile)
        llMyTrades              =binding.llInclude.findViewById(R.id.llMyTrades)
        llStore                 =binding.llInclude.findViewById(R.id.llStore)
        llSetting               =binding.llInclude.findViewById(R.id.llSetting)
        llHelp                  =binding.llInclude.findViewById(R.id.llHelp)
        llLogout                =binding.llInclude.findViewById(R.id.llLogout)

        llDrawerNotification.setOnClickListener(this)

        llMyProfile.setOnClickListener(this)
        llInbox.setOnClickListener(this)
        llMyTrades.setOnClickListener(this)
        llSetting.setOnClickListener(this)
        llLogout.setOnClickListener(this)
        llStore.setOnClickListener(this)
        llStore.setOnClickListener(this)
        llHelp.setOnClickListener(this)

        binding.rlNotifications.setOnClickListener(this)
        binding.llSearch.setOnClickListener(this)
        binding.rlUser.setOnClickListener(this)
        binding.ivArrow.setOnClickListener(this)
        binding.llHomeBack.setOnClickListener(this)
        binding.ivLogo.setOnClickListener(this)

        val firstFragment  = HomeFragment()
        val secondFragment = TradeFragment()
        val thirdFragment  = AroundMeFragment()
        val fourFragment   = MyCouponFragment()
        val fiveFragment   = TheMallFragment()

        setCurrentFragment(firstFragment)
        getUserDetails()

        binding.navigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_one -> {
                    hideDrawerItemsFromTab()
                    getUserDetails()
                    setCurrentFragment(firstFragment)
                    binding.llHomeNavigSearch.visibility=View.VISIBLE
                    binding.llHomeBack.visibility=View.GONE
                    binding.tvHomeTitle.text="My Coupons(0)"
                    navigationImagesMargin(binding.navigationView)
                }
                R.id.navigation_two -> {
                    hideDrawerItemsFromTab()
                    getUserDetails()
                    binding.llHomeNavigSearch.visibility=View.GONE
                    binding.llHomeBack.visibility=View.VISIBLE
                    setCurrentFragment(secondFragment)
                    binding.tvHomeTitle.text="My Coupons(0)"
                    navigationImagesMargin(binding.navigationView)
                }
                R.id.navigation_three -> {
                    hideDrawerItemsFromTab()
                    getUserDetails()
                    binding.llHomeNavigSearch.visibility=View.GONE
                    binding.llHomeBack.visibility=View.VISIBLE
                    setCurrentFragment(thirdFragment)
                    binding.tvHomeTitle.text="My Coupons(0)"
                }
                R.id.navigation_four -> {
                    hideDrawerItemsFromTab()
                    getUserDetails()
                    binding.llHomeNavigSearch.visibility=View.GONE
                    binding.llHomeBack.visibility=View.VISIBLE
                    setCurrentFragment(fourFragment)
                    binding.tvHomeTitle.text="My Coupons(0)"
                }
                R.id.navigation_five -> {
                    hideDrawerItemsFromTab()
                    getUserDetails()
                    binding.llHomeNavigSearch.visibility=View.GONE
                    binding.llHomeBack.visibility=View.VISIBLE
                    setCurrentFragment(fiveFragment)
                    binding.tvHomeTitle.text="My Coupons(0)"
                }
            }
            true
        }
    }

    private fun navigationImagesMargin(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                navigationImagesMargin(child)
            }
        } else if (view is ImageView) {
            val param = view.layoutParams as ViewGroup.MarginLayoutParams
            param.topMargin = convertDpToPx(14)
            view.layoutParams = param
        }
    }

    fun convertDpToPx(dp: Int): Int {
        return Math.round(dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    private fun setCurrentFragment(fragment: Fragment) {
        try {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, fragment)
            ft.commit()
        } catch (e: Exception) {
        }
    }

    private fun exitFromApp() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to exit from app?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            finish()
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onBackPressed() {
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.container)
        if ( fragment is TradeFragment || fragment is AroundMeFragment || fragment is MyCouponFragment|| fragment is TheMallFragment) {
            setCurrentFragment(HomeFragment())
            binding.navigationView.selectedItemId = R.id.navigation_one
        }else if (fragment is HomeFragment ) {
            exitFromApp()
        }else {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    override fun onClick(v: View?) {
        if (v==binding.rlUser){
            showDrawerItems()
        }else if (v==binding.ivArrow){
            hideDrawerItems()
        }else if (v==llDrawerNotification){
            hideDrawerItems()
            CommonMethod.popFragment(this)
            CommonMethod.replaceFragment(this,NotificationFragment())
        }else if (v==llInbox){
            hideDrawerItems()
            CommonMethod.popFragment(this)
            CommonMethod.replaceFragment(this,InboxFragment())
        } else if (v==llMyProfile){
            hideDrawerItems()
            CommonMethod.popFragment(this)
            CommonMethod.replaceFragment(this,UserProfileFragment())
        }else if (v==llMyTrades){
            hideDrawerItems()
            CommonMethod.popFragment(this)
            CommonMethod.replaceFragment(this,MySentTradesFragment())
        }else if (v==llStore){
            hideDrawerItems()
            CommonMethod.popFragment(this)
            CommonMethod.replaceFragment(this,StoreCoinsFragment())
        }else if (v==llSetting){
            hideDrawerItems()
            CommonMethod.popFragment(this)
            CommonMethod.replaceFragment(this,SettingsFragment())
        }else if (v==llHelp){
            hideDrawerItems()
            CommonMethod.popFragment(this)
            CommonMethod.replaceFragment(this,HelpPagerFragment())
        } else if (v==binding.llHomeBack){
            handleBackPress()
        }else if (v==llLogout){
            hideDrawerItems()
            CustomDialog.logoutApp(this)
        }else if (v== binding.rlNotifications){
            hideDrawerItems()
            CommonMethod.replaceFragment(this,NotificationFragment())
        } else if (v== binding.llSearch){
            hideDrawerItems()
            CommonMethod.replaceFragment(this,HomeSearchFragment())
        }else if(v== binding.ivLogo){
            val intent= Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }

    }

    private fun handleBackPress(){
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.container)
        if ( fragment is TradeFragment || fragment is AroundMeFragment || fragment is MyCouponFragment|| fragment is TheMallFragment) {
            setCurrentFragment(HomeFragment())
            binding.navigationView.selectedItemId = R.id.navigation_one
        }else {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    private fun showDrawerItems(){

        strUserResponse = SharedPrefModule(this).getUserLoginResponseData()
        userDetail= Gson().fromJson(strUserResponse, UserDetailsResponse::class.java)

        drawerBinding.drawerCoinsTV.text=userDetail.data[0].balance_coins.toString()
        CommonMethod.ShowGlideCircular(this,Apis.IMAGE_BASE_URL+userDetail.data[0].profile_pic,drawerBinding.civProfile)
        drawerBinding.userNameTV.text=userDetail.data[0].username

        binding.llInclude.visibility=View.VISIBLE
        binding.rlUser.visibility=View.GONE
        binding.ivArrow.visibility=View.VISIBLE

        val render = Render(this)
        render.setAnimation(Bounce().InDown(binding.llInclude))
        render.start()
    }

    private fun hideDrawerItems(){
        binding.llInclude.visibility=View.VISIBLE
        binding.rlUser.visibility=View.VISIBLE
        binding.llInclude.visibility=View.GONE
        binding.ivArrow.visibility=View.GONE

        val render = Render(this)
        render.setAnimation(Bounce().InUp(binding.rlUser))
        render.start()
    }

    private fun hideDrawerItemsFromTab(){
        if (binding.ivArrow.visibility==View.VISIBLE){
            binding.llInclude.visibility=View.VISIBLE
            binding.rlUser.visibility=View.VISIBLE
            binding.llInclude.visibility=View.GONE
            binding.ivArrow.visibility=View.GONE
            val render = Render(this)
            render.setAnimation(Bounce().InUp(binding.rlUser))
            render.start()
        }
    }

    override fun onResume() {
        super.onResume()
        booleanDrawerDialog=true
    }

    private fun checkPermission(requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(this,
                ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION), requestCode)
        } else {
//            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                    buildAlertMessagePermission()
                } else {
                    Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show()
                      //proceed with logic by disabling the related features or quit the app.
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }
    }

    private fun buildAlertMessagePermission() {
        val builder: android.app.AlertDialog.Builder =
            android.app.AlertDialog.Builder(this)
        builder.setMessage("Location Services Permission required for this app")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                dialog.cancel()
                checkPermission(LOCATION_PERMISSION_CODE)
            }.setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show()
            }
        val alert: android.app.AlertDialog? = builder.create()
        alert?.show()
    }

     fun getUserDetails() {
        if (CommonUtils().isNetworkAvailable(this)) {
            try {
                val paramObject = JSONObject()
                paramObject.put(Constant.USER_ID, SharedPrefModule(this).userId)
                paramObject.put(Constant.TOKEN, SharedPrefModule(this).token)

                Log.d("userdetails",paramObject.toString())

                makeHttpCall(this, Apis.USER_DETAILS, getRetrofitInterface()!!.getUserDetails(paramObject.toString()))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            CustomDialog.commonDialog(this, getString(R.string.internet_error))
        }
    }

    override fun onSuccess(url: String?, responce: String?) {
        hideProgressDialog(this)
         if (Apis.USER_DETAILS == url) {

                val userDetailsResponse: UserDetailsResponse = Gson().fromJson(responce, UserDetailsResponse::class.java)
                if (userDetailsResponse.status_code ==Constant.SUCESS_CODE) {

                    Log.d("balancedcoinsss",userDetailsResponse.data[0].balance_coins.toString())
                    binding.coinsTV.text=userDetailsResponse.data[0].balance_coins.toString()
                    CommonMethod.ShowGlideCircular(this,Apis.IMAGE_BASE_URL+userDetailsResponse.data[0].profile_pic.toString(),binding.civProfile)

                    SharedPrefModule(this).setUserLoginResponse(Gson().toJson(userDetailsResponse))

                } else if(userDetailsResponse.status_code==Constant.STATUS_CODE){
                    hideProgressDialog(this)
                    CustomDialog.commonDialog(this, userDetailsResponse.msg)
                }
                else if(userDetailsResponse.status_code==Constant.INVAID_token_CODE){
                    hideProgressDialog(this)
                    Toast.makeText(this,userDetailsResponse.msg, Toast.LENGTH_SHORT).show()
                    CommonMethod.moveToLogin(this)
                }
            }
    }

    override fun onFailure(url: String?, throwable: Throwable?) {
        CustomDialog.commonDialog(this!!, resources.getString(R.string.server_error))
        hideProgressDialog(this)
    }
}