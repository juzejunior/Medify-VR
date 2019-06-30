package br.com.spacerocket.medifyvr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    var db = FirebaseFirestore.getInstance()
    var patients = ArrayList<Paciente>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        indeterminateBar.visibility = View.VISIBLE
        getPatients()
        initListeners()
    }

    private fun getPatients() {
        db.collection("pacientes")
            .get()
            .addOnCompleteListener { task ->
                indeterminateBar.visibility = View.INVISIBLE
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d("PATIENT", document.getId() + " => " + document.getData())
                        var patient: Paciente = document.toObject(Paciente::class.java)
                        patients.add(patient)
                    }
                } else {
                    Log.d("ERRO GET PATIENTS", "Error getting documents: ", task.exception)
                }
            }
    }

    private fun initListeners() {
        /*goToTreatmentsCard.setOnClickListener {
            startActivity(Intent(this, CatchBallSessionActivity::class.java))
        }*/
        exitBTN.setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    Toast.makeText(this, "Até mais!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
        }
    }
}
