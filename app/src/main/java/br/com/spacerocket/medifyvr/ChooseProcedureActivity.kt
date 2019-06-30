package br.com.spacerocket.medifyvr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_choose_procedure.*

class ChooseProcedureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_procedure)
        initListeners()
    }

    fun initListeners() {
        startBTN.setOnClickListener {
            val intent = Intent(this, CatchBallSessionActivity::class.java)
            intent.putExtra("PATIENT", intent.getStringExtra("PATIENT"))
            startActivity(intent)
        }
    }
}
