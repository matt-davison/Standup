package com.mdavison.standup.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.mdavison.standup.R;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_PICTURES;


/**
 * This Fragment implements the Compose user story, allowing the user to
 * create and upload posts.
 */
public class ComposeFragment extends Fragment
        implements MultiSelectionSpinnerDialog.OnMultiSpinnerSelectionListener {

    public final static int PICK_PHOTO_CODE = 8;
    private static final String TAG = "ComposeFragment";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 7;
    private static final String PHOTO_FILENAME = "photo.jpg";
    private File photoFile;
    private ImageView ivPostImage;
    private EditText etTitle;
    private EditText etDescription;
    private MultiSpinner msCommunity;
    private HashMap<String, Community> userCommunities;
    private List<Community> selectedCommunities;

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
        final Button btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        ivPostImage = view.findViewById(R.id.ivPostImage);
        final Button btnSubmit = view.findViewById(R.id.btnSubmit);
        final Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        msCommunity = view.findViewById(R.id.msCommunity);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto();
            }
        });
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etTitle.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Title cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedCommunities == null || selectedCommunities.isEmpty()) {
                    Toast.makeText(getContext(),
                            "Select communities to post to", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                savePost();
            }
        });
        userCommunities = new HashMap<>();
        setAvailableCommunities();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage =
                        rotateBitmapOrientation(photoFile.getAbsolutePath());
                ivPostImage.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getContext(), "Picture wasn't taken!",
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PICK_PHOTO_CODE && data != null) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = loadFromUri(photoUri);
            File mediaStorageDir = new File(
                    getContext().getExternalFilesDir(DIRECTORY_PICTURES), TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
            }
            String destinationFilename =
                    mediaStorageDir.getPath() + File.separator + PHOTO_FILENAME;
            try (FileOutputStream out = new FileOutputStream(
                    destinationFilename)) {
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            photoFile = new File(destinationFilename);
            ivPostImage.setImageBitmap(selectedImage);
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(PHOTO_FILENAME);
        Uri fileProvider = FileProvider.getUriForFile(getContext(),
                "com.codepath.fileprovider.standup", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void savePost() {
        //save the post to Parse
        //create the post
        //for each selectedCommunity add the post to its post relation and save
        Post newPost = new Post();
        newPost.setAuthor(ParseUser.getCurrentUser());
        newPost.setDescription(etDescription.getText().toString());
        newPost.setTitle(etTitle.getText().toString());
        newPost.setMedia(new ParseFile(photoFile));
        ParseRelation<Community> userCommunities =
                newPost.getRelation("postedTo");
        for (Community community : selectedCommunities) {
            userCommunities.add(community);
        }
        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Post uploaded!",
                            Toast.LENGTH_SHORT).show();
                    etDescription.setText("");
                    etTitle.setText("");
                    ivPostImage.setImageResource(R.drawable.ic_add_box_24px);
                }
            }
        });
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific
        // directories.
        // This way, we don't need to request external read/write runtime
        // permissions.
        File mediaStorageDir =
                new File(getContext().getExternalFilesDir(DIRECTORY_PICTURES),
                        TAG);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }


    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder
                        .createSource(getContext().getContentResolver(),
                                photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media
                        .getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app
        // can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    private void setAvailableCommunities() {
        ParseRelation<Community> communityParseRelation =
                ParseUser.getCurrentUser().getRelation("communities");
        communityParseRelation.getQuery()
                .findInBackground(new FindCallback<Community>() {
                    @Override
                    public void done(List<Community> objects,
                                     ParseException e) {
                        if (e != null) {
                            //TODO: Handle this
                            return;
                        } else {
                            List<String> communityNames = new ArrayList<>();
                            for (Community community : objects) {
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
        selectedCommunities = new ArrayList<>();
        for (String communityName : chosenItems) {
            selectedCommunities.add(userCommunities.get(communityName));
            Log.i("ComposeFragment", "selected: " + communityName);
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation =
                orientString != null ? Integer.parseInt(orientString) :
                        ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
            rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
            rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
            rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2,
                (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight,
                        matrix, true);
        // Return result
        return rotatedBitmap;
    }

}