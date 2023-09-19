package br.com.igorbag.githubsearch.ui.adapter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.MyViewHolder>() {

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return MyViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvRepoName.text = this.repositories[position].name

        // Exemplo de click no item
        holder.itemView.setOnClickListener {
            // Open the url
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(this.repositories[position].htmlUrl)
            )

            try {
                it.context.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                ex.printStackTrace()
                Toast.makeText(it.context, "No app found to open the url.", Toast.LENGTH_LONG)
                    .show()
            }
        }

        // Exemplo de click no btn Share
        holder.ivShare.setOnClickListener {
            // Open share options with the url
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, this@RepositoryAdapter.repositories[position].htmlUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(intent, null)

            try {
                it.context.startActivity(shareIntent)
            } catch (ex: ActivityNotFoundException) {
                ex.printStackTrace()
                Toast.makeText(it.context, "No app found to open the url.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    // Pega a quantidade de repositorios da lista
    override fun getItemCount(): Int = this.repositories.size

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvRepoName: TextView
        var ivShare: ImageView

        init {
            view.apply {
                tvRepoName = view.findViewById(R.id.tv_repo_name)
                ivShare = view.findViewById(R.id.iv_share)
            }
        }
    }
}


