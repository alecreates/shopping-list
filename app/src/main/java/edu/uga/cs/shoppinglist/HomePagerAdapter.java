package edu.uga.cs.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new ShoppingListFragment();
            case 1: return new PurchasedListFragment();
            case 2: return new SummaryFragment();
            default: return new ShoppingListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}