package com.albertkingdom.mybusmap.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;


import com.albertkingdom.mybusmap.R;
import com.albertkingdom.mybusmap.databinding.UserFragmentBinding;
import com.bumptech.glide.Glide;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import timber.log.Timber;

public class UserFragment extends Fragment {
    static final String TAG = "UserFragment";
    UserFragmentBinding binding;
    private FirebaseAuth mAuth;
    GetCredentialRequest request;
    SignInButton googleSignInButton;

    private CredentialManager credentialManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = UserFragmentBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        bindingView();
        GetSignInWithGoogleOption signInWithGoogleOption = new GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id)).build();
        request = new GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build();
        credentialManager = CredentialManager.create(requireContext());
        return binding.getRoot();
    }
    public void handleSignIn(GetCredentialResponse result) {
        // Handle the successfully returned credential.
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                // Use googleIdTokenCredential and extract id to validate and authenticate on your server.
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());

            } else {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential");
            }
        } else {
            // Catch any unrecognized credential type here.
            Log.e(TAG, "Unexpected type of credential");
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri userImageUri = user.getPhotoUrl();

            Log.d(TAG, "user name " + name + " email" + email + " image " + userImageUri);
            binding.username.setText(name);
            googleSignInButton.setVisibility(View.INVISIBLE);
            binding.signOutButton.setVisibility(View.VISIBLE);
            Glide.with(this).load(userImageUri).into(binding.userImage);
            binding.tip.setVisibility(View.INVISIBLE);
        } else {
            binding.signOutButton.setVisibility(View.INVISIBLE);
            googleSignInButton.setVisibility(View.VISIBLE);
            binding.userImage.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_account_circle_24));
            binding.tip.setVisibility(View.VISIBLE);
        }
    }

    private void bindingView() {
        googleSignInButton = binding.googleSignInButton;
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click google sign in");
                signIn();
            }
        });
        binding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                binding.username.setText("");
            }
        });
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        Timber.tag(TAG).d("firebaseAuthWithGoogle token " + idToken);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Timber.tag(TAG).d("signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Timber.tag(TAG).w(task.getException(), "signInWithCredential:failure");
                            updateUI(null);
                        }
                    }
                });
    }

    private void signIn() {
        credentialManager.getCredentialAsync(
                requireContext(),
                request,
                null,
                ContextCompat.getMainExecutor(requireContext()),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {

                        Log.d(TAG, "從google取得credential");
                        handleSignIn(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
}
