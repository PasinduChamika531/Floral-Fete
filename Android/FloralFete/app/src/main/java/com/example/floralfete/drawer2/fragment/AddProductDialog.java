package com.example.floralfete.drawer2.fragment;

import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import com.example.floralfete.model.ProductModel;
import com.example.floralfete.drawer2.adapter.FlowerTypeSpinnerAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddProductDialog extends DialogFragment {

    private static final int PICK_IMAGES_REQUEST = 1;

    private EditText etProductName, etProductDescription, etProductPrice, etProductQty;
    private Spinner spinnerFlowerType;
    private ChipGroup chipGroupOccasions;
    private LinearLayout imagePreviewLayout;
    private Button btnSaveProduct, btnPickImages;
    private FirebaseFirestore db;
    private List<String> occasionList = new ArrayList<>();
    private List<String> flowerTypeList = new ArrayList<>();
    private List<Uri> imageUriList = new ArrayList<>();

    private ProductModel product;
    private Context context;

    public AddProductDialog() {

    }

    public static AddProductDialog newInstance() {
        return new AddProductDialog();
    }

    public AddProductDialog(Context context, FirebaseFirestore db) {
        this.context = context;
        this.db = db;
    }

    public AddProductDialog(Context context, FirebaseFirestore db, ProductModel product) {
        this.context = context;
        this.db = db;
        this.product = product;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_product, null);
        builder.setView(view);


        etProductName = view.findViewById(R.id.etProductName);
        etProductDescription = view.findViewById(R.id.etProductDescription);
        etProductPrice = view.findViewById(R.id.etProductPrice);
        etProductQty = view.findViewById(R.id.etProductQty);
        spinnerFlowerType = view.findViewById(R.id.spinnerFlowerType);
        chipGroupOccasions = view.findViewById(R.id.chipGroupOccasions);
        imagePreviewLayout = view.findViewById(R.id.imagePreviewLayout);
        btnSaveProduct = view.findViewById(R.id.btnSaveProduct);
        btnPickImages = view.findViewById(R.id.btnPickImages); // Pick Images Button

        db = FirebaseFirestore.getInstance();


        loadFlowerTypes();
        loadOccasions();


        btnSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProductToFirestore();
            }
        });

        btnPickImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImages();
            }
        });

        return builder.create();
    }


    private void loadFlowerTypes() {
        db.collection("flowerTypes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    flowerTypeList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        flowerTypeList.add(document.getString("name"));
                    }

                    FlowerTypeSpinnerAdapter adapter = new FlowerTypeSpinnerAdapter(getContext(), flowerTypeList);
                    spinnerFlowerType.setAdapter(adapter);
                }
            }
        });
    }

    private void loadOccasions() {
        db.collection("occasions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    occasionList.clear();
                    chipGroupOccasions.removeAllViews();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String occasionName = document.getString("name");
                        occasionList.add(occasionName);


                        Chip chip = new Chip(getContext());
                        chip.setText(occasionName);
                        chip.setCheckable(true);
                        chipGroupOccasions.addView(chip);
                    }
                }
            }
        });
    }

    private void pickImages() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {

            imageUriList.clear();
            if (data.getClipData() != null) {

                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUriList.add(imageUri);
                }
            } else if (data.getData() != null) {

                imageUriList.add(data.getData());
            }

            displayImagePreviews();
        }
    }

    private void displayImagePreviews() {
        imagePreviewLayout.removeAllViews();
        for (Uri uri : imageUriList) {
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            Glide.with(getContext()).load(uri).into(imageView);
            imagePreviewLayout.addView(imageView);
        }
    }

    private void saveProductToFirestore() {
        String name = etProductName.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        double price = Double.parseDouble(etProductPrice.getText().toString().trim());
        int qty = Integer.parseInt(etProductQty.getText().toString().trim());
        String selectedFlowerType = spinnerFlowerType.getSelectedItem().toString();


        List<String> selectedOccasions = new ArrayList<>();
        for (int i = 0; i < chipGroupOccasions.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupOccasions.getChildAt(i);
            if (chip.isChecked()) {
                selectedOccasions.add(chip.getText().toString());
            }
        }

        // Create a new product document
        CollectionReference productRef = db.collection("products");
        String productId = productRef.document().getId();

        ProductModel product = new ProductModel(productId,name, description, price, qty, selectedFlowerType, selectedOccasions, new ArrayList<>());

        productRef.document(productId).set(product).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    uploadImagesToFirebase(productId);
                }
            }
        });
    }

    private void uploadImagesToFirebase(String productId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("products/" + productId + "/");

        List<String> uploadedImageUrls = new ArrayList<>();
        for (Uri imageUri : imageUriList) {
            String fileName = UUID.randomUUID().toString();
            StorageReference imageRef = storageRef.child(fileName + ".jpg");

            imageRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        uploadedImageUrls.add(task.getResult().toString());

                        if (uploadedImageUrls.size() == imageUriList.size()) {
                            updateProductImageUrls(productId, uploadedImageUrls);
                        }
                    }
                }
            });
        }
    }

    private void updateProductImageUrls(String productId, List<String> imageUrls) {
        DocumentReference productRef = db.collection("products").document(productId);
        productRef.update("imageUrls", imageUrls).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("Firebase", "Product images updated successfully");
                    successCustomToast("Product Added Successfully",getContext());
                    dismiss();
                }
            }
        });
    }


}
