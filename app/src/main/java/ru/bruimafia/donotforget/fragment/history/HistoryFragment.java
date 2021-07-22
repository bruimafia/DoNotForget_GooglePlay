package ru.bruimafia.donotforget.fragment.history;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.databinding.FragmentHistoryBinding;
import ru.bruimafia.donotforget.util.NoteAdapter;
import ru.bruimafia.donotforget.fragment.tasks.OnItemClickListener;
import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.util.SharedPreferencesManager;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private NavController navController;

    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        binding.setViewModel(viewModel);
        viewModel.isFullVersion.set(SharedPreferencesManager.getInstance(binding.getRoot().getContext()).isFullVersion());

        navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        NoteAdapter adapter = new NoteAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                HistoryFragmentDirections.ActionHistoryFragmentToEditFragment action = HistoryFragmentDirections.actionHistoryFragmentToEditFragment();
                action.setNoteID(note.getId());
                navController.navigate(action);
            }

            @Override
            public boolean onItemLongClick(Note note) {
                showRecoverDialog(note.getId());
                return true;
            }
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        binding.recycler.setItemAnimator(new DefaultItemAnimator());
        binding.recycler.setAdapter(adapter);

        disposable.add(viewModel.getNotesInHistory()
                .subscribeOn(Schedulers.io())
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscription -> viewModel.isLoading.set(true))
                .subscribe(notes -> {
                    adapter.setData(notes);
                    viewModel.notes.set(notes);
                    viewModel.isLoading.set(false);
                }));

        initAndShowAdsBanner();
    }

    private void showRecoverDialog(long id) {
        Dialog dialog = new Dialog(binding.getRoot().getContext());
        dialog.setContentView(R.layout.dialog_alert);
        ((TextView) dialog.findViewById(R.id.text)).setText(R.string.question_restore);
        ((TextView) dialog.findViewById(R.id.btn_ok)).setText(R.string.restore);
        dialog.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            viewModel.recover(id);
            dialog.cancel();
        });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.cancel());
        dialog.show();
    }

    // показ баннерной рекламы
    void initAndShowAdsBanner() {
        AdRequest request = new AdRequest.Builder().build();
        binding.adView.loadAd(request);
    }

    @Override
    public void onStop() {
        disposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }
}