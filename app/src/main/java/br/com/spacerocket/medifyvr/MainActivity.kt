package br.com.spacerocket.medifyvr

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.spacerocket.medifyvr.adapter.PatientAdapter
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_light.bt_keep
import kotlinx.android.synthetic.main.dialog_register_patient.*
import io.cubos.r2d2lib.insertMask


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
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(qs: QuerySnapshot?, erro: FirebaseFirestoreException?) {
                    patients.clear()
                    indeterminateBar.visibility = View.INVISIBLE
                    if (erro != null) {
                        return
                    }

                    for (doc in qs!!) {
                      if (doc.get("idMedico") == auth.currentUser?.uid) {
                         var patient: Paciente = Paciente()
                          patient.nome = (doc.get("nome") as String)
                          patient.telefone = (doc.get("telefone") as String)
                          patient.idMedico = (doc.get("idMedico") as String)
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
                }
            })
            /*.addOnCompleteListener { task ->
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
            }*/
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

        fabBTN.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
            dialog.setContentView(R.layout.dialog_register_patient)
            dialog.setCancelable(true)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT

            dialog.patientPhoneET.insertMask("(##) #####-####")

            dialog.bt_keep.setOnClickListener {
                if (dialog.patientNameET.getText().toString() == "") {
                    Toast.makeText(this, "Ops! Você esqueceu de informar o nome do paciente!", Toast.LENGTH_SHORT).show()
                } else {
                    val data = HashMap<String, Any>()
                    var phone = ""
                    if (dialog.patientPhoneET.getText().toString() != "") {
                       phone  = dialog.patientPhoneET.getText().toString()
                    }
                    data.put("nome", dialog.patientNameET.getText().toString())
                    data.put("telefone", dialog.patientPhoneET.getText().toString())
                    data.put("idMedico", auth.currentUser!!.uid)

                    db.collection("pacientes").add(data)
                        .addOnSuccessListener {
                            dialog.dismiss()
                            Toast.makeText(this@MainActivity, "Novo paciente adicionado!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Opa ocorreu um erro, tente novamente!", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            dialog.bt_close.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            dialog.window!!.attributes = lp
        }
    }
}
