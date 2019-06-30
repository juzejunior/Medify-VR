package br.com.spacerocket.medifyvr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val handle = Handler()
        handle.postDelayed({ goToLogin() }, 2000)
    }

    private fun goToLogin() {
        val intentLogin = Intent(this, LoginActivity::class.java)
        startActivity(intentLogin)
        finish()
    }

}
