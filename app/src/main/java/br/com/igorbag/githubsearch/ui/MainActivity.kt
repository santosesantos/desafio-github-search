package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var llNoInternet: LinearLayout
    lateinit var llUserNotFound: LinearLayout
    lateinit var pbLoader: ProgressBar
    lateinit var githubApi: GitHubService

    val KEY_USERNAME = "username"

    lateinit var sharedPref: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inits
        setupView()
        setupSharedPrefs()
        showUserName()
        setupRetrofit()

        getAllReposByUserName()
    }

    override fun onResume() {
        super.onResume()
        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        this.nomeUsuario = findViewById(R.id.et_nome_usuario)
        this.btnConfirmar = findViewById(R.id.btn_confirmar)
        this.listaRepositories = findViewById(R.id.rv_lista_repositories)
        this.llNoInternet = findViewById(R.id.ll_no_internet)
        this.llUserNotFound = findViewById(R.id.ll_404)
        this.pbLoader = findViewById(R.id.pb_loader)

        setupListeners()
    }

    private fun setupSharedPrefs() {
        sharedPref = getSharedPreferences("git_search", MODE_PRIVATE)
        prefEditor = sharedPref.edit()
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        this.btnConfirmar.setOnClickListener {
            saveUserLocal()
            showUserName()
            if (!username.isEmpty()) {
                getAllReposByUserName()
            }
        }
    }

    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        val username = this.nomeUsuario.text.toString()

        if (username.isEmpty()) {
            Toast.makeText(
                this, "Please, specify an GitHub username.", Toast.LENGTH_LONG
            ).show()
            return
        }

        prefEditor.putString(KEY_USERNAME, username)
        prefEditor.apply()
        Log.i("SharedPreferences", "Username saved successfully.")
    }

    private fun showUserName() {
        username = sharedPref.getString(KEY_USERNAME, "") ?: ""
        this.nomeUsuario.setText(username)
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {
        val retrofit = Retrofit.Builder().baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName() {
        this.listaRepositories.visibility = View.GONE
        this.llNoInternet.visibility = View.GONE
        this.llUserNotFound.visibility = View.GONE
        this.pbLoader.visibility = View.VISIBLE

        if (!checkInternet()) {
            this.pbLoader.visibility = View.GONE
            this.llNoInternet.visibility = View.VISIBLE
            return
        }

        githubApi.getAllRepositoriesByUser(username).enqueue(object : Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>, response: Response<List<Repository>>
            ) {
                this@MainActivity.pbLoader.visibility = View.GONE

                if (response.isSuccessful) {
                    this@MainActivity.listaRepositories.visibility = View.VISIBLE

                    response.body()?.let {
                        setupAdapter(it)
                    }
                } else {
                    this@MainActivity.llUserNotFound.visibility = View.VISIBLE
                    Toast.makeText(
                        this@MainActivity,
                        "Something went wrong: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                this@MainActivity.pbLoader.visibility = View.GONE
                this@MainActivity.llUserNotFound.visibility = View.VISIBLE

                t.printStackTrace()
                Toast.makeText(
                    this@MainActivity, "Something went wrong: ${t.message}", Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun checkInternet(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {
        this.listaRepositories.adapter = RepositoryAdapter(list)
    }

    /* santosesantos -> Eu acabei fazendo sozinho esses métodos, direto no adapter, pois fiquei
        curioso enquanto estava construindo ele e não sabia que existiam esses daqui. Acho que a
        experiência foi até MELHOR porque eu mesmo busquei como fazer na própria documentação do
        Android.
    */
    // Metodo responsavel por compartilhar o link do repositorio selecionado
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse(urlRepository)
            )
        )

    }

}