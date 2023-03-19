package app.moosync.moosync.ui.pages.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import app.moosync.moosync.R
import app.moosync.moosync.databinding.FragmentExploreBindingImpl
import app.moosync.moosync.ui.base.BaseFragment

class ExploreFragment: BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentExploreBindingImpl =
            DataBindingUtil.inflate(inflater, R.layout.fragment_explore, container, false)
        rootView = binding.root

        setToolbar(binding.root)
        setHeaderVisibility(View.GONE)

        return rootView
    }
}