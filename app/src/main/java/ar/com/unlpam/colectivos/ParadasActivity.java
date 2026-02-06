package ar.com.unlpam.colectivos;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ParadasActivity extends BaseActivity {

    private ParadasViewModel viewModel;
    private ViewPager2 viewPager;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paradas);

        viewModel = new ViewModelProvider(this).get(ParadasViewModel.class);

        setupUI();

        // Adapter de ViewPager2: se setea una sola vez
        adapter = new Adapter(this);
        viewPager.setAdapter(adapter);

        observeViewModel();

        viewModel.fetchParadas();
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        viewPager = findViewById(R.id.viewpager);
    }

    private void observeViewModel() {
        viewModel.paradas.observe(this, paradas -> {
            if (paradas != null) {
                showParadas(paradas);
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) showLoading();
            else hideLoading();
        });

        viewModel.error.observe(this, errorState -> {
            if (errorState != null) {
                showErrorDialog(errorState.message);
            }
        });
    }

    private void showParadas(JSONArray paradas) {
        Bundle args = new Bundle();
        args.putString("data", paradas.toString());

        ParadasListFragment paradaListFragment = ParadasListFragment.newInstance(args);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(paradaListFragment);

        List<String> titles = new ArrayList<>();
        titles.add("List");

        adapter.setFragments(fragments, titles);

        // Opcional: asegurarte que quede en la primera página
        viewPager.setCurrentItem(0, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    static class Adapter extends FragmentStateAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public Adapter(@NonNull FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }

        // IDs estables para que ViewPager2 refresque bien cuando cambian fragments
        @Override
        public long getItemId(int position) {
            return fragmentList.get(position).hashCode();
        }

        @Override
        public boolean containsItem(long itemId) {
            for (Fragment f : fragmentList) {
                if (f.hashCode() == itemId) return true;
            }
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setFragments(List<Fragment> fragments, List<String> titles) {
            fragmentList.clear();
            fragmentTitleList.clear();

            if (fragments != null) fragmentList.addAll(fragments);
            if (titles != null) fragmentTitleList.addAll(titles);

            notifyDataSetChanged();
        }

        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}