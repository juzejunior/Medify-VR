package br.com.spacerocket.medifyvr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.spacerocket.medifyvr.adapter.PatientAdapter
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    var db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    var patients = ArrayList<Paciente>()
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        indeterminateBar.visibility = View.VISIBLE
        getPatients()
        initElements()
        initListeners()
    }

    private fun initElements() {
        mRecyclerView = findViewById<View>(R.id.rvPatients) as RecyclerView
        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.layoutManager = mLayoutManager
        mAdapter = PatientAdapter(this, patients)
        mRecyclerView!!.adapter = mAdapter
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
                        if (patient.idMedico == auth.currentUser?.uid) {
                            patients.add(patient)
                        }
                    }

                    totalPatientsTV.text = patients.size.toString()
                    if (patients.isEmpty()) {
                        noPatientsRL.visibility = View.VISIBLE
                        rvPatients.visibility = View.GONE
                    } else {
                        noPatientsRL.visibility = View.GONE
                        rvPatients.visibility = View.VISIBLE
                    }
                    mAdapter?.notifyDataSetChanged()
                } else {
                    Log.d("ERRO GET PATIENTS", "Error getting documents: ", task.exception)
                }
            }
    }

    fun goToChooseProcedureScreen(patient: Paciente) {
       val intent = Intent(this, ChooseProcedureActivity::class.java)
       val jsonPatient = Gson().toJson(patient)
        intent.putExtra("PATIENT", jsonPatient)
       startActivity(intent)
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
