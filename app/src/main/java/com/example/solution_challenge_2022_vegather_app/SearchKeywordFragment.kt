package com.example.solution_challenge_2022_vegather_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentSearchKeywordBinding
import com.example.solution_challenge_2022_vegather_app.databinding.SearchAutocompleteRecyclerBinding

class SearchKeywordFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("text","호출")
        // Inflate the layout for this fragment
        val binding = FragmentSearchKeywordBinding.inflate(inflater,container,false)
        val bundle : Bundle? = arguments
        val foodNameList = bundle?.getStringArrayList("foodNameList")
        val startIndex = bundle?.getIntegerArrayList("startIndex")
        val inputValueLength = bundle?.getInt("inputSearchLength")

        binding.searchKeywordList.layoutManager = LinearLayoutManager(this.context)
        val adapter = AutocompleteSearchAdapter(SearchAutocompleteRecyclerBinding.inflate(layoutInflater))

        if( foodNameList!=null && startIndex!=null && inputValueLength!=null ){
            adapter.setData(foodNameList,startIndex,inputValueLength)
            adapter.loadParentActivity(requireContext())
            binding.searchKeywordList.adapter = adapter
        }

        // 검색어를 보기 위해 화면을 터치하는 상황이 발생한다면 방해가 되지 않도록 키보드를 내려야 한다.
        binding.searchKeywordList.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    hideKeyBoard()
                }
            }
            false
        }

        return binding.root
    }

    private fun hideKeyBoard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }
}

