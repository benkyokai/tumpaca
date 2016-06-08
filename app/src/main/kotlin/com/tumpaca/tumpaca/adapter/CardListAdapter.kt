package com.tumpaca.tumpaca.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.tumblr.jumblr.JumblrClient
import com.tumblr.jumblr.types.*
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.util.AsyncTaskHelper
import com.tumpaca.tumpaca.util.Credentials
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 * Created by yabu on 2016/06/07.
 */


class CardListAdapter(ctx: Context): ArrayAdapter<Post>(ctx, 0) {

    val mInflater = LayoutInflater.from(ctx)
    val packageManager = ctx.packageManager

    val credentialsFile = "credentials.properties"
    val consumerKeyProp = "tumblr.consumer.key"
    val consumerSecretProp = "tumblr.consumer.secret"
    val authTokenProp = "auth.token"
    val authTokenSecretProp = "auth.token.secret"

    val credentials = Credentials()

    fun loadCredentials() {
        val authProps = Properties()

        context.resources.openRawResource(R.raw.auth).use { authProps.load(it) }
        credentials.consumerKey = String(Base64.decode(authProps.get(consumerKeyProp) as String, Base64.DEFAULT))
        credentials.consumerSecret = String(Base64.decode(authProps.get(consumerSecretProp) as String, Base64.DEFAULT))

        try {
            context.openFileInput(credentialsFile).use { authProps.load(it) }
            authProps.get(authTokenProp)?.let {
                credentials.authToken = String(Base64.decode(it as String, Base64.DEFAULT))
            }
            authProps.get(authTokenSecretProp)?.let {
                credentials.authTokenSecret = String(Base64.decode(it as String, Base64.DEFAULT))
            }
        } catch (e: FileNotFoundException) {
            // まだ認証したことがないのでスルー
        }
    }

    fun getClient(): JumblrClient {
        loadCredentials()
        return JumblrClient(credentials.consumerKey, credentials.consumerSecret,
                credentials.authToken, credentials.authTokenSecret)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: mInflater.inflate(R.layout.adapter_list_item_card, parent, false)

        val post = getItem(position)
        val tv = view.findViewById(R.id.title) as TextView
        val subTextView = view.findViewById(R.id.sub) as WebView
        val imageView = view.findViewById(R.id.icon) as ImageView

        tv.setText(post.type.value)
        tv.setTextColor(Color.BLACK)

        val mimeType = "text/html; charset=utf-8"

        val client = getClient()

        AsyncTaskHelper.first<Void, Void, Blog?> {
            client.blogInfo(post.blogName)
        }.then {blog ->
            AsyncTaskHelper.first<Void, Void, URL?> {
                URL(blog?.avatar())
            }.then { avatarUrl ->
                AsyncTaskHelper.first<Void, Void, InputStream?> {
                    avatarUrl?.openStream()
                }.then { stream ->
                    AsyncTaskHelper.first<Void, Void, Bitmap?> {
                        BitmapFactory.decodeStream(stream)
                    }.then {bitmap ->
                        imageView.setImageBitmap(bitmap)
                    }
                    //stream?.close()
                }.go()
            }.go()
        }.go()

        when (post.type.value) {
            "text" -> {
                val textPost = post as TextPost
                //tv.setText(textPost.title)
                //subTextView.setText(textPost.body)
                subTextView.loadData(textPost.body, mimeType, null)
            }
            "photo" -> {
                val photoPost = post as PhotoPost
                subTextView.loadData(photoPost.caption, mimeType, null)
                //subTextView.setText(photoPost.caption)
                /*
                val details = photoPost.detail()
                val photo = photoPost.photos[0]
                when(photo.type) {
                    Photo.PhotoType.SOURCE -> {
                        val source = details.get("source") as String
                        val uri = Uri.parse(source)
                        imageView.setImageURI(uri)
                    }
                    Photo.PhotoType.FILE -> {
                        val file = details.get("file[0]") as File
                        val drawable = Drawable.createFromPath(file.absolutePath)
                        imageView.setImageDrawable(drawable)
                    }
                }*/
            }
            "quote" -> {
                val quotePost = post as QuotePost
                //tv.setText(quotePost.source)
                subTextView.loadData(quotePost.text, mimeType, null)
                //subTextView.setText(quotePost.text)
            }
            "link" -> {}
            "chat" -> {}
            "audio" -> {}
            "video" -> {}
            "answer" -> {}
            "postcard" -> {}
            else -> {}
        }

        return view
    }

}