package com.mapmitra.mapmitra.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mapmitra.mapmitra.R
import com.mapmitra.mapmitra.models.Developers
import kotlinx.android.synthetic.main.item_developer.view.*


class DevsAdapter(val context: Context , val developers: List<Developers>) :
    RecyclerView.Adapter<DevsAdapter.ViewHolder>() {

    override fun getItemCount() = developers.size


    override fun onBindViewHolder(holder: ViewHolder , position: Int) {
        holder.Bind(developers[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun Bind(Developers: Developers) {
            val countryCode = "+91"

            itemView.dev_name.text = Developers.name
            itemView.dev_mail.text = Developers.Email
            itemView.dev_phone.text = countryCode + Developers.contact_number.toString().toLong()
            itemView.dev_linkedIn.text = Developers.linkedIn
            Glide.with(context).load(Developers.image_url).into(itemView.dev_Image)

            val linkedInLink = itemView.findViewById<TextView>(R.id.dev_linkedIn)
            val mail = itemView.findViewById<TextView>(R.id.dev_mail)
            val mobile = itemView.findViewById<TextView>(R.id.dev_phone)

            mail.setOnClickListener {
                try {
                    val intent =
                        Intent(Intent.ACTION_VIEW , Uri.parse("mailto:" + mail.text.toString()))
                    intent.putExtra(Intent.EXTRA_SUBJECT , "your_subject")
                    intent.putExtra(Intent.EXTRA_TEXT , "your_text")
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context,"No Mail Client Found",Toast.LENGTH_SHORT).show()
                }
            }

            linkedInLink.setOnClickListener {
                var url = linkedInLink.text.toString()
                val browserIntent =
                    Intent(Intent.ACTION_VIEW , Uri.parse(url))
                context.startActivity(browserIntent)
            }

            mobile.setOnClickListener {
                val number = mobile.text.toString()
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$number")
                context.startActivity(intent)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_developer , parent , false)
        return ViewHolder(view)
    }


}