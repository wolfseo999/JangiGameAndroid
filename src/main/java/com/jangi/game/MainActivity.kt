package com.jangi.game

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jangi.game.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAloneMode.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("mode", GameMode.ALONE)
            startActivity(intent)
        }

        binding.btnTogetherMode.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("mode", GameMode.TOGETHER)
            startActivity(intent)
        }
    }
}

enum class GameMode {
    ALONE, TOGETHER
}
