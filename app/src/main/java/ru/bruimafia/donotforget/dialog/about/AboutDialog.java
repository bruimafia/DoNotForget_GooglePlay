package ru.bruimafia.donotforget.dialog.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.bruimafia.donotforget.BuildConfig;
import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.databinding.DialogAboutBinding;
import ru.bruimafia.donotforget.util.Constants;
import ru.bruimafia.donotforget.util.SharedPreferencesManager;

public class AboutDialog extends DialogFragment implements OnClickMethod {

    private DialogAboutBinding binding;
    public ObservableField<Boolean> isFullVersion = new ObservableField<>(false);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_about, container, false);
        binding.setView(this);

        isFullVersion.set(SharedPreferencesManager.getInstance(binding.getRoot().getContext()).isFullVersion());

        return binding.getRoot();
    }

    @Override
    public void onVkLink() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("linkedin://profile/%s", Constants.VK_ID))));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/id" + Constants.VK_ID)));
        }
    }

    @Override
    public void onGoogleplayLink() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
        }
    }
}