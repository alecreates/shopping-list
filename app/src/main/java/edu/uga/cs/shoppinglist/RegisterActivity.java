package edu.uga.cs.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import android.util.Log;
import android.widget.Toast;

/**
 * RegisterActivity allows new users to create an account
 * using Firebase Authentication.
 *
 * Users must provide:
 * - Display name
 * - Email address
 * - Password
 *
 * After successful registration, the user's display name is
 * stored in Firebase and they are redirected to LogInActivity.
 */
public class RegisterActivity extends AppCompatActivity {

    EditText displayNameEditText;
    EditText registerEmailEditText;
    EditText registerPasswordEditText;
    Button registerButton;
    private FirebaseAuth mAuth;

    /**
     * Called when the activity is first created.
     *
     * Initializes UI components, enables edge-to-edge layout,
     * and sets up the registration button listener.
     *
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        displayNameEditText = findViewById(R.id.displayNameEditText);
        registerEmailEditText = findViewById(R.id.registerEmailEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(v -> {
            String name = displayNameEditText.getText().toString();
            String email = registerEmailEditText.getText().toString();
            String password = registerPasswordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(name, email, password);
        });
    }

    /**
     * Creates a new user account using Firebase Authentication.
     *
     * Also updates the user's Firebase profile with a display name.
     * On success, navigates to the login screen.
     *
     * @param name display name of the user
     * @param email email address
     * @param password password
     */
    private void registerUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("REGISTER", "createUserWithEmail:success");

                        FirebaseUser user = mAuth.getCurrentUser();

                        // Set display name
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                        user.updateProfile(profileUpdates);

                        Toast.makeText(RegisterActivity.this,
                                "Registration successful!",
                                Toast.LENGTH_SHORT).show();

                        // Route to LogInActivity
                        startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
                        finish();

                    } else {
                        Log.w("REGISTER", "createUserWithEmail:failure", task.getException());

                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}