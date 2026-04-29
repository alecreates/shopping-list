package edu.uga.cs.shoppinglist;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

/**
 * PersonalListActivity displays the user's personal cart list.
 *
 * It hosts the PersonalListFragment inside a fragment container
 * and provides a toolbar with an Up button for navigation back
 * to the previous screen.
 */
public class PersonalListActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     *
     * Initializes the layout, configures the toolbar,
     * enables back navigation, and loads the
     * PersonalListFragment if this is the first creation.
     *
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Cart");
        }

        if (savedInstanceState == null) {
            PersonalListFragment fragment = new PersonalListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }

    /**
     * Handles toolbar Up button navigation.
     *
     * Returns the user to the previous activity.
     *
     * @return true after handling navigation
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}