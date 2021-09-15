package com.cliffordlab.amoss.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.R

import com.cliffordlab.amoss.gui.environment.EnvironmentFragment.OnListFragmentInteractionListener
import com.cliffordlab.amoss.gui.environment.EnvironmentListContent
import com.cliffordlab.amoss.gui.environment.EnvironmentListContent.EnvironmentItem

import kotlinx.android.synthetic.main.fragment_environment.view.*

/**
 * [RecyclerView.Adapter] that can display a [EnvironmentItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class EnvironmentAdapter(
    content: EnvironmentListContent,
    private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<EnvironmentAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private var mValues: List<EnvironmentItem> = content.getItems()

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as EnvironmentItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_environment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIcon.setImageDrawable(item.icon)
        holder.mTitle.text = item.title
        holder.mSummary.text = item.summary

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mIcon: ImageView = mView.icon
        var mTitle: TextView = mView.title
        var mSummary: TextView = mView.summary
    }
}
