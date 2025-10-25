package com.example.floralfete.drawer2.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.floralfete.R;
import com.example.floralfete.drawer2.adapter.ManageUsersAdapter;
import com.example.floralfete.model.UserModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class ManageUsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private ManageUsersAdapter adapter;
    private List<UserModel> userList;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);

        recyclerView = view.findViewById(R.id.manage_users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        adapter = new ManageUsersAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        loadUsers();

        return view;
    }

    private void loadUsers() {
        firestore.collection("user").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    UserModel user = doc.toObject(UserModel.class);
                    user.setUserId(doc.getId());
                    userList.add(user);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
