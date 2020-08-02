package com.darkrockstudios.apps.f3dservers

data class Server(
		var ip: String,
		var port: Int,
		var name: String,
		var game_version: Int,
		var is_joinable: Boolean,
		var last_seen: String)