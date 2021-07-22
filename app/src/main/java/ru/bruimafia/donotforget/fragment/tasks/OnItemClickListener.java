package ru.bruimafia.donotforget.fragment.tasks;


import ru.bruimafia.donotforget.repository.local_store.Note;

public interface OnItemClickListener {
    void onItemClick(Note note);

    boolean onItemLongClick(Note note);
}
