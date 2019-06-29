package br.com.spacerocket.medifyvr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        setResult()
    }

    private fun setResult() {
      resultTV.text = "Parabéns o seu resultado foi "+ intent.getIntExtra("RESULT", 0)
    }
}
