package com.example.androidcode;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MovieFragment();
            case 1:
                return ContentFragment.newInstance("Lịch sử đặt vé");
            case 2:
                return ContentFragment.newInstance("Cài đặt & Thông báo");
            default:
                return new MovieFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}