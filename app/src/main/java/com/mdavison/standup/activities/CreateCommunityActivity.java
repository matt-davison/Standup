package com.mdavison.standup.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mdavison.standup.R;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.models.User;
import com.mdavison.standup.support.Extras;
import com.mdavison.standup.support.ImageHelp;
import com.mdavison.standup.support.RequestCodes;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.DIRECTORY_PICTURES;

public class CreateCommunityActivity extends AppCompatActivity {

    private static final String TAG = "CreateCommunityActivity";
    private static final String PHOTO_FILENAME = "photo.jpg";
    private EditText etName;
    private EditText etDescription;
    private ImageView ivIcon;
    private ImageView ivBanner;
    private File iconFile;
    private File bannerFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);

        etName = findViewById(R.id.etName);
        String nameSuggestion = getIntent().getStringExtra(
                Extras.EXTRA_COMMUNITY_NAME);
        if (nameSuggestion != null) {
            etName.setText(nameSuggestion);
        }
        etDescription = findViewById(R.id.etDescription);
        ivIcon = findViewById(R.id.ivIcon);
        ivIcon.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, RequestCodes.PICK_ICON_CODE);
            }
        });
        ivBanner = findViewById(R.id.ivBanner);
        ivBanner.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, RequestCodes.PICK_BANNER_CODE);
            }
        });
        final Button btnCreateCommunity = findViewById(R.id.btnCreateCommunity);
        btnCreateCommunity.setOnClickListener(view -> {
            if (etName.getText().toString().isEmpty()) {
                Toast.makeText(this, "Title cannot be empty",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            saveCommunity();
        });
    }

    private void saveCommunity() {
        Community community = new Community();
        community.setDescription(etDescription.getText().toString());
        community.setName(etName.getText().toString());
        //TODO: Check if community with name already exists
        if (iconFile != null) {
            community.setIcon(new ParseFile(iconFile));
        }
        if (bannerFile != null) {
            community.setBanner(new ParseFile(bannerFile));
        }
        community.saveInBackground(parseException -> {
            if (parseException != null) {
                Log.e(TAG, "Error while saving", parseException);
                Toast.makeText(this, "Error while saving", Toast.LENGTH_SHORT)
                        .show();
            } else {
                //TODO: Show new Community in a new CommunityDetailsActivity
                Toast.makeText(this, "Community created!", Toast.LENGTH_SHORT)
                        .show();
                etDescription.setText("");
                etName.setText("");
                ivIcon.setImageResource(R.drawable.ic_add_box_24px);
                ivBanner.setImageResource(R.drawable.ic_add_box_24px);
                ParseUser.getCurrentUser().getRelation(User.KEY_COMMUNITIES).add(community);
                ParseUser.getCurrentUser().saveInBackground();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.PICK_ICON_CODE ||
                requestCode == RequestCodes.PICK_BANNER_CODE && data != null) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = ImageHelp.loadFromUri(this, photoUri);
            File mediaStorageDir =
                    new File(getExternalFilesDir(DIRECTORY_PICTURES), TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                Toast.makeText(this, "Failed to select photo",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String destinationFilename =
                    mediaStorageDir.getPath() + File.separator + PHOTO_FILENAME;
            try (FileOutputStream out = new FileOutputStream(
                    destinationFilename)) {
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "failed to save photo");
                Toast.makeText(this, "Failed to select photo",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (requestCode == RequestCodes.PICK_ICON_CODE) {
                iconFile = new File(destinationFilename);
                ivIcon.setImageBitmap(selectedImage);
            } else {
                bannerFile = new File(destinationFilename);
                ivBanner.setImageBitmap(selectedImage);
            }
        }
    }
}