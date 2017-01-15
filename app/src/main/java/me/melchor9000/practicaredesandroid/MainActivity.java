package me.melchor9000.practicaredesandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.melchor9000.practicaredesandroid.lastfm.AlbumSearchResult;
import me.melchor9000.practicaredesandroid.lastfm.ArtistSearchResult;
import me.melchor9000.practicaredesandroid.lastfm.LastFMApi;
import me.melchor9000.practicaredesandroid.lastfm.SearchResult;
import me.melchor9000.practicaredesandroid.lastfm.TrackSearchResult;

public class MainActivity extends AppCompatActivity {
    private List<SearchResult> searchResultItems1 = new ArrayList<>(),
        searchResultItems2 = new ArrayList<>(),
        searchResultItems3 = new ArrayList<>();
    private SearchResultItemsAdapter adapter = new SearchResultItemsAdapter();
    private LinearLayoutManager recyclerViewLayoutManager;
    @BindView(R.id.search_results) RecyclerView recyclerView;
    @BindView(R.id.editText) EditText editText;
    @BindView(R.id.progressBar4) ProgressBar progressBar;
    @BindView(R.id.button) Button searchButton;
    private int page = 0;
    private boolean addingMoreResults = false;
    private int prevItemCount = 0;
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Preparamos el terreno para usar la lista de resultados
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(recyclerViewLayoutManager = new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter = new SearchResultItemsAdapter());

        //Con este callback vamos cargando nuevos resultados a medida que el usuario se acerca al final
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = recyclerViewLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = recyclerViewLayoutManager.getItemCount();

                if(totalItemCount < prevItemCount) {
                    page = 1;
                    prevItemCount = totalItemCount;
                    if(prevItemCount == 0) addingMoreResults = true;
                }

                if(!addingMoreResults && (lastVisibleItemPosition + /*visibleThreshold = */ 5) > totalItemCount) {
                    page++;
                    addingMoreResults = true;
                    addMoreResults();
                }
            }
        });
    }

    public void doSearch(View view) {
        //Realiza la busqueda
        page = 1;
        addingMoreResults = true;
        int size = searchResultItems1.size() + searchResultItems2.size() + searchResultItems3.size();
        searchResultItems1.clear(); searchResultItems2.clear(); searchResultItems3.clear();
        adapter.notifyItemRangeRemoved(0, size);
        if(editText.length() != 0)
            addMoreResults();
    }

    public void addMoreResults() {
        //Pide más resultados a LastFM y luego los muestra
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        String ss = editText.getText().toString();
        LastFMApi.Artist.search(ss, 5, page).setCallback(new FutureResponse.ResponseCallback<List<ArtistSearchResult>>() {
            @Override
            public void done(List<ArtistSearchResult> o, Exception e) {
                addResults(o, e, 0);
            }
        });
        LastFMApi.Album.search(ss, 5, page).setCallback(new FutureResponse.ResponseCallback<List<AlbumSearchResult>>() {
            @Override
            public void done(List<AlbumSearchResult> o, Exception e) {
                addResults(o, e, 1);
            }
        });
        LastFMApi.Track.search(ss, null, 5, page).setCallback(new FutureResponse.ResponseCallback<List<TrackSearchResult>>() {
            @Override
            public void done(List<TrackSearchResult> o, Exception e) {
                addResults(o, e, 2);
            }
        });

        //Ocultamos el teclado al buscar
        // http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private int addResultsCalls = 0;

    /**
     * Añade los resultados a la vista
     * @param o lista de resultados
     * @param e error si ha habido uno
     * @param stride 0 para artistas, 1 para albumes y 2 para canciones
     * @param <SearchResultType> el tipo del resultado de búsqueda
     */
    private <SearchResultType extends SearchResult> void addResults(final List<SearchResultType> o, final Exception e, final int stride) {
        h.post(new Runnable() {
            @Override
            public void run() {
                if(e == null) {
                    if(stride == 0) searchResultItems1.addAll(o); else
                    if(stride == 1) searchResultItems2.addAll(o); else
                    if(stride == 2) searchResultItems3.addAll(o);
                } else {
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.error_searching), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                if(++addResultsCalls % 3 == 0) {
                    //Mostramos los resultados cuando hemos cargado todos
                    addingMoreResults = false;
                    int count = adapter.getItemCount();
                    adapter.notifyItemRangeInserted(prevItemCount, count - prevItemCount);
                    prevItemCount = count;
                    Log.d("addResults", addResultsCalls + " - " + prevItemCount + "/" + count + " - " + (count - prevItemCount));
                    progressBar.setVisibility(View.GONE);
                    searchButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * El adaptador de la lista
     */
    private class SearchResultItemsAdapter extends RecyclerView.Adapter<SearchResultItemsAdapter.Holder> {

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView view = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_item, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(final Holder holder, int position) {
            SearchResult result = resultForPosition(position);
            if(result == null) {
                holder.title().setText("");
                holder.type().setText((position / 5 % 3) == 0 ? R.string.artist : R.string.album);
                holder.content().setText("");
            } else if(result instanceof AlbumSearchResult) {
                holder.title().setText(result.getName());
                holder.type().setText(R.string.album);
                holder.content().setText(((AlbumSearchResult) result).getArtist());
            } else if(result instanceof ArtistSearchResult) {
                holder.title().setText(result.getName());
                holder.type().setText(R.string.artist);
                holder.content().setText("");
            } else if(result instanceof TrackSearchResult) {
                holder.title().setText(result.getName());
                holder.type().setText(R.string.track);
                holder.content().setText(((TrackSearchResult) result).getArtist());
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    SearchResult res = resultForPosition(pos);
                    Intent i = new Intent();
                    if(res instanceof ArtistSearchResult) {
                        i.setClass(MainActivity.this, ArtistActivity.class);
                        i.putExtra("artist", res.getName());
                    } else if(res instanceof AlbumSearchResult) {
                        i.setClass(MainActivity.this, AlbumActivity.class);
                        i.putExtra("album", res.getName());
                        i.putExtra("artist", ((AlbumSearchResult) res).getArtist());
                    } else if(res instanceof TrackSearchResult) {
                        i.setClass(MainActivity.this, TrackActivity.class);
                        i.putExtra("track", res.getName());
                        i.putExtra("artist", ((TrackSearchResult) res).getArtist());
                    }
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return searchResultItems1.size() + searchResultItems2.size() + searchResultItems3.size();
        }

        private SearchResult resultForPosition(int position) {
            int type = (position / 5) % 3;
            int pos = position / 15 + position % 5;
            if(type == 0 && pos < searchResultItems1.size()) return searchResultItems1.get(pos);
            else if(type == 1 && pos < searchResultItems2.size()) return searchResultItems2.get(pos);
            else if(type == 2 && pos < searchResultItems3.size()) return searchResultItems3.get(pos);
            return null;
        }

        class Holder extends RecyclerView.ViewHolder {
            private CardView view;
            private Holder(CardView itemView) {
                super(itemView);
                view = itemView;
            }

            private TextView type() {
                return (TextView) view.findViewById(R.id.search_result_item_type);
            }

            private TextView title() {
                return (TextView) view.findViewById(R.id.search_result_item_title);
            }

            private TextView content() {
                return (TextView) view.findViewById(R.id.search_result_item_content);
            }
        }
    }
}
