package com.github.cuonghuynh.weather.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.cuonghuynh.weather.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {
    private static final String TAG = "MapsActivity";
    private FragmentMapBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMapBinding.inflate(
                inflater, container, false);
        View view = binding.getRoot();
//        binding.dd1.setOnClickListener(v->{
//            Intent myIntent = new Intent(getActivity(), MapActivity.class);
//            myIntent.putExtra("key", 1); //Optional parameters
//            getActivity().startActivity(myIntent);
//        });
        //here data must be an instance of the class MarsDataProvider
        return view;
    }


}