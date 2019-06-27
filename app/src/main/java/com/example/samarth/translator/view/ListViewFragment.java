package com.example.samarth.translator.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.samarth.translator.AppPreference;
import com.example.samarth.translator.adapter.CustomAdapter;
import com.example.samarth.translator.R;
import com.example.samarth.translator.storage.Word;
import com.example.samarth.translator.storage.DataBaseHelper;

import java.util.ArrayList;



public class ListViewFragment extends Fragment {

    public static final String ARGS_NAME = "nameOfDB";
    private ActionBar actionBar;
    private CustomAdapter adapter;
    private String nameOfDB;
    private View rootView;
    private AppPreference appPreference;


    public static ListViewFragment newInstance(String nameOfDB) {
        ListViewFragment listViewFragment = new ListViewFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_NAME, nameOfDB);
        listViewFragment.setArguments(args);
        return listViewFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nameOfDB = getArguments().getString(ARGS_NAME);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        appPreference = AppPreference.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.listview_fragment, container, false);

        changeToListView();
        setHintToSearch();
        return rootView;
    }

    public void setHintToSearch() {
        EditText search = (EditText) rootView.findViewById(R.id.search);
        if (nameOfDB.equals(DataBaseHelper.HISTORY_DB)) {
            search.setHint(R.string.history_search_hint);
        } else {
            search.setHint(R.string.favourite_search_hint);
        }
    }

    public void setCustomActionBar() {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.custom_action_bar);

        if (nameOfDB.equals(DataBaseHelper.HISTORY_DB)) {
            actionBar.setTitle(R.string.text_history);
        } else {
            actionBar.setTitle(R.string.text_favourites);
        }
    }

    public void changeToListView() {
        final DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext(), nameOfDB);
        ArrayList<Word> arrayList = dataBaseHelper.getAllWords();
        adapter = new CustomAdapter(getContext(), R.layout.list_item, arrayList);

        if (arrayList.isEmpty()) {
            TextView noWordsText = (TextView) rootView.findViewById(R.id.no_words_in_listview);
            EditText search = (EditText) rootView.findViewById(R.id.search);
            noWordsText.setVisibility(View.VISIBLE);
            search.setVisibility(View.INVISIBLE);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
            if (nameOfDB.equals(DataBaseHelper.HISTORY_DB)) {
                noWordsText.setText(R.string.no_words_in_history);
                actionBar.setTitle(R.string.text_history);
            } else {
                noWordsText.setText(R.string.no_words_in_favourites);
                actionBar.setTitle(R.string.text_favourites);
            }
        } else {
            setCustomActionBar();


            ListView listView = (ListView) rootView.findViewById(R.id.listView);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView text = (TextView) view.findViewById(R.id.text);
                    TextView translation = (TextView) view.findViewById(R.id.translation);
                    TextView textView = (TextView) view.findViewById(R.id.languages);
                    String[] langs = String.valueOf(textView.getText()).split("-");

                    DataBaseHelper dbhelper = new DataBaseHelper(view.getContext(), nameOfDB);
                    int[] languages = dbhelper.getLanguages(String.valueOf(text.getText()), langs[0],
                            langs[1]);
                    dbhelper.close();

                    dbhelper = new DataBaseHelper(view.getContext(), DataBaseHelper.FAVOURITES_DB);
                    appPreference.setSelectionOne(languages[0]);
                    appPreference.setSelectionTwo(languages[1]);
                    appPreference.setTextToTranslate(text.getText().toString());
                    appPreference.setTranslatedText(translation.getText().toString());
                    if(dbhelper.isInDataBase(new Word(text.getText().toString(),
                            translation.getText().toString(), languages[0], languages[1]))){
                        appPreference.setIsFavourite(true);
                    } else{
                        appPreference.setIsFavourite(false);
                    }
                    dbhelper.close();

                    ((MainActivity) getActivity()).changeToMainView();
                }
            });

            EditText search = (EditText) rootView.findViewById(R.id.search);
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    adapter.getFilter().filter(s.toString());
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setTitle(R.string.app_name);
        super.onDestroy();
    }
}
