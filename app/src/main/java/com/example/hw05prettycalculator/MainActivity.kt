package com.example.hw05prettycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hw05prettycalc.CalculatorFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, CalculatorFragment())
                .commit()
        }
    }
}

