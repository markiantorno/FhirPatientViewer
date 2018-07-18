package com.iantorno.fhirtestpaging

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.iantorno.fhirtestpaging.repository.NetworkState
import com.iantorno.fhirtestpaging.objects.Resource
import com.iantorno.fhirtestpaging.repository.Status
import com.iantorno.fhirtestpaging.ui.PatientAdapter
import com.iantorno.fhirtestpaging.ui.PatientViewModel
import kotlinx.android.synthetic.main.activity_patient_display.*
import kotlinx.android.synthetic.main.activity_patient_display.*
import kotlinx.android.synthetic.main.item_network_state.*

class PatientDisplayActivity : AppCompatActivity() {

    private lateinit var patientViewModel: PatientViewModel
    private lateinit var patientAdapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_display)

        patientViewModel = getViewModel()
        initAdapter()
        initSwipeToRefresh()
    }

    private fun getViewModel(): PatientViewModel {
        return ViewModelProviders.of(this).get(PatientViewModel::class.java)
    }

    private fun initAdapter() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        patientAdapter = PatientAdapter {
            patientViewModel.retry()
        }

        patient_recyclerview.layoutManager = linearLayoutManager
        patient_recyclerview.adapter = patientAdapter
        patientViewModel.patientList.observe(this, Observer<PagedList<Resource>> { patientAdapter.submitList(it) })
        patientViewModel.getNetworkState().observe(this, Observer<NetworkState> { patientAdapter.setNetworkState(it) })
    }

    /**
     * Init swipe to refresh and enable pull to refresh only when there are items in the adapter
     */
    private fun initSwipeToRefresh() {
        patientViewModel.getRefreshState().observe(this, Observer { networkState ->
            if (patientAdapter.currentList != null) {
                if (patientAdapter.currentList!!.size > 0) {
                    usersSwipeRefreshLayout.isRefreshing = networkState?.status == NetworkState.LOADING.status
                } else {
                    setInitialLoadingState(networkState)
                }
            } else {
                setInitialLoadingState(networkState)
            }
        })
        usersSwipeRefreshLayout.setOnRefreshListener { patientViewModel.refresh() }
    }

    /**
     * Show the current network state for the first load when the user list
     * in the adapter is empty and disable swipe to scroll at the first loading
     *
     * @param networkState the new network state
     */
    private fun setInitialLoadingState(networkState: NetworkState?) {
        //error message
        errorMessageTextView.visibility = if (networkState?.msg != null) View.VISIBLE else View.GONE
        if (networkState?.msg != null) {
            errorMessageTextView.text = networkState.msg
        }

        //loading and retry
        retryLoadingButton.visibility = if (networkState?.status == Status.FAILED) View.VISIBLE else View.GONE
        loadingProgressBar.visibility = if (networkState?.status == Status.RUNNING) View.VISIBLE else View.GONE

        usersSwipeRefreshLayout.isEnabled = networkState?.status == Status.SUCCESS
        retryLoadingButton.setOnClickListener { patientViewModel.retry() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_patient_display, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


}
