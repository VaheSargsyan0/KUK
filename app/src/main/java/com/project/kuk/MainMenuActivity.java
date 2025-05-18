package com.project.kuk;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainMenuActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment homeFragment = new HomeFragment();
    private Fragment cartFragment = new CartFragment();
    private Fragment ordersFragment = new OrdersFragment();
    private Fragment profileFragment = new ProfileFragment();
    private Fragment activeFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars());
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.container, homeFragment, "HomeFragment")
                .commit();
        fragmentManager.beginTransaction()
                .add(R.id.container, cartFragment, "CartFragment").hide(cartFragment)
                .commit();
        fragmentManager.beginTransaction()
                .add(R.id.container, ordersFragment, "OrdersFragment").hide(ordersFragment)
                .commit();
        fragmentManager.beginTransaction()
                .add(R.id.container, profileFragment, "ProfileFragment").hide(profileFragment)
                .commit();

        activeFragment = homeFragment;

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = homeFragment;
            } else if (item.getItemId() == R.id.cart) {
                selectedFragment = cartFragment;
            }else if (item.getItemId() == R.id.orders) {
                selectedFragment = ordersFragment;
            }else if (item.getItemId() == R.id.profile) {
                selectedFragment = profileFragment;
            }

            if (selectedFragment != null && activeFragment != selectedFragment) {
                fragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(selectedFragment)
                        .commit();
                activeFragment = selectedFragment;
            }
            return true;
        });
    }
}

