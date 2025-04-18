package com.example.lab1.Media;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.lab1.Fragment.AudioFragment;
import com.example.lab1.Fragment.DownloadFragment;
import com.example.lab1.Fragment.VideoFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new AudioFragment();
            case 2:
                return new DownloadFragment();
            default:
                return new VideoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}