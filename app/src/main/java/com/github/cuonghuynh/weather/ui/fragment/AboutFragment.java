package com.github.cuonghuynh.weather.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.cuonghuynh.weather.databinding.FragmentAboutBinding;


public class AboutFragment extends Fragment {

  private FragmentAboutBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    binding = FragmentAboutBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }


}
