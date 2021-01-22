package com.example.whatsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsapp.adapters.ScreenSliderAdapter
import com.example.whatsapp.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.viewPager.adapter= ScreenSliderAdapter(this)
        TabLayoutMediator(binding.tabs,binding.viewPager, TabLayoutMediator.TabConfigurationStrategy{ tab: TabLayout.Tab, i: Int ->
            when(i){
                0-> tab.text="CHATS"
                1-> tab.text="PEOPLE"
            }
        }).attach()
    }
}