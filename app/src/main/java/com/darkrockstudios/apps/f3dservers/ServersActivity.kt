package com.darkrockstudios.apps.f3dservers

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_servers.*
import kotlinx.android.synthetic.main.server_list.*
import kotlinx.coroutines.*
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
			Snackbar.make(view, R.string.refresh, Snackbar.LENGTH_SHORT).show()
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

	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		val inflater: MenuInflater = menuInflater
		inflater.inflate(R.menu.main_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		return when(item.itemId)
		{
			R.id.menu_restart_server_1 ->
			{
				restartServer(1)
				true
			}
			R.id.menu_restart_server_2 ->
			{
				restartServer(2)
				true
			}
			R.id.menu_set_password ->
			{
				PasswordFragment.show(this)
				true
			}
			else                       -> super.onOptionsItemSelected(item)
		}
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

			withContext(Dispatchers.Main) {
				Snackbar.make(root_servers_view, R.string.worker_scheduled, Snackbar.LENGTH_LONG).show()
			}
		}
	}

	private fun removeWorker()
	{
		workManager.cancelUniqueWork(WORK_NAME)
		Snackbar.make(root_servers_view, R.string.worker_removed, Snackbar.LENGTH_LONG).show()
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
		servers.clear()
		adapter.notifyDataSetChanged()
		progress_bar.visibility = View.VISIBLE

		MainScope().launch {
			refresh()
		}
	}

	private fun restartServer(serverId: Int)
	{
		val auth = PreferenceManager.getDefaultSharedPreferences(this).getString(PasswordFragment.KEY_PASSWORD, null)
		val server = "official$serverId"

		if(auth != null)
		{
			MainScope().launch {
				val success = API.restartServer(auth, server)

				withContext(Dispatchers.Main) {
					if(success)
					{
						Snackbar.make(root_servers_view, "Server restarted successfully", Snackbar.LENGTH_LONG).show()
					}
					else
					{
						Snackbar.make(root_servers_view, "Failed to restart server", Snackbar.LENGTH_LONG).show()
					}
				}

				if(success)
				{
					delay(1000)
					doRefresh()
				}
			}
		}
		else
		{
			Snackbar.make(root_servers_view, "Password not set", Snackbar.LENGTH_LONG).show()
		}
	}

	private suspend fun refresh()
	{
		API.getServers()?.let { newServers ->
			servers.clear()
			servers.addAll(newServers)
			adapter.notifyDataSetChanged()
		}

		progress_bar.visibility = View.GONE
	}
}