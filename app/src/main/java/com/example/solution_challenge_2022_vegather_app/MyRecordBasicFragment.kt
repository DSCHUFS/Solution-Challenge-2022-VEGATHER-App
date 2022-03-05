package com.example.solution_challenge_2022_vegather_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solution_challenge_2022_vegather_app.databinding.FragmentMyRecordBasicBinding
import com.example.solution_challenge_2022_vegather_app.databinding.MainPageMoreRecipeRecyclerBinding
import com.google.firebase.firestore.FirebaseFirestore

class MyRecordBasicFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentMyRecordBasicBinding.inflate(inflater,container,false)
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)

        val temp = ArrayList<String>()
        temp.add("1")
        temp.add("2")
        temp.add("3")
        temp.add("4")


        val adapter = MoreRecipeAdapter(MainPageMoreRecipeRecyclerBinding.inflate(layoutInflater))
//        adapter.appendRecipeData(RecipeInformation())
        adapter.loadParentActivity(requireContext())
        binding.recyclerView.adapter = adapter

        return binding.root
    }
}