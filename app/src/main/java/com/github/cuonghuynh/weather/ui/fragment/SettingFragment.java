package com.github.cuonghuynh.weather.ui.fragment;

import static com.github.cuonghuynh.weather.utils.AppUtil.setTextWithLinks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.cuonghuynh.weather.R;
import com.github.cuonghuynh.weather.databinding.FragmentMultipleDaysBinding;
import com.github.cuonghuynh.weather.databinding.FragmentSettingBinding;
import com.github.cuonghuynh.weather.utils.AppUtil;
import com.github.cuonghuynh.weather.utils.ViewAnimation;

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

    @SuppressLint("StringFormatInvalid")
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
        initInfo();
        String versionName = "1.0.0";
        setTextWithLinks(view.findViewById(R.id.text_application_info), getString(R.string.application_info_text, versionName));
        setTextWithLinks(view.findViewById(R.id.text_developer_info), getString(R.string.developer_info_text));
        setTextWithLinks(view.findViewById(R.id.text_design_api), getString(R.string.design_api_text));
        setTextWithLinks(view.findViewById(R.id.text_libraries), getString(R.string.libraries_text));
        setTextWithLinks(view.findViewById(R.id.text_license), getString(R.string.license_text));
        return view;
    }

    private void setTextWithLinks(TextView textView, String htmlText) {
        AppUtil.setTextWithLinks(textView, AppUtil.fromHtml(htmlText));
    }

    private void initInfo(){
        binding.toggleInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleView();
            }
        });
        binding.toggleInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleView();
            }
        });
    }

    private void toggleView() {
        boolean show = toggleArrow(binding.toggleInfoButton);
        if (show) {
            ViewAnimation.expand(binding.expandLayout, new ViewAnimation.AnimListener() {
                @Override
                public void onFinish() {
                }
            });
        } else {
            ViewAnimation.collapse(binding.expandLayout);
        }
    }

    private boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(180);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }
}