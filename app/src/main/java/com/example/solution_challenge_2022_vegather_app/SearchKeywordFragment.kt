package com.example.solution_challenge_2022_vegather_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentSearchKeywordBinding

class SearchKeywordFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentSearchKeywordBinding.inflate(inflater,container,false)
        binding.testText.text = "helloWorld!"
        return binding.root
    }

}