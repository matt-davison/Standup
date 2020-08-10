package com.mdavison.standup.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.anurag.multiselectionspinner.MultiSelectionSpinnerDialog;
import com.anurag.multiselectionspinner.MultiSpinner;
import com.bumptech.glide.Glide;
import com.mdavison.standup.R;
import com.mdavison.standup.activities.PostDetailsActivity;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.mdavison.standup.support.Extras;
import com.mdavison.standup.support.ImageHelp;
import com.mdavison.standup.support.RequestCodes;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.provider.MediaStore.*;


/**
 * This Fragment implements the Compose user story, allowing the user to
 * create, edit, and upload posts.
 */
public class ComposeFragment extends Fragment
        implements MultiSelectionSpinnerDialog.OnMultiSpinnerSelectionListener {

    private static final String TAG = "ComposeFragment";
    private static final String PHOTO_FILENAME = "photo.gif";
    private File photoFile;
    private ImageView ivPostImage;
    private EditText etTitle;
    private EditText etDescription;
    private MultiSpinner msCommunity;
    private HashMap<String, Community> userCommunities = new HashMap<>();
    private Set<Community> selectedCommunities;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        ivPostImage = view.findViewById(R.id.ivPostImage);
        msCommunity = view.findViewById(R.id.msCommunity);

        final Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto();
            }
        });

        final Button btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        final Button btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etTitle.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Title cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedCommunities == null ||
                        selectedCommunities.isEmpty()) {
                    Toast.makeText(getContext(),
                            "Select communities to post to", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                savePost();
            }
        });
        setAvailableCommunities();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = ImageHelp
                        .rotateBitmapOrientation(photoFile.getAbsolutePath());
                ivPostImage.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!",
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == RequestCodes.PICK_PHOTO_CODE && data != null) {
            Uri photoUri = data.getData();
            File mediaStorageDir = new File(
                    getContext().getExternalFilesDir(DIRECTORY_PICTURES), TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                Toast.makeText(getContext(), "Failed to select photo",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String destinationFilename =
                    mediaStorageDir.getPath() + File.separator + PHOTO_FILENAME;
            try {
                InputStream in =  getContext().getContentResolver().openInputStream(photoUri);
                FileOutputStream out = new FileOutputStream(new File(destinationFilename));
                byte[] buf = new byte[1024];
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
                out.close();
                in.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "failed to save photo");
                Toast.makeText(getContext(), "Failed to select photo",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            photoFile = new File(destinationFilename);
            Glide.with(getContext()).load(photoUri).into(ivPostImage);
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(ACTION_IMAGE_CAPTURE);
        photoFile =
                ImageHelp.getPhotoFileUri(getContext(), TAG, PHOTO_FILENAME);
        Uri fileProvider = FileProvider.getUriForFile(getContext(),
                "com.codepath.fileprovider.standup", photoFile);
        intent.putExtra(EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent,
                    RequestCodes.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void savePost() {
        Post newPost = new Post();
        newPost.setAuthor(ParseUser.getCurrentUser());
        newPost.setDescription(etDescription.getText().toString());
        newPost.setTitle(etTitle.getText().toString());
        if (photoFile != null) {
            newPost.setMedia(new ParseFile(photoFile));
        }
        ParseRelation<Community> userCommunities =
                newPost.getRelation("postedTo");
        for (Community community : selectedCommunities) {
            userCommunities.add(community);
        }
        Toast.makeText(getContext(), "Uploading Post!",
                Toast.LENGTH_SHORT).show();
        newPost.saveInBackground(parseException -> {
            if (parseException != null) {
                Log.e(TAG, "Error while saving", parseException);
                Toast.makeText(getContext(), "Error while saving",
                        Toast.LENGTH_SHORT).show();
            } else {
                etDescription.setText("");
                etTitle.setText("");
                ivPostImage.setImageResource(R.drawable.ic_add_box_24px);
                Intent i = new Intent(getContext(), PostDetailsActivity.class);
                i.putExtra(Extras.EXTRA_POST, Parcels.wrap(newPost));
                startActivity(i);
            }
        });
    }

    // Trigger gallery selection for a photo
    private void onPickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, RequestCodes.PICK_PHOTO_CODE);
        }
    }

    private void setAvailableCommunities() {
        ParseRelation<Community> communityParseRelation =
                ParseUser.getCurrentUser().getRelation("communities");
        communityParseRelation.getQuery()
                .findInBackground(new FindCallback<Community>() {
                    @Override
                    public void done(List<Community> communities,
                                     ParseException e) {
                        if (e != null) {
                            //TODO: Handle this
                            Log.e(TAG, "Error getting available communities");
                        } else {
                            List<String> communityNames = new ArrayList<>();
                            for (Community community : communities) {
                                userCommunities
                                        .put(community.getName(), community);
                                communityNames.add(community.getName());
                            }
                            msCommunity.setAdapterWithOutImage(getContext(),
                                    communityNames, ComposeFragment.this);
                            msCommunity.initMultiSpinner(getContext(),
                                    msCommunity);
                        }
                    }
                });
    }

    @Override
    public void OnMultiSpinnerItemSelected(List<String> chosenItems) {
        selectedCommunities = new ArraySet<>();
        for (String communityName : chosenItems) {
            selectedCommunities.add(userCommunities.get(communityName));
        }
    }

}