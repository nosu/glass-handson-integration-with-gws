package com.example.android.glass.cardsample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.example.android.glass.cardsample.R;


public class ImageLayoutFragment extends BaseFragment {
  public static ImageLayoutFragment newInstance(String imageUrl) {
    final ImageLayoutFragment fragment = new ImageLayoutFragment();

    final Bundle args = new Bundle();
    args.putString("imageUrl", imageUrl);
    fragment.setArguments(args);

    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.image_layout, container, false);
    ImageView imageView = view.findViewById(R.id.slideImageView);
    String imageUrl = getArguments().getString("imageUrl");

    Glide.with(this)
            .load(imageUrl)
            .into(imageView);

    return view;
  }
}
