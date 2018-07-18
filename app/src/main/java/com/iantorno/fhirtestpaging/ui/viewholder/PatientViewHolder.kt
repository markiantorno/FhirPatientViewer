package com.iantorno.fhirtestpaging.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.iantorno.fhirtestpaging.R
import com.iantorno.fhirtestpaging.extensions.inflate
import com.iantorno.fhirtestpaging.objects.Resource
import kotlinx.android.synthetic.main.patient_card.view.*

class PatientViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(patient: Resource?) = with(itemView) {
        content_active.text = patient?.active?.toString() ?: " - "
        content_birthday.text = patient?.birthDate ?: " - "
        content_gender.text = patient?.gender ?: " - "
        content_id.text = patient?.id ?: " - "
    }

    companion object {
        fun create(parent: ViewGroup): PatientViewHolder {
            val view = parent.inflate(R.layout.patient_card)
            return PatientViewHolder(view)
        }
    }


}