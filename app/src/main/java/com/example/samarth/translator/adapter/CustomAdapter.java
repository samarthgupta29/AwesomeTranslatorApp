package com.example.samarth.translator.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.samarth.translator.R;
import com.example.samarth.translator.storage.DataBaseHelper;
import com.example.samarth.translator.storage.Word;

import java.util.ArrayList;


public class CustomAdapter extends ArrayAdapter<Word> {

    private ArrayList<Word> originalItems;
    private ArrayList<Word> filteredItems;
    private LayoutInflater inflater;
    private MyFilter myFilter;
    private DataBaseHelper dbhelper;

    public CustomAdapter(@NonNull Context context, @LayoutRes int resource,
                         @NonNull ArrayList<Word> objects) {
        super(context, resource, objects);
        this.originalItems = objects;
        filteredItems = new ArrayList<Word>();
        filteredItems.addAll(this.originalItems);
        inflater = LayoutInflater.from(context);
        myFilter = new MyFilter(originalItems, results -> {
            filteredItems = results;
            notifyDataSetChanged();
        });
        dbhelper = new DataBaseHelper(context, DataBaseHelper.FAVOURITES_DB);
    }

    public int getCount() {
        return filteredItems.size();
    }

    public Word getItem(int position) {
        return filteredItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return myFilter;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final Word item = filteredItems.get(position);
        View v = null;

        if (convertView == null) {
            v = inflater.inflate(R.layout.list_item, parent, false);
        } else {
            v = convertView;
        }

        ImageButton button = (ImageButton) v.findViewById(R.id.addToFavourites2);
        TextView text = (TextView) v.findViewById(R.id.text);
        TextView translation = (TextView) v.findViewById(R.id.translation);
        final TextView language = (TextView) v.findViewById(R.id.languages);

        text.setText(item.getWord());
        translation.setText(item.getTranslation());
        language.setText(item.getSourceLanguage() + "-" +
                item.getTargetLanguage());

        if (dbhelper.isInDataBase(item)) {
            button.setImageResource(R.drawable.selected_favourites_icon);
        }
        dbhelper.close();

        button.setOnClickListener(
                v1 -> {
                    String text1 = item.getWord();
                    String translation1 = item.getTranslation();
                    int[] languages = {item.getSourcePosition(), item.getTargetPosition()};
                    ImageButton button1 = (ImageButton) v1.findViewById(R.id.addToFavourites2);
                    Word word = new Word(text1, translation1, languages[0], languages[1]);
                    if (dbhelper.isInDataBase(word)) {
                        dbhelper.setDeleted(word);
                        button1.setImageResource(R.drawable.default_favourites_icon);
                    } else {
                        dbhelper.insertWord(word);
                        button1.setImageResource(R.drawable.selected_favourites_icon);
                    }
                    dbhelper.close();

                });

        return v;
    }
}
