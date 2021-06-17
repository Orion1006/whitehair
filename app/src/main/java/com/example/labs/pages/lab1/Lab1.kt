package com.example.labs.pages.lab1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.labs.R


class Lab1 : Fragment() {
    private lateinit var lab1ViewModel: Lab1ViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val root = inflater.inflate(R.layout.lab1, container, false)
        return root
    }
}

