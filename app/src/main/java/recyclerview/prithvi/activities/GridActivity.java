package recyclerview.prithvi.activities;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import recyclerview.prithvi.R;
import recyclerview.prithvi.adapter.GridAdapter;
import recyclerview.prithvi.model.UnsplashData;
import recyclerview.prithvi.utils.VolleySingleton;

import static recyclerview.prithvi.model.Endpoints.PHOTOS_URL;
import static recyclerview.prithvi.model.ResponseKeys.IMAGE_HEIGHT;
import static recyclerview.prithvi.model.ResponseKeys.IMAGE_ID;
import static recyclerview.prithvi.model.ResponseKeys.IMAGE_URLS;
import static recyclerview.prithvi.model.ResponseKeys.IMAGE_URLS_SMALL;
import static recyclerview.prithvi.model.ResponseKeys.IMAGE_WIDTH;

public class GridActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;

    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue = null;
    private ArrayList<UnsplashData> unsplashList = new ArrayList();
    private GridAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;

    private int visibleItemCount, totalItemCount, firstVisibleItem;
    private int previousTotal = 0, pageCount = 1, visibleThreshold = 4;
    private boolean loading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        volleySingleton = VolleySingleton.getsInstance();
        requestQueue = volleySingleton.getRequestQueue();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGridLayoutManager = new GridLayoutManager(this, 3);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mGridLayoutManager = new GridLayoutManager(this, 2);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rvUnsplash);
        mAdapter = new GridAdapter(unsplashList, getApplicationContext());

        sendJSONRequest(PHOTOS_URL);
        setUpRecyclerView(mRecyclerView);
    }

    public void sendJSONRequest(String url){
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        unsplashList = parseJSONResponse(response);
                        mAdapter.notifyItemInserted(unsplashList.size());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(request);
    }

    private ArrayList<UnsplashData> parseJSONResponse(JSONArray response){

        try{
            for(int i = 0; i < response.length(); i++){
                String id, width, height, urlRegular;
                JSONObject image, urls;
                image = (JSONObject) response.get(i);
                id = image.getString(IMAGE_ID);
                width = image.getString(IMAGE_WIDTH);
                height = image.getString(IMAGE_HEIGHT);
                urls = image.getJSONObject(IMAGE_URLS);
                urlRegular = urls.getString(IMAGE_URLS_SMALL);

                UnsplashData unsplashData = new UnsplashData();
                unsplashData.setId(id);
                unsplashData.setWidth(width);
                unsplashData.setHeight(height);
                unsplashData.setUrlRegular(urlRegular);
                unsplashList.add(unsplashData);
            }
        }catch (JSONException e){

        }
        return unsplashList;
    }

    private void setUpRecyclerView(RecyclerView rv){
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setLayoutManager(mGridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                String url;

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mGridLayoutManager.getItemCount();
                firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                        pageCount++;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)){
                    url = PHOTOS_URL + "&page=" + String.valueOf(pageCount);
                    sendJSONRequest(url);
                    loading = true;
                }
            }
        });
        rv.setAdapter(mAdapter);
    }
}
