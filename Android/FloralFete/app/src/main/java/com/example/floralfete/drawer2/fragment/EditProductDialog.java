package com.example.floralfete.drawer2.fragment;

import static com.example.floralfete.customcomponents.CustomToast.errorCustomToast;
import static com.example.floralfete.customcomponents.CustomToast.successCustomToast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.example.floralfete.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.floralfete.drawer2.adapter.FlowerTypeSpinnerAdapter;
import com.example.floralfete.model.ProductModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProductDialog extends DialogFragment {

    private FirebaseFirestore db;
    private ProductModel product;
    private Spinner flowerTypeSpinner;
    private ChipGroup occasionChipGroup;
    private Button updateButton, changeImageButton1, changeImageButton2, changeImageButton3;
    private ImageView productImageView1, productImageView2, productImageView3;

    private Uri[] newProductImageUris = new Uri[3];

    private List<String> selectedOccasions = new ArrayList<>();
    private List<String> flowerTypes = new ArrayList<>();
    private List<String> flowerTypeIds = new ArrayList<>();

    private EditText productName, productDescription, productPrice, productQty;

    public static EditProductDialog newInstance(ProductModel product) {
        EditProductDialog fragment = new EditProductDialog();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (ProductModel) getArguments().getSerializable("product");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_product, null);
        builder.setView(view);


        flowerTypeSpinner = view.findViewById(R.id.spinnerFlowerType);
        occasionChipGroup = view.findViewById(R.id.chipGroupOccasions);
        updateButton = view.findViewById(R.id.updateButton);
        changeImageButton1 = view.findViewById(R.id.changeImageButton1);
        changeImageButton2 = view.findViewById(R.id.changeImageButton2);
        changeImageButton3 = view.findViewById(R.id.changeImageButton3);
        productImageView1 = view.findViewById(R.id.productImageView1);
        productImageView2 = view.findViewById(R.id.productImageView2);
        productImageView3 = view.findViewById(R.id.productImageView3);
        productName = view.findViewById(R.id.productNameEditText);
        productDescription = view.findViewById(R.id.productDescriptionEditText);
        productPrice = view.findViewById(R.id.productPriceEditText);
        productQty = view.findViewById(R.id.productQtyEditText);


        loadFlowerTypes();
        loadOccasions();
        loadExistingData();


        changeImageButton1.setOnClickListener(v -> pickImage(0));
        changeImageButton2.setOnClickListener(v -> pickImage(1));
        changeImageButton3.setOnClickListener(v -> pickImage(2));



        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProduct();
            }
        });

        return builder.create();
    }

    private void loadFlowerTypes() {
        db.collection("flowerTypes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                flowerTypes.clear();
                flowerTypeIds.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    flowerTypes.add(document.getString("name"));
                    flowerTypeIds.add(document.getId());
                }

                FlowerTypeSpinnerAdapter adapter = new FlowerTypeSpinnerAdapter(requireContext(), flowerTypes);
                flowerTypeSpinner.setAdapter(adapter);


                int index = flowerTypeIds.indexOf(product.getFlowerTypeId());
                if (index != -1) {
                    flowerTypeSpinner.setSelection(index);
                }
            }
        });
    }

    private void loadOccasions() {
        db.collection("occasions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                occasionChipGroup.removeAllViews();
                selectedOccasions.clear();
                selectedOccasions.addAll(product.getOccasionIds());

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String occasionId = document.getId();
                    String occasionName = document.getString("name");

                    Chip chip = new Chip(requireContext());
                    chip.setText(occasionName);
                    chip.setCheckable(true);
                    chip.setChecked(selectedOccasions.contains(occasionId));

                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            selectedOccasions.add(occasionId);
                        } else {
                            selectedOccasions.remove(occasionId);
                        }
                    });

                    occasionChipGroup.addView(chip);
                }
            }
        });
    }

    private void loadExistingData() {
        productName.setText(product.getName());
        productDescription.setText(product.getDescription());
        productPrice.setText(String.valueOf(product.getPrice()));
        productQty.setText(String.valueOf(product.getQty()));

        List<String> imageUrls = product.getImageUrls();
        if (imageUrls.size() > 0) Glide.with(requireContext()).load(imageUrls.get(0)).into(productImageView1);
        if (imageUrls.size() > 1) Glide.with(requireContext()).load(imageUrls.get(1)).into(productImageView2);
        if (imageUrls.size() > 2) Glide.with(requireContext()).load(imageUrls.get(2)).into(productImageView3);
    }

    private void pickImage(int index) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100 + index);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            int index = requestCode - 100;
            if (index >= 0 && index < 3) {
                newProductImageUris[index] = data.getData();
                if (index == 0) productImageView1.setImageURI(newProductImageUris[index]);
                if (index == 1) productImageView2.setImageURI(newProductImageUris[index]);
                if (index == 2) productImageView3.setImageURI(newProductImageUris[index]);
            }
        }
    }

    private void updateProduct() {

        String productId = product.getId();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("products/" + productId + "/");

        // Update product details
        String updatedName = productName.getText().toString().trim();
        String updatedDescription = productDescription.getText().toString().trim();
        double updatedPrice = Double.parseDouble(productPrice.getText().toString().trim());
        int updatedQty = Integer.parseInt(productQty.getText().toString().trim());

        int selectedFlowerTypeIndex = flowerTypeSpinner.getSelectedItemPosition();
        String updatedFlowerTypeId = flowerTypeIds.get(selectedFlowerTypeIndex);

        // Collect selected occasions
        List<String> updatedOccasionIds = new ArrayList<>();
        for (int i = 0; i < occasionChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) occasionChipGroup.getChildAt(i);
            if (chip.isChecked() && chip.getText() != null) {
                updatedOccasionIds.add(chip.getText().toString()); // Retrieve occasionId from tag
            }
        }


        Map<String, Object> productUpdates = new HashMap<>();
        productUpdates.put("name", updatedName);
        productUpdates.put("description", updatedDescription);
        productUpdates.put("price", updatedPrice);
        productUpdates.put("qty", updatedQty);
        productUpdates.put("flowerTypeId", updatedFlowerTypeId);
        productUpdates.put("occasionIds", updatedOccasionIds);

        // Check if any new images are selected
        boolean imagesUpdated = false;
        for (Uri uri : newProductImageUris) {
            if (uri != null) {
                imagesUpdated = true;
                break;
            }
        }

        if (imagesUpdated) {
            uploadNewImagesAndUpdateProduct(productId, storageReference, productUpdates);
        } else {

            db.collection("products").document(productId)
                    .update(productUpdates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("FloralFeteLog", "Product details updated successfully");
                            successCustomToast("Product details updated successfully",getContext());

                            dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FloralFeteLog", "Failed to update product details", e);
                            errorCustomToast("Failed to update product details",getContext());
                        }
                    });
        }
    }


    private void uploadNewImagesAndUpdateProduct(String productId, StorageReference storageReference, Map<String, Object> productUpdates) {
        List<String> updatedImageUrls = new ArrayList<>(product.getImageUrls());
        final int[] uploadedImagesCount = {0};
        final int totalImagesToUpload = countSelectedImages();

        for (int i = 0; i < 3; i++) {
            if (newProductImageUris[i] != null) {
                final int imageIndex = i;
                StorageReference imgRef = storageReference.child("image" + (i + 1) + ".jpg");

                imgRef.putFile(newProductImageUris[i]).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                updatedImageUrls.set(imageIndex, uri.toString());
                                uploadedImagesCount[0]++;

                                // If all selected images are uploaded, update Firestore
                                if (uploadedImagesCount[0] == totalImagesToUpload) {
                                    productUpdates.put("imageUrls", updatedImageUrls);
                                    db.collection("products").document(productId)
                                            .update(productUpdates)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("FloralFeteLog", "Product updated successfully with new images");
                                                    successCustomToast("Product updated successfully with new images",getContext());
                                                    dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("FloralFeteLog", "Failed to update product details with images", e);
                                                    errorCustomToast("Failed to update product details with images",getContext());

                                                }
                                            });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("FloralFeteLog", "Failed to get download URL for index " + imageIndex, e);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FloralFeteLog", "Image upload failed for index " + imageIndex, e);
                    }
                });
            }
        }
    }



    private int countSelectedImages() {
        int count = 0;
        for (Uri uri : newProductImageUris) {
            if (uri != null) {
                count++;
            }
        }
        return count;
    }

}
