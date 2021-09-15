package com.cliffordlab.amoss.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.surveys.GAD7Activity
import com.cliffordlab.amoss.gui.surveys.GSQActivity
import com.cliffordlab.amoss.gui.mom.MomSymptomsActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class SurveyAdapter(context: Context, private val dataSet: List<Triple<String, List<String>, Int?>>, private val surveyActivity: String) :
    RecyclerView.Adapter<SurveyAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "SurveyViewAdapter"
    }

    val surveyAnswerMap = mutableMapOf<String, Any?>()
    val mContext = context

    /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioGroup: RadioGroup = view.findViewById(R.id.level_group1)
        val radioGroupButton0: RadioButton = view.findViewById(R.id.levelGroup1_0)
        val radioGroupButton1: RadioButton = view.findViewById(R.id.levelGroup1_1)
        val radioGroupButton2: RadioButton = view.findViewById(R.id.levelGroup1_2)
        val radioGroupButton3: RadioButton = view.findViewById(R.id.levelGroup1_3)
        val radioGroupButton4: RadioButton = view.findViewById(R.id.levelGroup1_4)
        val submitBtn: Button = view.findViewById(R.id.submitBtn)
        val titleView: TextView = view.findViewById(R.id.titleView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textInputLayout: TextInputLayout = view.findViewById(R.id.textInputLayout)
        val textInputField: TextInputEditText = view.findViewById(R.id.textInputEditText)
    }

    fun getSurveyResults(): MutableMap<String, Any?> {
        return this.surveyAnswerMap
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.survery_row_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.titleView.text = dataSet[position].first
        setImageView(viewHolder, position)
        val possibleOptions = dataSet[position].second

        when (possibleOptions.size) {
            1 -> {
                viewHolder.radioGroupButton0.visibility = GONE
                viewHolder.radioGroupButton1.visibility = GONE
                viewHolder.radioGroupButton2.visibility = GONE
                viewHolder.radioGroupButton3.visibility = GONE
                viewHolder.radioGroupButton4.visibility = GONE
            }
            2 -> {
                viewHolder.radioGroupButton0.text = possibleOptions[0]
                viewHolder.radioGroupButton1.text = possibleOptions[1]
                viewHolder.radioGroupButton2.visibility = GONE
                viewHolder.radioGroupButton3.visibility = GONE
                viewHolder.radioGroupButton4.visibility = GONE
            }
            3 -> {
                viewHolder.radioGroupButton0.text = possibleOptions[0]
                viewHolder.radioGroupButton1.text = possibleOptions[1]
                viewHolder.radioGroupButton2.text = possibleOptions[2]
                viewHolder.radioGroupButton3.visibility = GONE
                viewHolder.radioGroupButton4.visibility = GONE
            }
            4 -> {
                viewHolder.radioGroupButton0.text = possibleOptions[0]
                viewHolder.radioGroupButton1.text = possibleOptions[1]
                viewHolder.radioGroupButton2.text = possibleOptions[2]
                viewHolder.radioGroupButton3.text = possibleOptions[3]
                viewHolder.radioGroupButton4.visibility = GONE
            }
            else -> {
                viewHolder.radioGroupButton0.text = possibleOptions[0]
                viewHolder.radioGroupButton1.text = possibleOptions[1]
                viewHolder.radioGroupButton2.text = possibleOptions[2]
                viewHolder.radioGroupButton3.text = possibleOptions[3]
                viewHolder.radioGroupButton4.text = possibleOptions[4]
            }
        }

        viewHolder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.levelGroup1_0 -> {
                    surveyAnswerMap[viewHolder.titleView.text.toString()] = 0
                }
                R.id.levelGroup1_1 -> {
                    surveyAnswerMap[viewHolder.titleView.text.toString()] = 1
                }
                R.id.levelGroup1_2 -> {
                    surveyAnswerMap[viewHolder.titleView.text.toString()] = 2
                }
                R.id.levelGroup1_3 -> {
                    surveyAnswerMap[viewHolder.titleView.text.toString()] = 3
                }
                R.id.levelGroup1_4 -> {
                    surveyAnswerMap[viewHolder.titleView.text.toString()] = 4
                }
            }
            setCheckedListenerEvents(viewHolder)
        }
        setSubmitBtn(viewHolder, position)
    }

    private fun setImageView(viewHolder: ViewHolder, position: Int) {
        when (surveyActivity) {
            MomSymptomsActivity.TAG -> {
                viewHolder.imageView.visibility = VISIBLE
                viewHolder.imageView.background = ContextCompat.getDrawable(mContext, dataSet[position].third!!)
            }
        }
    }

    private fun setCheckedListenerEvents(viewHolder: ViewHolder) {
        when (surveyActivity) {
            GAD7Activity.TAG -> {
                if (surveyAnswerMap.size == dataSet.size) {
                    viewHolder.submitBtn.isEnabled = true
                }
            }
            MomSymptomsActivity.TAG -> {
                if (surveyAnswerMap.size == dataSet.size) {
                    viewHolder.submitBtn.isEnabled = true
                }
            }
        }
    }

    private fun setSubmitBtn(viewHolder: ViewHolder, position: Int) {
        when (surveyActivity) {
            GAD7Activity.TAG -> {
                if (position == dataSet.size -1) {
                    viewHolder.submitBtn.visibility = VISIBLE
                    viewHolder.textInputLayout.visibility = GONE
                }
            }
            GSQActivity.TAG -> {
                if (position == dataSet.size -1) {
                    viewHolder.submitBtn.visibility = VISIBLE
                    viewHolder.textInputLayout.visibility = VISIBLE
                }
                viewHolder.textInputField.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {
                        surveyAnswerMap[viewHolder.titleView.text.toString()] = viewHolder.textInputField.editableText.toString()
                    }

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (surveyAnswerMap.size == dataSet.size - 1) {
                            viewHolder.submitBtn.isEnabled = true
                        }
                    }
                })
            }
            else -> {
                if (position == dataSet.size -1) {
                    viewHolder.submitBtn.visibility = VISIBLE
                }
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}