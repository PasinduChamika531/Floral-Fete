package com.example.floralfete.drawer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.floralfete.MainActivity;
import com.example.floralfete.R;
import com.example.floralfete.UserSigninActivity;
import com.example.floralfete.drawer.UserProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UpdateProfileFragment extends Fragment {

    private EditText edtFname, edtLname, edtMobile, edtOldPassword, edtNewPassword, edtEmail;
    private Button btnUpdateProfile, btnUploadImage;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    private View viewParent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_profile, container, false);
        super.onViewCreated(view, savedInstanceState);

        viewParent = view;

        edtFname = view.findViewById(R.id.user_update_edittext1);
        edtLname = view.findViewById(R.id.user_update_edittext2);
        edtMobile = view.findViewById(R.id.user_update_edittext3);
        edtEmail = view.findViewById(R.id.user_update_edittext4);
        edtOldPassword = view.findViewById(R.id.user_update_editpassword1);
        edtNewPassword = view.findViewById(R.id.user_update_editpassword2);
        btnUpdateProfile = view.findViewById(R.id.user_update_button2);
        btnUploadImage = view.findViewById(R.id.user_update_button1);

        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Activity.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        loadUserData();
        loadUserToolbarData();

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });

        return view;
    }

    private void loadUserData() {
        String userId = sharedPreferences.getString("user_id", "");

        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            edtFname.setText(documentSnapshot.getString("fname"));
                            edtLname.setText(documentSnapshot.getString("lname"));
                            edtMobile.setText(documentSnapshot.getString("mobile"));
                            edtEmail.setText(documentSnapshot.getString("email"));

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                        errorCustomToast("Failed to load user data");
                    }
                });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            confirmChooseImage();
        }
    }

    private void confirmChooseImage() {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_logout_dialog, null);

        //Find Views
        TextView customtextView1 = dialogView.findViewById(R.id.custom_alert_textView1);
        customtextView1.setText("Are you sure want to Upload");
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        btnConfirm.setText("Upload");
        builder.setView(dialogView);

        // Create dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadProfileImage();
                alertDialog.dismiss();

            }
        });
    }


    private void uploadProfileImage() {
        if (selectedImageUri != null) {
            String userId = sharedPreferences.getString("user_id", "");
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userId + ".jpg");

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    saveImageUrlToFirestore(userId, uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageUrlToFirestore(String userId, String imageUrl) {
        db.collection("user").document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sharedPreferences.edit().putString("user_profile_url", imageUrl).apply();
                        loadUserToolbarData();

                        successCustomToast("Profile Image Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        errorCustomToast("Failed to Update Image URL");
                    }
                });
    }

    private void updateUserData() {
        String userId = sharedPreferences.getString("user_id", "");
        String fname = edtFname.getText().toString();
        String lname = edtLname.getText().toString();
        String mobile = edtMobile.getText().toString();
        String oldPassword = edtOldPassword.getText().toString();
        String newPassword = edtNewPassword.getText().toString();

        if (!newPassword.isEmpty()) {
            verifyOldPasswordAndUpdate(userId, oldPassword, newPassword, fname, lname, mobile);
        } else {
            saveUserDataToFirestore(userId, fname, lname, mobile);
        }
    }

    private void verifyOldPasswordAndUpdate(String userId, String oldPassword, String newPassword, String fname, String lname, String mobile) {
        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String savedPassword = documentSnapshot.getString("password");
                            if (savedPassword != null && savedPassword.equals(oldPassword)) {
                                db.collection("user").document(userId)
                                        .update("password", newPassword)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                saveUserDataToFirestore(userId, fname, lname, mobile);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                errorCustomToast("Failed to Update Password");
                                            }
                                        });
                            } else {

                                errorCustomToast("Old Password is Incorrect");
                            }
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String fname, String lname, String mobile) {
        db.collection("user").document(userId)
                .update("fname", fname, "lname", lname, "mobile", mobile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        saveUserDataToSharedPreferences(fname, lname, mobile); // Save locally
                        successCustomToast("Profile Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorCustomToast("Failed to Update Profile");
                    }
                });
    }

    private void saveUserDataToSharedPreferences(String fname, String lname, String mobile) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_fname", fname);
        editor.putString("user_lname", lname);
        editor.putString("user_mobile", mobile);
        editor.apply(); // Save changes
        loadUserToolbarData();
    }


    private void loadUserToolbarData() {


        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userFName = sharedPreferences.getString("user_fname", "Guest");
        String userLName = sharedPreferences.getString("user_lname", "Name");
        String userEmail = sharedPreferences.getString("user_email", "No Email");
        String userProfileImage = sharedPreferences.getString("user_profile_url", "");


        NavigationView navigationView = getActivity().findViewById(R.id.navigationView);
        if (navigationView == null) {
            Log.e("FloralFeteLog", "NavigationView is NULL");
            return;
        }

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            Log.e("FloralFeteLog", "Header View is NULL");
            return;
        }


        TextView userProfileText1 = headerView.findViewById(R.id.user_profile_text1);
        TextView userProfileText2 = headerView.findViewById(R.id.user_profile_text2);
        ImageView userProfileImageView1 = headerView.findViewById(R.id.user_profile_image1);

        // Set profile image
        if (!userProfileImage.equals("")) {
            Glide.with(requireContext()).load(userProfileImage).into(userProfileImageView1);
        } else {
            userProfileImageView1.setImageResource(R.drawable.ic_user_profile);
            userProfileImageView1.invalidate();
        }

        // Set user details
        userProfileText1.setText(userFName + " " + userLName);
        userProfileText2.setText(userEmail);
    }


    private void successCustomToast(String message) {
        Toast t = new Toast(getContext());
        t.setDuration(Toast.LENGTH_LONG);
        t.setGravity(Gravity.BOTTOM, 0, 200);
        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.custom_toast1, null, false);
        TextView textView = view1.findViewById(R.id.customToastTextView1);
        textView.setText(message);
        t.setView(view1);
        t.show();
    }

    private void errorCustomToast(String message) {
        Toast t = new Toast(getContext());
        t.setDuration(Toast.LENGTH_LONG);
        t.setGravity(Gravity.BOTTOM, 0, 200);
        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.custom_toast2, null, false);
        TextView textView = view1.findViewById(R.id.customToastTextView2);
        textView.setText(message);
        t.setView(view1);
        t.show();
    }


}
