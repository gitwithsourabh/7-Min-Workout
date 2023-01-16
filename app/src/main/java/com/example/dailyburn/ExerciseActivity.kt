package com.example.dailyburn

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.example.dailyburn.databinding.ActivityExerciseBinding
import com.example.dailyburn.databinding.ActivityMainBinding

class ExerciseActivity : AppCompatActivity() {

    private var binding :ActivityExerciseBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExercise)

        if(supportActionBar != null){                                          //Error not resolved
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.toolbarExercise?.setNavigationOnClickListener{
            onBackPressed()                                                  //Error resolved
        }
    }
}