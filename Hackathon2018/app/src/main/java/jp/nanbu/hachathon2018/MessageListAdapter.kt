package jp.nanbu.hachathon2018

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.amazonaws.models.nosql.MessagesDO

class MessageListAdapter(context: Context, @LayoutRes resource: Int, items: List<MessagesDO>) :
        ArrayAdapter<MessagesDO>(context, resource, items) {

    private var mailAddressTextView: TextView? = null
    private var messageTextView: TextView? = null
    private val mInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mResource = resource
    private val mItems = items

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: mInflater.inflate(mResource, null)

        mailAddressTextView = view.findViewById(R.id.mailAddress)
        messageTextView = view.findViewById(R.id.messageText)

        mailAddressTextView?.text = "To: " + mItems.get(position).receiver
        messageTextView?.text = "遺言: " + mItems.get(position).message

        return view
    }
}