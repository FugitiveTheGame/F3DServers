package com.darkrockstudios.apps.f3dservers

import android.util.Log
import com.beust.klaxon.Klaxon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

object API
{
	private val OFFICIAL_SERVERS = hashSetOf("Fugitive Official #1", "Fugitive Official #2")

	private val client = OkHttpClient()
	private val parser = Klaxon()

	fun missingOfficialServers(servers: Collection<Server>): List<String>
	{
		val missingServers = mutableListOf<String>()
		OFFICIAL_SERVERS.onEach { official ->
			if(servers.firstOrNull { it.name == official } == null)
				missingServers.add(official)
		}

		return missingServers
	}

	suspend fun getServers(): List<Server>?
	{
		var servers: List<Server>? = null

		withContext(Dispatchers.IO) {
			val request = Request.Builder()
					.url("http://repository.fugitivethegame.online/servers")
					.build()

			try
			{
				client.newCall(request).execute().use { response: Response ->
					if(!response.isSuccessful) throw IOException("Unexpected code $response")

					response.body?.let { responseBody ->
						val json = responseBody.string()
						servers = parser.parseArray(json)
					}
				}
			}
			catch(e: IOException)
			{
				Log.e("abrown", "HTTP Error", e)
			}
		}

		return servers
	}

	suspend fun restartServer(auth: String, serverId: String): Boolean
	{
		var success = false

		withContext(Dispatchers.IO) {
			val url = "http://gameserver00.fugitivethegame.online/restart?auth=$auth&server=$serverId"

			val request = Request.Builder()
					.url(url)
					.build()

			try
			{
				client.newCall(request).execute().use { response: Response ->
					success = response.isSuccessful
				}
			}
			catch(e: IOException)
			{
				Log.e("abrown", "HTTP Error", e)
				success = false
			}
		}

		return success
	}
}