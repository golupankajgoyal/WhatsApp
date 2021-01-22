package com.example.whatsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.example.whatsapp.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {
    private lateinit var phoneNumber:String
    private lateinit var countryCode:String
    private lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.phoneNumberEt.addTextChangedListener {
            binding.nextBtn.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }
        binding.nextBtn.setOnClickListener {
            checkNumber()
        }
    }

    private fun checkNumber() {
        countryCode=binding.ccp.selectedCountryCodeWithPlus
        phoneNumber=countryCode + binding.phoneNumberEt.text.toString()
        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this)
            .apply{
                setMessage(
                    "We will be verifying the phone number:$phoneNumber\n" +
                            "Is this OK, or would you like to edit the number?"
                )
                setPositiveButton("OK") { _, _ -> showOtpActivity() }
                setNegativeButton("Edit"){dialog,_->
                    dialog.dismiss()
                }
                setCancelable(false)
                create()
                show()
            }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this,OtpActivity::class.java).putExtra(PHONE_NUMBER,phoneNumber))
        finish()
    }
}