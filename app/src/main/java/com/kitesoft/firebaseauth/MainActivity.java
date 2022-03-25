package com.kitesoft.firebaseauth;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.internal.AccountAccessor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.kitesoft.firebaseauth.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth= FirebaseAuth.getInstance();

        binding.btnSignup.setOnClickListener(view -> {
            
            String email= binding.etEmail.getText().toString();
            String pw= binding.etPw.getText().toString();
            
            firebaseAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //비번 최소 6자이상.
                        //이메일형식체크 ㅁㅁ@ㅁㅁ.ㅁㅁ
                        Toast.makeText(MainActivity.this, "메일과 비번이 사용가능합니다.", Toast.LENGTH_SHORT).show();
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Toast.makeText(MainActivity.this, "전송된 메일을 확인하세요.", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(MainActivity.this, "메일 전송 실패", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }else{
                        Toast.makeText(MainActivity.this, "가입 실패", Toast.LENGTH_SHORT).show();
                    }
                    
                }
            });

        });

        binding.btnSignin.setOnClickListener(view -> {
            String email= binding.etEmail.getText().toString();
            String pw= binding.etPw.getText().toString();

            firebaseAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                        if(firebaseAuth.getCurrentUser().isEmailVerified()){
                            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            String email = firebaseAuth.getCurrentUser().getEmail();
                            String name= firebaseAuth.getCurrentUser().getDisplayName();
                            Uri url= firebaseAuth.getCurrentUser().getPhotoUrl();

                            binding.tv.setText(email+"");
                            binding.tv.append("\n"+name);
                            binding.tv.append("\n"+url);

                            Glide.with(MainActivity.this).load(url).into(binding.civ);

                        }else{
                            Toast.makeText(MainActivity.this, "이메일 인증을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(MainActivity.this, "로그인 실패 : " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                    }

                }
            });
        });



        binding.btnProfile.setOnClickListener(view -> {

            UserProfileChangeRequest request= new UserProfileChangeRequest.Builder().setDisplayName("sam").setPhotoUri(Uri.parse("https://t1.daumcdn.net/cfile/blog/254F8D4253AD2BB70A")).build();

            firebaseAuth.getCurrentUser().updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                        Toast.makeText(MainActivity.this, "profile update success", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity.this, "profile update fail", Toast.LENGTH_SHORT).show();
                }
            });
        });


        binding.btnPick.setOnClickListener(view -> {
            Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            resultLauncher.launch(intent);
        });

        binding.btnGoogle.setOnClickListener(view -> {
            GoogleSignInOptions options= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("648557912146-drr9v960rj866uof0tufpocikpamar4v.apps.googleusercontent.com").requestEmail().build();
            Intent intent= GoogleSignIn.getClient(this, options).getSignInIntent();
            gsoResultLauncher.launch(intent);
        });
    }

    ActivityResultLauncher<Intent> gsoResultLauncher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            Intent intent=result.getData();
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account= task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();

                            String email = firebaseAuth.getCurrentUser().getEmail();
                            String name= firebaseAuth.getCurrentUser().getDisplayName();
                            Uri url= firebaseAuth.getCurrentUser().getPhotoUrl();

                            binding.tv.setText(email+"");
                            binding.tv.append("\n"+name);
                            binding.tv.append("\n"+url);

                            Glide.with(MainActivity.this).load(url).into(binding.civ);
                        }else{
                            Toast.makeText(MainActivity.this, "인증 실패", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            } catch (ApiException e) {
                e.printStackTrace();
            }

        }
    });



    Uri profileUri=null;

    ActivityResultLauncher<Intent> resultLauncher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                profileUri= result.getData().getData();
            }
        }
    });
}