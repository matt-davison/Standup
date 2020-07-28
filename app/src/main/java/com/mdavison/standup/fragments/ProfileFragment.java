package com.mdavison.standup.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.activities.PostDetailsActivity;
import com.mdavison.standup.adapters.PostAdapter;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.support.EndlessRecyclerViewScrollListener;
import com.mdavison.standup.support.Extras;
import com.mdavison.standup.support.ImageHelp;
import com.mdavison.standup.support.ItemClickSupport;
import com.mdavison.standup.support.RequestCodes;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * This Fragment shows details about a user.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String PHOTO_FILENAME = "photo.jpg";
    private List<Post> userPosts;
    private PostAdapter postAdapter;
    private ParseUser user;
    private ImageView ivProfile;
    private File photoFile;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfile = view.findViewById(R.id.ivProfile);
        final ParseUser selectedUser = Parcels.unwrap(getActivity().getIntent()
                .getParcelableExtra(Extras.EXTRA_USER));
        if (selectedUser == null) {
            user = ParseUser.getCurrentUser();
            final ImageView ivLogout = view.findViewById(R.id.ivLogout);
            ivLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUser.logOut();
                    getActivity().finish();
                }
            });
            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchCamera();
                }
            });
        } else {
            user = selectedUser;
        }
        final TextView tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername.setText(user.getUsername());
        final ImageView ivProfile = view.findViewById(R.id.ivProfile);
        final ParseFile profileImage =
                (ParseFile) user.get("picture");
        if (profileImage != null) {
            Glide.with(getContext()).load(profileImage.getUrl()).into(ivProfile);
        }
        userPosts = new ArrayList<>();
        final RecyclerView rvPosts = view.findViewById(R.id.rvPosts);
        postAdapter = new PostAdapter(getContext(), userPosts);
        rvPosts.setAdapter(postAdapter);
        final LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(layoutManager);
        queryPosts();
        ItemClickSupport.addTo(rvPosts).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView,
                                              int position, View v) {
                        Intent i = new Intent(getContext(),
                                PostDetailsActivity.class);
                        i.putExtra(Extras.EXTRA_POST,
                                Parcels.wrap(userPosts.get(position)));
                        startActivity(i);
                    }
                });
        final EndlessRecyclerViewScrollListener scrollListener =
                new EndlessRecyclerViewScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount,
                                           RecyclerView view) {
                        queryPosts();
                    }
                };
        rvPosts.addOnScrollListener(scrollListener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = ImageHelp
                        .rotateBitmapOrientation(photoFile.getAbsolutePath());
                ivProfile.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void queryPosts() {
            ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
            query.whereEqualTo(Post.KEY_AUTHOR, user);
            query.setLimit(20);
            query.setSkip(userPosts.size());
            query.addDescendingOrder(Post.KEY_CREATED);
            //query.include(Post.KEY_AUTHOR);
            query.findInBackground((newPosts, error) -> {
                if (error != null) {
                    Log.e(TAG, "Issue with getting posts", error);
                    Toast.makeText(getContext(), "Issue getting posts",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                postAdapter.addAll(newPosts);
                Log.i(TAG, "Received " + newPosts.size() + " posts");
        });
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile =
                ImageHelp.getPhotoFileUri(getContext(), TAG, PHOTO_FILENAME);
        Uri fileProvider = FileProvider.getUriForFile(getContext(),
                "com.codepath.fileprovider.standup", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent,
                    RequestCodes.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
}
