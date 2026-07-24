package com.example.autimate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChildSelectionActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private RecyclerView recyclerViewChildren;
    private Button btnAddChild, btnLogin;

    private FirebaseFirestore db;
    private String parentId;
    private String parentName;

    private List<ChildProfile> childList;
    private ChildSelectionAdapter adapter;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_selection);

        db = FirebaseFirestore.getInstance();

        // Get parent info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE);
        parentId = prefs.getString("parentId", "");
        parentName = prefs.getString("parentName", "Parent");

        // Initialize views
        tvGreeting = findViewById(R.id.tvGreeting);
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        btnAddChild = findViewById(R.id.btnAddChild);
        btnLogin = findViewById(R.id.btnLogin);

        tvGreeting.setText("Hi " + parentName + "!");

        // Setup RecyclerView
        childList = new ArrayList<>();
        adapter = new ChildSelectionAdapter(childList);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChildren.setAdapter(adapter);

        // Load children
        loadChildren();

        btnAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(ChildSelectionActivity.this, AddChildProfileActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            if (selectedPosition != -1 && selectedPosition < childList.size()) {
                ChildProfile selectedChild = childList.get(selectedPosition);
                SharedPreferences childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                childPrefs.edit()
                        .putString("childId", selectedChild.id)
                        .putString("childName", selectedChild.name)
                        .putString("childAvatar", selectedChild.avatar != null ? selectedChild.avatar : "👧")
                        .apply();

                Intent intent = new Intent(ChildSelectionActivity.this, ChildHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ChildSelectionActivity.this, "Please select a child", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChildren() {
        if (parentId == null || parentId.isEmpty()) {
            Toast.makeText(this, "Parent ID not found. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear the list
                    childList.clear();

                    // Use a Set to track unique child names to avoid duplicates
                    Set<String> childNames = new HashSet<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String childName = doc.getString("childName");

                        // Skip if child name is null or empty
                        if (childName == null || childName.isEmpty()) continue;

                        // Skip duplicates (if same child name exists, skip the duplicate)
                        if (childNames.contains(childName)) continue;
                        childNames.add(childName);

                        ChildProfile child = new ChildProfile();
                        child.id = doc.getId();
                        child.name = childName;
                        child.age = doc.getString("age");
                        child.gender = doc.getString("gender");
                        child.avatar = doc.getString("avatar");

                        // Set default avatar if null or empty
                        if (child.avatar == null || child.avatar.isEmpty()) {
                            child.avatar = "👧";
                        }

                        childList.add(child);
                    }

                    adapter.notifyDataSetChanged();

                    if (childList.isEmpty()) {
                        Toast.makeText(this, "No children found. Please add a child.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list
        loadChildren();
    }

    // Child Profile Class
    static class ChildProfile {
        String id;
        String name;
        String age;
        String gender;
        String avatar;
    }

    // Child Selection Adapter
    class ChildSelectionAdapter extends RecyclerView.Adapter<ChildSelectionAdapter.ViewHolder> {
        private List<ChildProfile> children;

        public ChildSelectionAdapter(List<ChildProfile> children) {
            this.children = children;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_child_profile, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChildProfile child = children.get(position);

            if (child != null) {
                // Set avatar
                String avatar = child.avatar != null ? child.avatar : "👧";
                holder.tvAvatar.setText(avatar);

                // Set name
                holder.tvChildName.setText(child.name != null ? child.name : "Child");

                // Set age with proper handling
                String ageText = child.age != null && !child.age.isEmpty() ? child.age : "Not set";
                holder.tvChildAge.setText("Age: " + ageText);

                // Set radio button selection
                holder.radioSelect.setChecked(position == selectedPosition);

                // Click listeners
                holder.itemView.setOnClickListener(v -> {
                    selectedPosition = position;
                    notifyDataSetChanged();
                });

                holder.radioSelect.setOnClickListener(v -> {
                    selectedPosition = position;
                    notifyDataSetChanged();
                });
            }
        }

        @Override
        public int getItemCount() {
            return children != null ? children.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAvatar, tvChildName, tvChildAge;
            RadioButton radioSelect;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAvatar = itemView.findViewById(R.id.tvAvatar);
                tvChildName = itemView.findViewById(R.id.tvChildName);
                tvChildAge = itemView.findViewById(R.id.tvChildAge);
                radioSelect = itemView.findViewById(R.id.radioSelect);
            }
        }
    }
}