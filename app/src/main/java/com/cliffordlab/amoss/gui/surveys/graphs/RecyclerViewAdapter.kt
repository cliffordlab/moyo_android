package com.cliffordlab.amoss.gui.surveys.graphs

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.app.AmossApplication
import com.cliffordlab.amoss.gui.surveys.graphs.ListFragment.OnListFragmentInteractionListener
import com.cliffordlab.amoss.helper.ListItem
import com.cliffordlab.amoss.settings.SettingsUtil
import kotlinx.android.synthetic.main.fragment_survey.view.*


/**
 * [RecyclerView.Adapter] that can display a [ListItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class RecyclerViewAdapter(
        private val mValues: List<ListItem>,
        private val mListener: OnListFragmentInteractionListener?, private val mContext: Context
)
    : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "MySurveyRecyclerViewAda"
    }

    private var mExpandedPosition = -1
    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_survey, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = mValues[position]
        holder.mIdView.setImageDrawable(item.drawable)
        holder.mContentView.text = item.details

        if (SettingsUtil(mContext).studyId == "MME") {
            holder.mSurveyButton.text = "Record"
        }


        val isExpanded = position === mExpandedPosition
        holder.mOptions.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.itemView.isActivated = isExpanded
        holder.itemView.setOnClickListener {
            mExpandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(position)
        }
        holder.mGraphButton.setOnClickListener {
            Log.i(TAG, "onBindViewHolder: " + item.surveyGraphIntent.toString())
            val intent = item.surveyGraphIntent
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            AmossApplication.context.startActivity(intent)
        }
        holder.mSurveyButton.setOnClickListener {
            AmossApplication.context.startActivity(item.takeSurveyIntent)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: ImageView = mView.icon
        val mContentView: TextView = mView.title
        val mOptions: ConstraintLayout = mView.options
        val mGraphButton: Button = mView.graphButton
        val mSurveyButton: Button = mView.enterSurvey

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }

    //declare interface
    private var onClick: OnItemClicked? = null

    //make interface like this
    interface OnItemClicked {
        fun onItemClick(position: Int)
    }

    fun setOnClick(onClick: OnItemClicked) {
        this.onClick = onClick
    }

}
