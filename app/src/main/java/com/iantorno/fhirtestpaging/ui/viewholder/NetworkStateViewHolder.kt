package com.iantorno.fhirtestpaging.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iantorno.fhirtestpaging.R
import com.iantorno.fhirtestpaging.repository.NetworkState
import com.iantorno.fhirtestpaging.repository.Status
import kotlinx.android.synthetic.main.item_network_state.view.*

/**
 * Created by Ahmed Abd-Elmeged on 2/20/2018.
 */
class NetworkStateViewHolder(val view: View, private val retryCallback: () -> Unit) : RecyclerView.ViewHolder(view) {

    init {
        itemView.retryLoadingButton.setOnClickListener { retryCallback() }
    }

    fun bind(networkState: NetworkState?) {
        //error message
        itemView.errorMessageTextView.visibility = if (networkState?.msg != null) View.VISIBLE else View.GONE
        if (networkState?.msg != null) {
            itemView.errorMessageTextView.text = networkState.msg
        }

        //loading and retry
        itemView.retryLoadingButton.visibility = if (networkState?.status == Status.FAILED) View.VISIBLE else View.GONE
        itemView.loadingProgressBar.visibility = if (networkState?.status == Status.RUNNING) View.VISIBLE else View.GONE
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.item_network_state, parent, false)
            return NetworkStateViewHolder(view, retryCallback)
        }
    }

}