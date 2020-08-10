package com.mdavison.standup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mdavison.standup.R;
import com.mdavison.standup.activities.CommunityDetailsActivity;
import com.mdavison.standup.activities.CreateCommunityActivity;
import com.mdavison.standup.adapters.CommunityAdapter;
import com.mdavison.standup.models.Community;
import com.mdavison.standup.support.EndlessRecyclerViewScrollListener;
import com.mdavison.standup.support.Extras;
import com.mdavison.standup.support.ItemClickSupport;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment shows an explore feed, featuring recommended content and the
 * ability to search for Communities, Posts, and Users.
 */
public class ExploreFragment extends Fragment {
    private static final String TAG = "ExploreFragment";
    private List<Community> communities;
    private CommunityAdapter communityAdapter;
    private EditText etSearch;

    private RecyclerView rvContent;
    private Button btnCreateCommunity;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        queryCommunities();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        communities = new ArrayList<>();
        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i,
                                          int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1,
                                      int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                queryCommunities();
            }
        });
        rvContent = view.findViewById(R.id.rvContent);
        btnCreateCommunity = view.findViewById(R.id.btnCreateCommunity);
        btnCreateCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =
                        new Intent(getContext(), CreateCommunityActivity.class);
                i.putExtra(Extras.EXTRA_COMMUNITY_NAME,
                        etSearch.getText().toString());
                startActivity(i);
            }
        });
        communityAdapter = new CommunityAdapter(getContext(), communities);
        rvContent.setAdapter(communityAdapter);
        final LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext());
        rvContent.setLayoutManager(layoutManager);
        ItemClickSupport.addTo(rvContent).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView,
                                              int position, View v) {
                        Intent i = new Intent(getContext(),
                                CommunityDetailsActivity.class);
                        i.putExtra(Extras.EXTRA_COMMUNITY,
                                Parcels.wrap(communities.get(position)));
                        startActivity(i);
                    }
                });
        final EndlessRecyclerViewScrollListener scrollListener =
                new EndlessRecyclerViewScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount,
                                           RecyclerView view) {
                        queryCommunities();
                    }
                };
        rvContent.addOnScrollListener(scrollListener);
    }

    private void queryCommunities() {
        if (etSearch.getText().toString().isEmpty()) {
            ParseRelation<Community> communitiesRelation =
                    ParseUser.getCurrentUser().getRelation("communities");
            communitiesRelation.getQuery()
                    .addDescendingOrder(Community.KEY_USER_COUNT)
                    .findInBackground((newCommunities, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Issue with getting communities", error);
                            Toast.makeText(getContext(),
                                    "Issue getting communities",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        communityAdapter.clear();
                        communityAdapter.addAll(newCommunities);
                        if (communities.size() > 0) {
                            rvContent.setVisibility(View.VISIBLE);
                            btnCreateCommunity.setVisibility(View.GONE);
                        } else {
                            rvContent.setVisibility(View.GONE);
                            btnCreateCommunity.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            //TODO: Refactor to combine query results logic
            ParseQuery<Community> query = ParseQuery.getQuery(Community.class);
            query.setLimit(20);
            query.addDescendingOrder(Community.KEY_USER_COUNT);
            query.whereStartsWith(Community.KEY_NAME,
                    etSearch.getText().toString());
            query.findInBackground((newCommunities, error) -> {
                if (error != null) {
                    Log.e(TAG, "Issue searching communities", error);
                    Toast.makeText(getContext(), "Issue searching communities",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                communityAdapter.clear();
                communityAdapter.addAll(newCommunities);
                if (communities.size() > 0) {
                    rvContent.setVisibility(View.VISIBLE);
                    btnCreateCommunity.setVisibility(View.GONE);
                } else {
                    rvContent.setVisibility(View.GONE);
                    btnCreateCommunity.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}