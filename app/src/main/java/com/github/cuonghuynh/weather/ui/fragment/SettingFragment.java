package com.github.cuonghuynh.weather.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.github.cuonghuynh.weather.R;
import com.github.cuonghuynh.weather.databinding.FragmentMultipleDaysBinding;
import com.github.cuonghuynh.weather.databinding.FragmentSettingBinding;
import com.github.cuonghuynh.weather.utils.AppUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    FragmentSettingBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        if (AppUtil.getLanguageSelected()){
            binding.radioGroup.clearCheck();
            binding.radioVietnamese.toggle();
        } else {
            binding.radioGroup.clearCheck();
            binding.radioEnglish.toggle();
        }
        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radio_english:
                        AppUtil.setLanguageSelected(false);
                        break;
                    case R.id.radio_vietnamese:
                        AppUtil.setLanguageSelected(true);
                        break;
                }
            }
        });
        return view;
    }
}