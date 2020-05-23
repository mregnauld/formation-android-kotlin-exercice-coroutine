package com.formationandroidkt.coroutine

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity()
{

	// erreurs du job parent captées ici (hors CancellationException) :
	private val handler = CoroutineExceptionHandler { _, _ ->

		// doit être appelé depuis une coroutine main, sinon on n'est pas dans le thread UI et l'application crashe :
		CoroutineScope(Main).launch {
			afficherResultat(null)
		}
	}

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// init :
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// exemple de nombre premier de 15 chiffres :
		saisie_nombre.setText("993960000099397")
	}

	/**
	 * Listener clic bouton valider.
	 * @param view Bouton valider
	 */
	@Suppress("UNUSED_PARAMETER")
	fun onClickBoutonValider(view: View?)
	{
		// réinitialisation du message résultat :
		textview_resultat.text = ""

		// affichage de la progressbar et masquage du bouton :
		progressbar.visibility = View.VISIBLE
		bouton_valider.visibility = View.GONE

		// récupération de la saisie :
		val saisie: String = saisie_nombre.text.toString()
		if (!TextUtils.isEmpty(saisie))
		{
			// lancement du calcul :
			CoroutineScope(IO).launch(handler) {

				// calcul :
				val resultat = async { estNombrePremier(saisie)}

				// affichage du résultat :
				withContext(Main)
				{
					afficherResultat(resultat.await())
				}
			}
		}
		else
		{
			// message d'erreur si aucune saisie :
			Toast.makeText(this, R.string.main_erreur_saisie, Toast.LENGTH_LONG).show()
		}
	}

	/**
	 * Retourne true si le nombre passé en paramètre est premier.
	 * @param saisie Saisie contenant le nombre à tester
	 * @return Boolean
	 */
	private suspend fun estNombrePremier(saisie: String): Boolean
	{
		// vérification :
		val nombre = saisie.toLong()

		// calcul :
		val diviseurMax = ceil(sqrt(nombre.toDouble())).toLong()
		for (a in 2..diviseurMax)
		{
			if (nombre % a == 0L)
			{
				return false
			}

			// mise à jour de la barre de progression tous les 10000 itérations, par exemple :
			if (a % 10000 == 0L)
			{
				withContext(Main)
				{
					progressbar.progress = ((a.toDouble() / diviseurMax) * 100).toInt()
				}
			}
		}
		return true
	}

	/**
	 * Affichage du résultat du calcul
	 * @param resultat Résultat
	 */
	private fun afficherResultat(resultat: Boolean?)
	{
		// affichage du bouton et masquage de la progressbar :
		progressbar.visibility = View.GONE
		bouton_valider.visibility = View.VISIBLE

		// résultat :
		if (resultat == null)
		{
			textview_resultat.text = getString(R.string.main_erreur_calcul)
			textview_resultat.setTextColor(ContextCompat.getColor(this, R.color.couleurMainResultatNegatif))
		}
		else
		{
			if (resultat)
			{
				textview_resultat.text = getString(R.string.main_resultat_positif)
				textview_resultat.setTextColor(ContextCompat.getColor(this, R.color.couleurMainResultatPositif))
			}
			else
			{
				textview_resultat.text = getString(R.string.main_resultat_negatif)
				textview_resultat.setTextColor(ContextCompat.getColor(this, R.color.couleurMainResultatNegatif))
			}
		}
	}

}
