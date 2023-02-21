package com.paulbrian.elitecontentmanagement.user;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.paulbrian.elitecontentmanagement.MainActivity;
import com.paulbrian.elitecontentmanagement.R;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer= findViewById(R.id.drawer_layout);
        NavigationView navigationView= findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView user= headerView.findViewById(R.id.nav_header_name);
//        TextView phone= headerView.findViewById(R.id.nav_header_phone);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(getBaseContext(), MainActivity.class));
            finish();

        }

        navigationView.setNavigationItemSelectedListener(Item -> {
            switch (Item.getItemId()) {
                case R.id.menuProfile:
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new ProfileFragment()).commit();

                    break;
                case R.id.menuAddContent:
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new AddContentFragment()).commit();

                    break;
                case R.id.menuMyContents:
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new MyContentsFragment()).commit();

                    break;

                case R.id.menuCategories:
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new CategoriesFragment()).commit();

                    break;

                case R.id.menuSettings:
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new SettingsFragment()).commit();

                    break;

                case R.id.menuSupport:
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new ContactSupportFragment()).commit();

                    break;

                case R.id.customer_nav_share:
                    shareApp();
                    Toast.makeText(getBaseContext(), "Share this app", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menuLogout:
                    firebaseAuth.signOut();
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                    Toast.makeText(getBaseContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    finish();


            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState== null){
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                    new CategoriesFragment()).commit();
            navigationView.setCheckedItem(R.id.menuCategories);
        }

    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            String shareMessage = "\nElite Content Management App" +
                    "Download and try it from this link";
            shareMessage = shareMessage + "~PaulBrian";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }
    }

}