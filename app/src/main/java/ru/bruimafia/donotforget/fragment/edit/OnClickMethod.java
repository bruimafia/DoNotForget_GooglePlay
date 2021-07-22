package ru.bruimafia.donotforget.fragment.edit;

import ru.bruimafia.donotforget.repository.local_store.Note;

public interface OnClickMethod {
    void onCreate(Note note);

    void onUpdate(Note note);

    void onDelete(long id);

    void onRecover(long id);

    void onChooseColor();

    void onChooseDate();

    void onChooseTime();
}
