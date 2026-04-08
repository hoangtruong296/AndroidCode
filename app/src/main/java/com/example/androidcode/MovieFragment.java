package com.example.androidcode;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MovieFragment extends Fragment implements MovieAdapter.OnMovieClickListener {

    private RecyclerView rvMovies;
    private MovieAdapter adapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        rvMovies = view.findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(getContext()));
        
        movieList = new ArrayList<>();
        adapter = new MovieAdapter(movieList, this);
        rvMovies.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadMovies();

        return view;
    }

    private void loadMovies() {
        db.collection("movies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                movieList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movie movie = document.toObject(Movie.class);
                    movieList.add(movie);
                }
                adapter.notifyDataSetChanged();
                
                if (movieList.isEmpty()) {
                    // Add some dummy data if empty for demonstration
                    addDummyData();
                }
            } else {
                Toast.makeText(getContext(), "Lỗi tải phim", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDummyData() {
        Movie m1 = new Movie("1", "Spider-man: No Way Home", "Peter Parker's life is turned upside down.", "https://example.com/spiderman.jpg", 9.0);
        Movie m2 = new Movie("2", "Avengers: Endgame", "The Avengers assemble once more.", "https://example.com/avengers.jpg", 8.8);
        db.collection("movies").document(m1.getId()).set(m1);
        db.collection("movies").document(m2.getId()).set(m2);
        loadMovies();
    }

    @Override
    public void onBookClick(Movie movie) {
        Intent intent = new Intent(getActivity(), BookingActivity.class);
        intent.putExtra("movie_id", movie.getId());
        intent.putExtra("movie_title", movie.getTitle());
        startActivity(intent);
    }
}