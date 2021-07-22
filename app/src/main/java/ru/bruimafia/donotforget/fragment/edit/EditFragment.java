package ru.bruimafia.donotforget.fragment.edit;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.databinding.FragmentEditBinding;
import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.util.Constants;
import ru.bruimafia.donotforget.util.SharedPreferencesManager;

public class EditFragment extends Fragment implements OnClickMethod {

    private FragmentEditBinding binding;
    private EditViewModel viewModel;
    private NavController navController;
    private InterstitialAd interstitialAd; // межстраничная реклама
    private long id;
    private int[] colorPresets; // список цветовых пресетов по умолчанию

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EditFragmentArgs args = EditFragmentArgs.fromBundle(getArguments());
        id = args.getNoteID();
        colorPresets = new int[]{
                getResources().getColor(R.color.grey_50),
                getResources().getColor(R.color.orange_100),
                getResources().getColor(R.color.red_100),
                getResources().getColor(R.color.pink_100),
                getResources().getColor(R.color.purple_100),
                getResources().getColor(R.color.deepPurple_100),
                getResources().getColor(R.color.indigo_100),
                getResources().getColor(R.color.blue_100),
                getResources().getColor(R.color.lightBlue_100),
                getResources().getColor(R.color.cyan_100),
                getResources().getColor(R.color.teal_100),
                getResources().getColor(R.color.green_100),
                getResources().getColor(R.color.lightGreen_100),
                getResources().getColor(R.color.lime_100),
                getResources().getColor(R.color.yellow_100),
                getResources().getColor(R.color.amber_100),
                getResources().getColor(R.color.deepOrange_100),
                getResources().getColor(R.color.brown_100),
                getResources().getColor(R.color.grey_100),
                getResources().getColor(R.color.blueGrey_100)
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditViewModel.class);

        navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        binding.setView(this);
        binding.setViewModel(viewModel);
        viewModel.setNote(id);
        viewModel.isFullVersion.set(SharedPreferencesManager.getInstance(binding.getRoot().getContext()).isFullVersion());

        initAndShowAdsBanner(); // инициализация и показ баннерной рекламы
        initAdsInterstitial(); // инициализация межстраничной рекламы в приложении
    }

    @Override
    public void onCreate(Note note) {
        viewModel.add(note);
        showAdsInterstitial();
        navController.popBackStack();
    }

    @Override
    public void onUpdate(Note note) {
        viewModel.update(note);
        showAdsInterstitial();
        navController.popBackStack();
    }

    @Override
    public void onDelete(long id) {
        viewModel.delete(id);
        navController.popBackStack();
    }

    @Override
    public void onRecover(long id) {
        viewModel.recover(id);
        navController.popBackStack();
    }

    @Override
    public void onChooseColor() {
        new ColorPickerDialog()
                .withColor(viewModel.note.get().getColor())
                .withRetainInstance(false)
                .withTitle(getString(R.string.color_choose))
                .withCornerRadius(5)
                .withAlphaEnabled(true)
                .withPresets(colorPresets)
                .withListener((dialog, color) -> viewModel.note.get().setColor(color))
                .show(getChildFragmentManager(), "ColorPickerDialog");
    }

    @Override
    public void onChooseDate() {
        new TimePickerDialog.Builder()
                .setType(Type.YEAR_MONTH_DAY)
                .setCallBack((timePickerView, milliseconds) -> viewModel.setDate(milliseconds))
                .setCancelStringId(getString(R.string.cancel))
                .setSureStringId(getString(R.string.choose))
                .setTitleStringId(getString(R.string.date_choose))
                .setYearText(getString(R.string.year))
                .setMonthText(getString(R.string.month))
                .setDayText(getString(R.string.day))
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setCurrentMillseconds(viewModel.note.get().getDate())
                .setThemeColor(getResources().getColor(R.color.colorSecondary))
                .setWheelItemTextNormalColor(getResources().getColor(R.color.colorOnPrimary))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.colorSecondary))
                .setWheelItemTextSize(14)
                .build()
                .show(getChildFragmentManager(), "TimePickerDialog_YEAR_MONTH_DAY");
    }

    @Override
    public void onChooseTime() {
        new TimePickerDialog.Builder()
                .setType(Type.HOURS_MINS)
                .setCallBack((timePickerView, milliseconds) -> viewModel.setTime(milliseconds))
                .setCancelStringId(getString(R.string.cancel))
                .setSureStringId(getString(R.string.choose))
                .setTitleStringId(getString(R.string.time_choose))
                .setHourText(getString(R.string.hour))
                .setMinuteText(getString(R.string.minute))
                .setCyclic(false)
                .setCurrentMillseconds(viewModel.note.get().getDate())
                .setThemeColor(getResources().getColor(R.color.colorSecondary))
                .setWheelItemTextNormalColor(getResources().getColor(R.color.colorOnPrimary))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.colorSecondary))
                .setWheelItemTextSize(14)
                .build()
                .show(getChildFragmentManager(), "TimePickerDialog_HOURS_MINS");
    }

    // показ баннерной рекламы
    void initAndShowAdsBanner() {
        AdRequest request = new AdRequest.Builder().build();
        binding.adView.loadAd(request);
    }

    // показ межстраничной рекламы в приложении
    public void showAdsInterstitial() {
        if (interstitialAd != null && !viewModel.isFullVersion.get())
            interstitialAd.show(requireActivity());
        else
            Log.d(Constants.TAG, "The interstitial ad wasn't ready yet.");
    }

    // инициализация межстраничной рекламы в приложении
    private void initAdsInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(requireContext(), getString(R.string.interstitial_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                // ссылка mInterstitialAd будет равна нулю до тех пор, пока не будет загружено объявление
                Log.d(Constants.TAG, "onAdLoaded");
                interstitialAd = ad;
                ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // вызывается при отклонении полноэкранного содержимого. Установить значение null, чтобы не показывать ее во второй раз
                        Log.d(Constants.TAG, "The ad was dismissed.");
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // вызывается, когда содержимое полноэкранного режима не отображается. Установить значение null, чтобы не показывать ее во второй раз
                        Log.d(Constants.TAG, "The ad failed to show.");
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(Constants.TAG, "The ad was shown."); // вызывается при отображении полноэкранного содержимого
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d(Constants.TAG, loadAdError.getMessage()); // ошибка инициализации
                interstitialAd = null;
            }
        });
    }

}