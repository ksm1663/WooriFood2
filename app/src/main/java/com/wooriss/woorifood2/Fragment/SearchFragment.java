package com.wooriss.woorifood2.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Control.FoodLocation;
import com.wooriss.woorifood2.MainActivity;
import com.wooriss.woorifood2.Model.PageListSikdang;
import com.wooriss.woorifood2.Model.Sikdang;
import com.wooriss.woorifood2.Model.SikdangAdapter;
import com.wooriss.woorifood2.Model.User;
import com.wooriss.woorifood2.R;

import java.util.List;

import retrofit2.Response;

public class SearchFragment extends Fragment {
    private Context context;
    private FirebaseUser f_user;
    private User user;

    private FirebaseFirestore fireStore;

    private LinearLayout laySearchMain;
    private EditText textBranch;
    private Button btnSearch;
    private FloatingActionButton btnMap;
    private TextView nonText;
    private ImageView nonImage;
    private SwipeRefreshLayout swipeSearchList;

    private static RecyclerView sikdangListRecyclerView;
    public static SikdangAdapter sikdangAdapter;
    private FragmentManager fManager;
    private MapFragment mapFragment;

    private static SearchFragment searchFragment;

    public static Response<PageListSikdang> pageListSikdang;

    public SearchFragment() { }

    public static SearchFragment newInstance (Bundle bundle) {
        if(searchFragment ==null) {
            searchFragment = new SearchFragment();
            MainActivity.foodLocation.setSearchfFragment(searchFragment);
            sikdangAdapter = new SikdangAdapter(null);

            searchFragment.setArguments(bundle);
        }
        return searchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            f_user = getArguments().getParcelable("f_user");
            user = (User) getArguments().getSerializable("user");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(container!=null)
            context = container.getContext();
        return inflater.inflate(R.layout.fragment_search, container, false);
    }


    public static int hadSearched = 0;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);
        setListner();
        init();
    }

    private void findViews(View v) {
        laySearchMain = v.findViewById(R.id.laySearchMain);
        textBranch = v.findViewById(R.id.textBranch);
        btnSearch = v.findViewById(R.id.btnSearch);
        btnMap = v.findViewById(R.id.btnMap);
        nonText = v.findViewById(R.id.non_item);
        nonImage = v.findViewById(R.id.non_item_img);

        swipeSearchList = v.findViewById(R.id.swipeSearchList);

        sikdangListRecyclerView = v.findViewById(R.id.sikdangList);
        sikdangListRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        sikdangListRecyclerView.setAdapter(sikdangAdapter);

        fManager = getParentFragmentManager();
    }

    private void setListner() {
        addListenerToSearchBtn();
        addListenerToMapBtn();
        addListenerToRecyclerView();
        addListenerToSwipeSearchList();
        addListenerToEditBranch();
    }

    private void init() {
        fireStore = FirebaseFirestore.getInstance();

        if (sikdangListRecyclerView.getAdapter().getItemCount() <= 0) {
            setNullResult(true);
        }
        else
            setNullResult(false);

        if (hadSearched == 0) {
            nonImage.setVisibility(View.VISIBLE);
            setNullResult(false);
        } else
            nonImage.setVisibility(View.GONE);

        //??????????????? ??????
        sikdangListRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL));
    }



    private String searchSikdang;
    // ?????? ?????? ????????? ??? ?????????
    private void addListenerToSearchBtn() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                searchSikdang = textBranch.getText().toString();

                if (searchSikdang.length() == 0) {
                    Snackbar.make(laySearchMain, getString(R.string.popNeedSikdang), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (FoodLocation.x != null)
                {
                    Log.d(Code.LOG_TAG, "call getSikdanList(addListenerToSearchBtn)");
                    MainActivity.foodLocation.getSikdangList(searchSikdang);
                }
            }
        });
    }



    // ?????? ?????? ????????? ??? ?????????
    private void addListenerToMapBtn() {
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment = MapFragment.newInstance(Code.MapType.SEARCH_MAP, getArguments());

                if (!mapFragment.isVisible()) {
                    fManager.beginTransaction().replace(R.id.main_container, mapFragment).addToBackStack(null).commit();
                }
            }
        });
    }


    // ?????????????????? ????????? ?????? ?????????
    private void addListenerToRecyclerView() {
        sikdangListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override  // ?????????, ????????? ??????
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (sikdangListRecyclerView.getAdapter().getItemCount() > 0) {

                    if (!recyclerView.canScrollVertically((-1))) {
                        Log.d(Code.LOG_TAG, "?????????????????? ?????? ???");
                    }

                    if (!recyclerView.canScrollVertically((1))) {
                        Log.d(Code.LOG_TAG, "?????????????????? ?????? ???");
                        MainActivity.foodLocation.callItemWithPaging(pageListSikdang.body().getMeta().total_count, 1);
                    }
                }
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }


    private void addListenerToSwipeSearchList() {
        swipeSearchList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if ((searchSikdang != null)) {
                    // swipe ??? ????????? ??????
                    MainActivity.foodLocation.getSikdangList(searchSikdang);
                }
                // ??????????????? ???????????? ??????
                swipeSearchList.setRefreshing(false);
            }
        });

    }

    // ???????????? ????????? ??????
    private void addListenerToEditBranch() {
        textBranch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    btnSearch.performClick();
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if(view==null)
            view = new View(context);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setSikdangList (Response<PageListSikdang> _response, int _curPageNum) {
        SearchFragmentListener listener = new SearchFragmentListener() {
            private Response<PageListSikdang> response = _response;
            private int curPageNum = _curPageNum;

            @Override
            //public void loadPage(Response<PageListSikdang> response, int curPageNum) {
            public void setList() {
                if (curPageNum <= 1)
                {
                    pageListSikdang = response;


                }
                else {
                    pageListSikdang.body().getDocuments().addAll(response.body().getDocuments());
                }

                if (pageListSikdang != null) {
                    sikdangAdapter.updateData(pageListSikdang.body().getDocuments());
                    sikdangAdapter.notifyDataSetChanged();
                }

                Log.d(Code.LOG_TAG, "set List successed");
            }
        };

        setViewType(listener, _response.body().getDocuments());
    }

    public void setNullResult (boolean flag) {

        if (nonImage != null && nonText != null) {
            if (hadSearched == 1)
                nonImage.setVisibility(View.GONE);

            if (flag == true) {
                nonText.setVisibility(View.VISIBLE);
            } else
                nonText.setVisibility(View.GONE);
        }
    }


    private int cntSetViewItem = 0;
    public void setViewType (SearchFragmentListener listener, List<Sikdang> sikdangList) {
        Log.d(Code.LOG_TAG, "sikdangList.size() : " + sikdangList.size() );
        cntSetViewItem = 0;
        if (sikdangList.size() == 0) {
            Log.d(Code.LOG_TAG, "sikdangList.size() == 0");
            listener.setList();
        }
        else {
            Log.d(Code.LOG_TAG, "sikdangList.size() != 0");
            for (int i = 0; i < sikdangList.size(); i++) {
                Log.d(Code.LOG_TAG, " i : " + i + ", (" + sikdangList.get(i).getPlace_name() + ")");


                chkReviewedSikdang(new MyCallback() {
                    @Override
                    public void onCallback(Sikdang sikdang, String baseSikdangId, int pos) {
                        Log.d(Code.LOG_TAG, "in onCallback , pos : " + pos);
                        cntSetViewItem++;
                        if (sikdang != null) {  // ?????? ?????? ????????? ??????
                            Log.d(Code.LOG_TAG, "numRating of " + sikdang.getId() + ", pos : " + pos);
                            sikdangList.get(pos).setViewType(Code.ViewType.REVIEWED_SIKDANG);
                            sikdangList.get(pos).setAvgTaste(sikdang.getAvgTaste());
                            sikdangList.get(pos).setNumRatings(sikdang.getNumRatings());

                            sikdangList.get(pos).setAvgPrice(sikdang.getAvgPrice());
                            sikdangList.get(pos).setAvgLuxury(sikdang.getAvgLuxury());
                            sikdangList.get(pos).setTitleImage(sikdang.getTitleImage());
                            sikdangList.get(pos).setTitleImageUri(sikdang.getTitleImageUri());


                        } else {                    // ?????? ?????? ????????? ??????
                            Log.d(Code.LOG_TAG, "null is mean default item set!! : " + baseSikdangId + ", pos" + pos);
                            sikdangList.get(pos).setViewType(Code.ViewType.DEFAULT_SIKDANG);
                        }

                        // ???????????? ?????? ??????????????? ??? ??????
                        if (sikdangList.size() == cntSetViewItem) {
                            Log.d(Code.LOG_TAG, "???????????????????????????????????????????????????");
                            cntSetViewItem = 0;
                            listener.setList();
                        }
                    }
                }, sikdangList.get(i).getId(), i);
            }
        }
    }

    public void chkReviewedSikdang(MyCallback mycallback, String sikdangId, int pos) {
        DocumentReference docRef = fireStore.collection("sikdangs").document(sikdangId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Sikdang sikdang = documentSnapshot.toObject(Sikdang.class);
                mycallback.onCallback(sikdang, sikdangId, pos);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //
                    }
                });
    }


    // inneer interface
    public interface SearchFragmentListener {
        void setList();
    }

    // inner interface
    public interface MyCallback {
        void onCallback (Sikdang sikdang, String baseSikdang, int pos);
    }
}
