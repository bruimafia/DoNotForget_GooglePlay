package ru.bruimafia.donotforget.dialog.sync;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.databinding.DialogSyncBinding;
import ru.bruimafia.donotforget.repository.Repository;
import ru.bruimafia.donotforget.util.Constants;
import ru.bruimafia.donotforget.util.SharedPreferencesManager;

public class SyncDialog extends DialogFragment implements OnClickMethod {

    private DialogSyncBinding binding;
    private Repository repository;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    public ObservableField<Boolean> isLogin = new ObservableField<>(false);
    public ObservableField<Long> lastSync = new ObservableField<>(0L);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_sync, container, false);
        binding.setView(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.gso_request_id_token))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(App.getInstance(), gso);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null)
            isLogin.set(true);

        lastSync.set(SharedPreferencesManager.getInstance(App.getInstance()).getLastSync());
        repository = new Repository();

        return binding.getRoot();
    }

    @Override
    public void onSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
    }

    @Override
    public void onSync() {
        binding.progress.setProgress(1);
        Completable.fromAction(() -> repository.syncing())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(1000, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> {
                    binding.progress.setProgress(0);
                    long time = System.currentTimeMillis();
                    SharedPreferencesManager.getInstance(App.getInstance()).setLastSync(time);
                    lastSync.set(time);
                })
                .subscribe();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
                isLogin.set(true);
                repository = new Repository();
            } catch (ApiException e) {
                Log.d(Constants.TAG, "Google sign in failed ", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(binding.getView().getActivity(), task -> {
                    if (task.isSuccessful())
                        Log.d(Constants.TAG, "signInWithCredential: success");
                    else
                        Log.d(Constants.TAG, "signInWithCredential: failure", task.getException());
                });
    }

    @Override
    public void onDestroy() {
        repository.onDestroy();
        super.onDestroy();
    }
}