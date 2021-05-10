package com.darkrockstudios.apps.f3dservers

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.password_fragment.*

class PasswordFragment: DialogFragment(R.layout.password_fragment)
{
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		password_save_button.setOnClickListener {
			savePassword()
		}
	}

	private fun savePassword()
	{
		val newPassword = server_password_edit?.text?.toString()
		if(newPassword?.isNotBlank() == true)
		{
			PreferenceManager.getDefaultSharedPreferences(context).edit {
				putString(KEY_PASSWORD, newPassword)
			}

			Snackbar.make(server_password_container, "", Snackbar.LENGTH_LONG).show()
			dismiss()
		}
		else
		{
			server_password_edit.error = "Must not be blank"
		}
	}

	companion object
	{
		const val KEY_PASSWORD = "server_password"

		fun show(activity: AppCompatActivity)
		{
			PasswordFragment().show(activity.supportFragmentManager, "PasswordFragment")
		}
	}
}