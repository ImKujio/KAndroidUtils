package me.kujio.android.kandroidutils.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import me.kujio.android.kandroidutils.KLoad
import me.kujio.android.kandroidutils.simple.databinding.ActivityConstraintLoadBinding

class ConstraintLoadActivity : AppCompatActivity() {

    private lateinit var binding : ActivityConstraintLoadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_constraint_load)
        binding.load = KLoad.Loading()
        binding.root.postDelayed({
            binding.load = KLoad.Failed("加载失败，请重试")
        },3000)
        binding.root.postDelayed({
            binding.load = KLoad.Loading()
        },6000)
        binding.root.postDelayed({
            binding.load = null
        },9000)
    }
}