package com.example.samarth.translator.adapter;

import android.widget.Filter;

import com.example.samarth.translator.storage.Word;

import java.util.ArrayList;

class MyFilter extends Filter {

    private ArrayList<Word> originalItems;
    private FilterAdapterInteraction listener;


    public MyFilter(ArrayList<Word> originalItems, FilterAdapterInteraction listener) {
        this.originalItems = originalItems;
        this.listener = listener;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        final ArrayList<Word> filteredList = new ArrayList<Word>();

        if (constraint.equals("") || constraint.toString().trim().length() == 0) {
            results.values = originalItems;
        } else {
            String textToFilter = constraint.toString().toLowerCase();
            for (Word word : originalItems) {
                if (word.getWord().length() >= textToFilter.length() &&
                        word.getWord().toLowerCase().contains(textToFilter)) {
                    filteredList.add(word);
                }
            }
            results.values = filteredList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results.values != null) {
            listener.updateResults((ArrayList<Word>) results.values);
        }
    }

}

interface FilterAdapterInteraction {
    void updateResults(ArrayList<Word> results);
}
