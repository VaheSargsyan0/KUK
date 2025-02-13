package com.project.kuk;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainMenuActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    CartFragment cartFragment = new CartFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars());
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, homeFragment)
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    if (getSupportFragmentManager().findFragmentByTag("HomeFragment") == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, homeFragment, "HomeFragment")
                                .commit();
                    }
                    return true;
                } else if (itemId == R.id.cart) {
                    if (getSupportFragmentManager().findFragmentByTag("CartFragment") == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, cartFragment, "CartFragment")
                                .commit();
                    }
                    return true;
                } else if (itemId == R.id.profile) {
                    if (getSupportFragmentManager().findFragmentByTag("ProfileFragment") == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, profileFragment, "ProfileFragment")
                                .commit();
                    }
                    return true;
                }

                return false;
            }
        });
    }
}
