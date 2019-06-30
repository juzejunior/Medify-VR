package br.com.spacerocket.medifyvr

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.dialog_light.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        showDialog()
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_light)
        dialog.setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        dialog.title.text = "Parabéns você conseguiu coletar "
        dialog.tvTotalPoint.text = intent.getIntExtra("RESULT", 0).toString()+" elementos!"

        dialog.bt_keep.setOnClickListener {
           startActivity(Intent(this, MainActivity::class.java))
           finish()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }
}
