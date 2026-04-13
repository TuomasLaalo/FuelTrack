package com.laatu.fueltank

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.laatu.fueltank.ui.FuelTankAppRoot
import com.laatu.fueltank.ui.theme.FuelTankTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FuelTankTheme {
                FuelTankAppRoot()
            }
        }
    }
}
