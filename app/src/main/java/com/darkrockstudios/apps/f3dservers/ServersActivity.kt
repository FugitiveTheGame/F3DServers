package com.darkrockstudios.apps.f3dservers

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_servers.*
import kotlinx.android.synthetic.main.server_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration


class ServersActivity: AppCompatActivity()
{
	companion object
	{
		const val WORK_NAME = "SERVER_CHECK"
	}

	private lateinit var workManager: WorkManager
	private lateinit var adapter: ServerListAdapter
	private val servers = mutableListOf<Server>()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_servers)
		setSupportActionBar(toolbar)

		toolbar_layout.title = title
		fab.setOnClickListener { view ->
			doRefresh()
			Snackbar.make(view, "Refreshing", Snackbar.LENGTH_SHORT).show()
		}

		workManager = WorkManager.getInstance(this)

		adapter = ServerListAdapter(servers)
		server_list.adapter = adapter

		worker_scheduled.setOnCheckedChangeListener { _, isChecked ->
			if(isChecked)
			{
				scheduleWorker()
			}
			else
			{
				removeWorker()
			}
		}

		doRefresh()
	}

	override fun onResume()
	{
		super.onResume()

		updateIsScheduled()
	}

	private fun scheduleWorker()
	{
		MainScope().launch(Dispatchers.IO) {
			val constraints = Constraints.Builder()
					.setRequiresBatteryNotLow(true)
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.build()

			val request = PeriodicWorkRequest
					.Builder(ServerCheckWorker::class.java, Duration.ofHours(1), Duration.ofDays(1))
					.setConstraints(constraints)
					.build()

			workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request)
		}
	}

	private fun removeWorker()
	{
		workManager.cancelUniqueWork(WORK_NAME)
	}

	private fun updateIsScheduled()
	{
		MainScope().launch(Dispatchers.IO) {
			val workers = workManager.getWorkInfosForUniqueWork(WORK_NAME).await()
			val scheduled = workers.isNotEmpty()

			withContext(Dispatchers.Main) {
				worker_scheduled.isChecked = scheduled
			}
		}
	}

	private fun doRefresh()
	{
		MainScope().launch {
			refresh()
		}
	}

	private suspend fun refresh()
	{
		API.getServers()?.let { newServers ->
			servers.clear()
			servers.addAll(newServers)
			adapter.notifyDataSetChanged()
		}
	}
}