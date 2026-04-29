package edu.uga.cs.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * HomePagerAdapter manages the fragments displayed in the ViewPager2
 * on the HomePageActivity.
 *
 * It supplies three fragments:
 * - ShoppingListFragment
 * - PurchasedListFragment
 * - SummaryFragment
 *
 * Each fragment corresponds to a tab on the home screen.
 */
public class HomePagerAdapter extends FragmentStateAdapter {

    /**
     * Constructs a HomePagerAdapter for the given activity.
     *
     * @param activity the hosting activity for the ViewPager2
     */
    public HomePagerAdapter(AppCompatActivity activity) {
        super(activity);
    }

    /**
     * Creates and returns the fragment associated with
     * the given tab position.
     *
     * Position mapping:
     * 0 = ShoppingListFragment
     * 1 = PurchasedListFragment
     * 2 = SummaryFragment
     *
     * @param position the selected tab index
     * @return the fragment for the specified position
     */
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new ShoppingListFragment();
            case 1: return new PurchasedListFragment();
            case 2: return new SummaryFragment();
            default: return new ShoppingListFragment();
        }
    }

    /**
     * Returns the total number of fragments managed
     * by this adapter.
     *
     * @return number of tabs/pages
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}