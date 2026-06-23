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
import java.util.List;

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

        btnAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChildSelectionActivity.this, AddChildProfileActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition != -1) {
                    ChildProfile selectedChild = childList.get(selectedPosition);
                    SharedPreferences childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE);
                    childPrefs.edit()
                            .putString("childId", selectedChild.id)
                            .putString("childName", selectedChild.name)
                            .putString("childAvatar", selectedChild.avatar)
                            .apply();

                    Intent intent = new Intent(ChildSelectionActivity.this, ChildHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ChildSelectionActivity.this, "Please select a child", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadChildren() {
        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ChildProfile child = new ChildProfile();
                        child.id = doc.getId();
                        child.name = doc.getString("childName");
                        child.age = doc.getString("age");
                        child.gender = doc.getString("gender");
                        child.avatar = doc.getString("avatar");
                        if (child.avatar == null) child.avatar = "👧";
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
        private int lastSelectedPosition = -1;

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
            holder.tvAvatar.setText(child.avatar);
            holder.tvChildName.setText(child.name);
            holder.tvChildAge.setText("Age: " + child.age);
            holder.radioSelect.setChecked(position == lastSelectedPosition);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastSelectedPosition = position;
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });

            holder.radioSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastSelectedPosition = position;
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return children.size();
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