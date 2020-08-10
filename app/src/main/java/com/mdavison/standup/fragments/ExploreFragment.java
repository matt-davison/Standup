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
import android.widget.TextView;
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
import com.parse.FindCallback;
import com.parse.ParseException;
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
    private List<Community> suggested;
    private CommunityAdapter communityAdapter;
    private CommunityAdapter suggestedAdapter;
    private RecyclerView rvContent;
    private RecyclerView rvSuggested;
    private Button btnCreateCommunity;
    private EditText etSearch;
    private TextView tvSuggested;
    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        suggestedAdapter.clear();
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
        etSearch = view.findViewById(R.id.etSearch);
        tvSuggested =view.findViewById(R.id.tvSuggested);
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
        communities = new ArrayList<>();
        communityAdapter = new CommunityAdapter(getContext(), communities);
        rvContent = view.findViewById(R.id.rvContent);
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
        suggested = new ArrayList<>();
        suggestedAdapter = new CommunityAdapter(getContext(), suggested);
        rvSuggested = view.findViewById(R.id.rvSuggested);
        rvSuggested.setAdapter(suggestedAdapter);
        final LinearLayoutManager suggestedManager =
                new LinearLayoutManager(getContext());
        rvSuggested.setLayoutManager(suggestedManager);
        ItemClickSupport.addTo(rvSuggested).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView,
                                              int position, View v) {
                        Intent i = new Intent(getContext(),
                                CommunityDetailsActivity.class);
                        i.putExtra(Extras.EXTRA_COMMUNITY,
                                Parcels.wrap(suggested.get(position)));
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
        rvSuggested.addOnScrollListener(scrollListener);
    }

    private void querySuggested() {
        ParseQuery<Community> querySuggested = ParseQuery.getQuery("Community");
        querySuggested.whereDoesNotMatchKeyInQuery(Community.KEY_OBJECT_ID,
                Community.KEY_OBJECT_ID,
                ParseUser.getCurrentUser().getRelation("communities").getQuery());
        querySuggested.setLimit(20);
        querySuggested.setSkip(suggested.size());
        querySuggested.addDescendingOrder(Community.KEY_USER_COUNT);
        querySuggested.findInBackground(new FindCallback<Community>() {
            @Override
            public void done(List<Community> newSuggested, ParseException e) {
                suggestedAdapter.addAll(newSuggested);
                showSuggested();
            }
        });
    }

    private void showSuggested() {
        tvSuggested.setVisibility(View.VISIBLE);
    }

    private void hideSuggested() {
        tvSuggested.setVisibility(View.GONE);
        suggestedAdapter.clear();
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
            querySuggested();
        } else {
            hideSuggested();
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