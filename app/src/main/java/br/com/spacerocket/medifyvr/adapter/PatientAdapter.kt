package br.com.spacerocket.medifyvr.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import br.com.spacerocket.medifyvr.Paciente
import br.com.spacerocket.medifyvr.R

/**
 *
 * Created by junior on 03/08/17.
 */

class PatientAdapter(private val mContext: Context, listPatient: ArrayList<Paciente>) : RecyclerView.Adapter<PatientAdapter.MyViewHolder>() {
    private var listPatient = ArrayList<Paciente>()

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvNamePatient: TextView
        var tvBirthDatePatient: TextView
        var linearLayoutView: LinearLayout

        var view: View? = null

        init {
            tvNamePatient = view.findViewById<View>(R.id.patientNameTV) as TextView
            tvBirthDatePatient = view.findViewById<View>(R.id.birthDateTV) as TextView
            linearLayoutView = view.findViewById<View>(R.id.containerpatient) as LinearLayout
        }
    }

    init {
        this.listPatient = listPatient
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_patient, parent, false)

        return this.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PatientAdapter.MyViewHolder, position: Int) {
        val patient = listPatient[position]

        holder.tvNamePatient.text = patient.nome
        holder.tvBirthDatePatient.text = patient.data_nascimento

        holder.linearLayoutView.setOnClickListener {

        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return listPatient.size
    }

}
