package edu.uga.cs.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

/**
 * HomePageActivity serves as the main screen of the Shopping List application.
 *
 * It provides:
 * - A navigation drawer for menu actions such as logout
 * - A toolbar with action items
 * - A tabbed interface using ViewPager2 for navigating between fragments
 *
 * Tabs include Shopping, Purchased, and Summary views.
 */
public class HomePageActivity extends AppCompatActivity {
    /** Layout used for the side navigation drawer. */
    DrawerLayout drawerLayout;
    /** Navigation menu displayed inside the drawer. */
    NavigationView navigationView;
    /** Tab layout used for switching between fragments. */
    TabLayout tabLayout;
    /** ViewPager used to host tab fragments. */
    ViewPager2 viewPager;
    /** Toggle button that opens and closes the navigation drawer. */
    ActionBarDrawerToggle drawerToggle;

    /**
     * Called when the activity is first created.
     *
     * Initializes the toolbar, drawer navigation, tab layout,
     * and ViewPager adapter.
     *
     * @param savedInstanceState previously saved state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Set up the drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Handle navigation view item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.logout) {
                logout();
                return true;
            }
            return false;
        });

        // ---------------- TABS ----------------
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new HomePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) tab.setText("Shopping");
                    else if (position == 1) tab.setText("Purchased");
                    else tab.setText("Summary");
                }
        ).attach();
    }

    /**
     * Inflates the options menu shown in the toolbar.
     *
     * @param menu the options menu
     * @return true if the menu is created successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    /**
     * Handles toolbar menu item selections.
     *
     * Supports:
     * - Drawer toggle button
     * - Logout action
     * - Cart navigation action
     *
     * @param item selected menu item
     * @return true if the selection was handled
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.logout) {
            logout();
            return true;
        } else if (id == R.id.action_cart) {
            Intent intent = new Intent(this, PersonalListActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Signs the user out of Firebase authentication
     * and returns them to the main login screen.
     *
     * Clears the activity stack so the user cannot
     * navigate back after logging out.
     */
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomePageActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}