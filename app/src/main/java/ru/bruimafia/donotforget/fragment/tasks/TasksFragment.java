package ru.bruimafia.donotforget.fragment.tasks;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.BuildConfig;
import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.databinding.FragmentTasksBinding;
import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.util.Constants;
import ru.bruimafia.donotforget.util.NoteAdapter;
import ru.bruimafia.donotforget.util.SharedPreferencesManager;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class TasksFragment extends Fragment implements OnClickOptionsMenu, BillingProcessor.IBillingHandler {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private FragmentTasksBinding binding;
    private TasksViewModel viewModel;
    private NavController navController;
    private NoteAdapter adapter;
    private BillingProcessor bp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tasks, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(this).get(TasksViewModel.class);
        binding.setView(this);
        binding.setViewModel(viewModel);

        navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        bp = BillingProcessor.newBillingProcessor(binding.getRoot().getContext(), getString(R.string.billing_license_key), this);
        bp.initialize();

        if (bp.isPurchased(getString(R.string.billing_product_id)))
            SharedPreferencesManager.getInstance(binding.getRoot().getContext()).setFullVersion(true);
        viewModel.isFullVersion.set(SharedPreferencesManager.getInstance(binding.getRoot().getContext()).isFullVersion());

        if (getActivity().getIntent() != null) {
            if (getActivity().getIntent().getLongExtra(Constants.NOTE_ID, -1) != -1) {
                long id = getActivity().getIntent().getLongExtra(Constants.NOTE_ID, -1);
                TasksFragmentDirections.ActionTasksFragmentToEditFragment action = TasksFragmentDirections.actionTasksFragmentToEditFragment();
                action.setNoteID(id);
                navController.navigate(action);
                getActivity().getIntent().removeExtra(Constants.NOTE_ID);
            }
        }

        adapter = new NoteAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                TasksFragmentDirections.ActionTasksFragmentToEditFragment action = TasksFragmentDirections.actionTasksFragmentToEditFragment();
                action.setNoteID(note.getId());
                navController.navigate(action);
            }

            @Override
            public boolean onItemLongClick(Note note) {
                showDeleteDialog(note.getId());
                return true;
            }
        });
        binding.recycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.recycler.setItemAnimator(new DefaultItemAnimator());
        binding.recycler.setAdapter(adapter);

        viewModel.isOrderById.set(SharedPreferencesManager.getInstance(binding.getRoot().getContext()).isOrderById());

        disposable.add(viewModel.getNotes()
                .subscribeOn(Schedulers.io())
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscription -> viewModel.isLoading.set(true))
                .subscribe(notes -> {
                    adapter.setData(notes);
                    viewModel.notesForScreen.set(notes);
                    viewModel.isLoading.set(false);
                    showPlayRatingDialog(notes.size());
                }));

        showFirstLaunch();
        initAndShowAdsBanner();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.onSort) {
            onChangeSort();
            return true;
        }

        if (item.getItemId() == R.id.onChangeTheme) {
            onChangeTheme();
            return true;
        }

        if (item.getItemId() == R.id.onBuy) {
            bp.purchase(getActivity(), getString(R.string.billing_product_id));
            return true;
        }

        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);

        menu.findItem(R.id.onBuy).setVisible(!bp.isPurchased(getString(R.string.billing_product_id)));

        if (AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_YES)
            menu.findItem(R.id.onChangeTheme).setTitle(R.string.light_theme);
        else
            menu.findItem(R.id.onChangeTheme).setTitle(R.string.dark_theme);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onChangeSort() {
        boolean orderById = SharedPreferencesManager.getInstance(binding.getRoot().getContext()).isOrderById();
        SharedPreferencesManager.getInstance(binding.getRoot().getContext()).setOrderById(!orderById);
        viewModel.isOrderById.set(!orderById);
        disposable.add(viewModel.getNotes()
                .subscribeOn(Schedulers.io())
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscription -> viewModel.isLoading.set(true))
                .subscribe(notes -> {
                    adapter.setData(notes);
                    viewModel.notesForScreen.set(notes);
                    viewModel.isLoading.set(false);
                }));
        notificationAboutOrder(!orderById);
    }

    private void notificationAboutOrder(boolean orderById) {
        if (orderById)
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_order_by_id), BaseTransientBottomBar.LENGTH_LONG).show();
        else
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_order_by_relevance), BaseTransientBottomBar.LENGTH_LONG).show();
    }

    @Override
    public void onChangeTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            SharedPreferencesManager.getInstance(binding.getRoot().getContext())
                    .setLightTheme(!SharedPreferencesManager.getInstance(binding.getRoot().getContext()).isLightTheme());
            App.getInstance().checkCurrentTheme();
        } else
            Snackbar.make(binding.getRoot(), getString(R.string.snackbar_no_change_theme), BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateNote() {
        navController.navigate(R.id.action_tasksFragment_to_editFragment);
    }

    private void showDeleteDialog(long id) {
        Dialog dialog = new Dialog(binding.getRoot().getContext());
        dialog.setContentView(R.layout.dialog_alert);
        ((TextView) dialog.findViewById(R.id.text)).setText(R.string.question_delete);
        ((TextView) dialog.findViewById(R.id.btn_ok)).setText(R.string.delete);
        dialog.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            viewModel.delete(id);
            dialog.cancel();
        });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.cancel());
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFirstLaunch() {
        if (SharedPreferencesManager.getInstance(App.getInstance()).isFirstLaunch()) {
            Dialog dialog = new Dialog(binding.getRoot().getContext());
            dialog.setContentView(R.layout.dialog_first_launch);
            dialog.findViewById(R.id.btn_ok).setOnClickListener(v -> {
                SharedPreferencesManager.getInstance(App.getInstance()).setFirstLaunch(false);
                dialog.cancel();
            });
            dialog.show();
        }
    }

    private void showPlayRatingDialog(int countNotes) {
        if (!SharedPreferencesManager.getInstance(App.getInstance()).isPlayRating() && countNotes != 0 && countNotes % 2 == 0) {
            Dialog dialog = new Dialog(binding.getRoot().getContext());
            dialog.setContentView(R.layout.dialog_rating);
            dialog.findViewById(R.id.btn_ok).setOnClickListener(v -> {
                // обновление информации об успешном оценивании и открытие приложения в Google Play
                SharedPreferencesManager.getInstance(App.getInstance()).setPlayRating(true);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                }
                dialog.cancel();
            });
            dialog.findViewById(R.id.btn_later).setOnClickListener(v -> dialog.cancel());
            dialog.show();
        }
    }

    // показ баннерной рекламы
    void initAndShowAdsBanner() {
        AdRequest request = new AdRequest.Builder().build();
        binding.adView.loadAd(request);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Snackbar.make(binding.getRoot(), getString(R.string.snackbar_reset_app), BaseTransientBottomBar.LENGTH_LONG).show();
        SharedPreferencesManager.getInstance(App.getInstance()).setFullVersion(true);
        viewModel.isFullVersion.set(true);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Snackbar.make(binding.getRoot(), error.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show();
    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    public void onStop() {
        disposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        disposable.dispose();
        super.onDestroy();
    }
}