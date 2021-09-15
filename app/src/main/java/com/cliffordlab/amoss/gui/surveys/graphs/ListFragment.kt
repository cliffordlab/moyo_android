package com.cliffordlab.amoss.gui.surveys.graphs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.extensions.DividerItemDecoration
import com.cliffordlab.amoss.gui.mom.MoyoMomActivity
import com.cliffordlab.amoss.gui.mom.MoyoMomListContent
import com.cliffordlab.amoss.helper.ListItem

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListFragment.OnListFragmentInteractionListener] interface.
 */
class ListFragment : Fragment() {
    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val args = arguments
        var activityName = ""
        val adapter: RecyclerViewAdapter
        if (args != null) {
            activityName = args.getString("activityName", "")
        }

        when (activityName) {
            MoyoMomActivity.name -> {
                adapter = RecyclerViewAdapter(MoyoMomListContent.ITEMS, listener, requireContext())
            }
            else -> {
                adapter = RecyclerViewAdapter(SurveyListContent.ITEMS, listener, requireContext())
            }
        }

        val view = inflater.inflate(R.layout.fragment_list, container, false)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> androidx.recyclerview.widget.LinearLayoutManager(context)
                    else -> androidx.recyclerview.widget.GridLayoutManager(context, columnCount)

                }
                view.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL_LIST
                    )
                )
                view.adapter = adapter
            }
        }
        return view
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: ListItem?)
    }

    companion object {
        private const val TAG = "SurveyListFragment"

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                ListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
