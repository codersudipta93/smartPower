package com.example.parkingagent.UI.fragments.LoginFragment

import android.app.AlertDialog
import android.os.Build
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.parkingagent.MainActivity
import com.example.parkingagent.R
import com.example.parkingagent.UI.base.BaseFragment
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.databinding.FragmentLoginBinding
import com.example.parkingagent.utils.scanner.DeviceInfoManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(),DeviceInfoManager.DeviceInfoListener {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var deviceInfoManager: DeviceInfoManager
    private lateinit var deviceId: String
    private lateinit var deviceSerialNumber: String
    private lateinit var sessionManager: SharedPreferenceManager
    //private lateinit var deviceImeiNumber: String


    override fun getLayoutResourceId(): Int {
        return R.layout.fragment_login
    }

    override fun initView() {
        super.initView()
        sessionManager= SharedPreferenceManager(requireContext())
        deviceInfoManager = DeviceInfoManager(this)
        //deviceInfoManager.checkAndRequestPermissions()
        binding.btnLoginContinue.setOnClickListener({
            deviceInfoManager.checkAndRequestPermissions()
        })



    }

    private fun agentLogin(){

        if (binding.edtEmail.text?.trim()?.isEmpty() == true){
            binding.edtEmail.error="Please enter User Id"
            return
        }
        if (binding.edtPassword.text?.trim()?.isEmpty() == true){
            binding.edtPassword.error="Please enter password"
            return
        }

        (requireActivity() as MainActivity).binding.loading.visibility=View.VISIBLE
        viewModel.callAgentLogin(binding.edtEmail.text.toString(),binding.edtPassword.text.toString(),this.deviceId,this.deviceSerialNumber)
    }

    private fun showActivationCodeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_activation_code, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val etActivationCode = dialogView.findViewById<EditText>(R.id.etActivationCode)
        val btnSubmitCode = dialogView.findViewById<Button>(R.id.btnSubmitCode)



        btnSubmitCode.setOnClickListener {
            val activationCode = etActivationCode.text.toString().trim()

            if (activationCode.isEmpty()) {
                etActivationCode.error = "Please enter activation code"
            } else {
                dialog.dismiss()
                callActivationApi(activationCode)
            }
        }

        dialog.show()
    }



    fun callActivationApi(activationCode:String){
        (requireActivity() as MainActivity).binding.loading.visibility=View.VISIBLE
        viewModel.callActivationApi(activationCode,deviceId,deviceSerialNumber)
    }

    override fun observe() {
        super.observe()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mutualSharedflow.collectLatest {
                    (requireActivity() as MainActivity).binding.loading.visibility=View.GONE
                    when (it) {

                        is LoginViewModel.LoginEvents.LoginSuccess -> {

                            if (sessionManager.getEntityId()==0){
                                showActivationCodeDialog()
                            }
                            else {
//                                findNavController().navigate(R.id.action_id_loginFragment_to_id_homeFragment)
                                findNavController().navigate(R.id.action_id_loginFragment_to_id_menuFragment)
                            }

                        }

                        is LoginViewModel.LoginEvents.ActivationCodeSuccess -> {
                            findNavController().navigate(R.id.action_id_loginFragment_to_id_menuFragment)
                        }

                        is LoginViewModel.LoginEvents.ActivationCodeFailed -> {
                            Toast.makeText(context,it.message,Toast.LENGTH_LONG).show()
                        }
                    }

                }
            }
        }
    }

    override fun onDeviceInfoFetched(deviceId: String?, imei: String?) {
        if (deviceId != null) {
            this.deviceId=deviceId
        }
//        if (imei != null) {
//            this.deviceImeiNumber=imei
//        }

        try {
            deviceSerialNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()  // Requires READ_PHONE_STATE permission
            } else {
                Build.SERIAL.toString()
            }
        } catch (e: SecurityException) {
            // Handle the case where the permission is not granted
            Log.e("LoginFragment", "Permission not granted to read serial", e)
            // Optionally, fall back to another identifier:
            deviceSerialNumber = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        }


        Log.d("deviceId",this.deviceId.toString())
        Log.d("deviceImeiNumber",imei.toString())
        Log.d("ANDROID_ID", Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID))
//        Log.d("SERIAL_NUMBER",Build.getSerial().toString())
        agentLogin()


    }

}