package com.paulbrian.elitecontentmanagement.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.paulbrian.elitecontentmanagement.MainActivity;
import com.paulbrian.elitecontentmanagement.R;
import com.paulbrian.elitecontentmanagement.admin.fradments.ApproveContentsFragment;
import com.paulbrian.elitecontentmanagement.admin.fradments.ApproveCreatorsFragment;
import com.paulbrian.elitecontentmanagement.admin.fradments.ViewContentCreatorsFragment;
import com.paulbrian.elitecontentmanagement.admin.fradments.ViewUsersFragment;

public class AdminHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewContentCreatorsFragment()).commit();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menuContentCreators) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewContentCreatorsFragment()).commit();
        }else if (id == R.id.menuUsers){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewUsersFragment()).commit();

        }else if (id == R.id.menuApproveContentCreators){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ApproveCreatorsFragment()).commit();

        }else if (id == R.id.menuApproveContents){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ApproveContentsFragment()).commit();

        }else if (id == R.id.customer_nav_share){
            Intent share=new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            String body="Download  app!";
            String sub="Elite App.";
            share.putExtra(Intent.EXTRA_TEXT,body);
            share.putExtra(Intent.EXTRA_SUBJECT,sub);
            share.putExtra(Intent.EXTRA_TEXT,"");
            startActivity(Intent.createChooser(share,"Share Using"));

        }else if (id == R.id.menuLogout){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signOut();

            Intent login=new Intent(getBaseContext(), MainActivity.class);
            startActivity(login);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}