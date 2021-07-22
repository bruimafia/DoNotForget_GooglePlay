package ru.bruimafia.donotforget.util;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.databinding.NoteBinding;
import ru.bruimafia.donotforget.fragment.tasks.OnItemClickListener;
import ru.bruimafia.donotforget.repository.local_store.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    private List<Note> notes;
    private OnItemClickListener listener;

    public NoteAdapter(OnItemClickListener listener) {
        this.notes = new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<Note> newNotes) {
        if (notes != null) {
            notes.clear();
            notes.addAll(newNotes);
            notifyDataSetChanged();
        } else
            notes = newNotes; // first initialization
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        NoteBinding binding = DataBindingUtil.inflate(inflater, R.layout.note, parent, false);
        return new NoteHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteHolder holder, int position) {
        holder.bind(notes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        NoteBinding binding;

        public NoteHolder(NoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Note note, OnItemClickListener listener) {
            binding.setNote(note);
            binding.executePendingBindings();
            itemView.setOnClickListener(v -> listener.onItemClick(note));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(note);
                return true;
            });
        }

    }

}
