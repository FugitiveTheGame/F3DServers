package com.darkrockstudios.apps.f3dservers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darkrockstudios.apps.f3dservers.databinding.ServerListItemBinding


class ServerListAdapter(private val servers: List<Server>): RecyclerView.Adapter<ServerListAdapter.ServerViewHolder>()
{
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder
	{
		val layoutInflater = LayoutInflater.from(parent.context)
		val itemBinding: ServerListItemBinding = ServerListItemBinding.inflate(layoutInflater, parent, false)
		return ServerViewHolder(itemBinding)
	}

	override fun getItemCount(): Int = servers.size

	override fun onBindViewHolder(holder: ServerViewHolder, position: Int) = holder.bind(servers[position])

	inner class ServerViewHolder(private val binding: ServerListItemBinding): RecyclerView.ViewHolder(binding.root)
	{
		fun bind(server: Server?)
		{
			binding.server = server
			binding.executePendingBindings()
		}
	}
}