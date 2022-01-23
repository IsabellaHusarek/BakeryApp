package org.wit.bakeryapp

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import org.wit.bakeryapp.databinding.ActivityLoadScreenBinding


class LoadScreenActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoadScreenBinding
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadScreenBinding.inflate(layoutInflater) //view binding
        setContentView(binding.root)

        progressBar = binding.loadScreenProgressBar
        setProgressAnimation()

        Handler().postDelayed({ //should run after 2sec -> load screen time out
            val intent = Intent(this, BakeryStartpageActivity::class.java) //use intent to go from one activity to another
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.load_screen_fade_out)

            finish()
        }, LOAD_SCREEN_TIME_OUT)
    }

    private fun setProgressAnimation() { //set progress for progressbar animation
        val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, progressBar.max)
        progressAnimator.duration = LOAD_SCREEN_TIME_OUT
        progressAnimator.interpolator = LinearInterpolator()
        progressAnimator.start()
    }

    companion object {
        private const val LOAD_SCREEN_TIME_OUT: Long = 8000 // = 8 sec
    }
}