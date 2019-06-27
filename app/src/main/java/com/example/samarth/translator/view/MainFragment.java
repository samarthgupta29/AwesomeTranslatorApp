package com.example.samarth.translator.view;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samarth.translator.AppPreference;
import com.example.samarth.translator.network.APIHelper;
import com.example.samarth.translator.network.NetworkConstants;
import com.example.samarth.translator.network.RetrofitClient;
import com.example.samarth.translator.storage.Languages;
import com.example.samarth.translator.R;
import com.example.samarth.translator.network.TranslatedText;
import com.example.samarth.translator.storage.Word;
import com.example.samarth.translator.storage.DataBaseHelper;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Action1;


public class MainFragment extends Fragment {

    private View rootView;
    private Spinner spinner1;
    private Spinner spinner2;
    private EditText textToTranslate;
    private ImageButton addToFavourites;
    private ImageButton changeLanguages;
    private ImageButton btnSpeak;
    private ImageButton btnClip;
    private TextView translatedText;
    private boolean isFavourite;
    private boolean noTranslate;
    private AppPreference appPreference;


    private final int REQ_CODE_SPEECH_INPUT = 100;

    /**
     * Initialize widget elements and create view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return created view of fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_fragment, container, false);
        spinner1 = (Spinner) rootView.findViewById(R.id.languages1);
        spinner2 = (Spinner) rootView.findViewById(R.id.languages2);
        textToTranslate = (EditText) rootView.findViewById(R.id.textToTranslate);
        textToTranslate.setMovementMethod(new ScrollingMovementMethod());
        textToTranslate.setVerticalScrollBarEnabled(true);
        changeLanguages = (ImageButton) rootView.findViewById(R.id.changeLanguages);
        addToFavourites = (ImageButton) rootView.findViewById(R.id.addToFavourites1);
        btnSpeak = (ImageButton) rootView.findViewById(R.id.btnSpeak);
        btnClip = (ImageButton) rootView.findViewById(R.id.btnClip);
        translatedText = (TextView) rootView.findViewById(R.id.translatedText);
        translatedText.setMovementMethod(new ScrollingMovementMethod());
        translatedText.setVerticalScrollBarEnabled(true);
        appPreference = AppPreference.getInstance(getContext());
        setArgs();
        return rootView;
    }

    /**
     * Add listeners and set data.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setSpinners();
        textChangedListener();
        addButtonListener();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        appPreference.setSelectionOne(spinner1.getSelectedItemPosition());
        appPreference.setSelectionTwo(spinner2.getSelectedItemPosition());
        appPreference.setTextToTranslate(textToTranslate.getText().toString());
        appPreference.setTranslatedText(translatedText.getText().toString());
        appPreference.setIsFavourite(isFavourite);
        super.onDestroyView();
    }

    public void addToHistory() {
        String text = String.valueOf(textToTranslate.getText()).trim();
        if (!text.equals("")) {
            DataBaseHelper dataBaseHelper = new DataBaseHelper(rootView.getContext(), DataBaseHelper.HISTORY_DB);
            dataBaseHelper.insertWord(new Word(textToTranslate.getText().toString().trim(),
                    translatedText.getText().toString(), spinner1.getSelectedItemPosition(),
                    spinner2.getSelectedItemPosition()));
            dataBaseHelper.close();
        }
    }

    public void checkIfInFavourites() {
        String text = String.valueOf(textToTranslate.getText());
        if (!text.equals("")) {
            addToFavourites.setVisibility(View.VISIBLE);


            DataBaseHelper dataBaseHelper = new DataBaseHelper(rootView.getContext(), DataBaseHelper.FAVOURITES_DB);
            if (dataBaseHelper.isInDataBase(new Word(text, translatedText.getText().toString(),
                    spinner1.getSelectedItemPosition(), spinner2.getSelectedItemPosition()))) {
                addToFavourites.setImageResource(R.drawable.selected_favourites_icon);
                isFavourite = true;
            } else {
                addToFavourites.setImageResource(R.drawable.default_favourites_icon);
                isFavourite = false;
            }
            dataBaseHelper.close();
        } else {
            isFavourite = false;
            addToFavourites.setVisibility(View.INVISIBLE);

            translatedText.setText("");
        }
    }

    public void setArgs() {
        String text = appPreference.getTextToTranslate();
        String translation = appPreference.getTranslatedText();
        int selection1 = appPreference.getSelectionOne();
        int selection2 = appPreference.getSelectionTwo();
        isFavourite = appPreference.getIsFavourite();
        if (!text.equals("")) {
            noTranslate = true;
            textToTranslate.setText(text);
            spinner1.setSelection(selection1);
            spinner2.setSelection(selection2);
            translatedText.setText(translation);
            addToFavourites.setVisibility(View.VISIBLE);

            if (isFavourite) {
                addToFavourites.setImageResource(R.drawable.selected_favourites_icon);
            } else {
                addToFavourites.setImageResource(R.drawable.default_favourites_icon);
            }
        }
    }


    public void addButtonListener() {

        addToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBaseHelper dataBaseHelper = new DataBaseHelper(v.getContext(),
                        DataBaseHelper.FAVOURITES_DB);
                String text = textToTranslate.getText().toString().trim();
                String translation = translatedText.getText().toString();
                int source = spinner1.getSelectedItemPosition();
                int target = spinner2.getSelectedItemPosition();
                Word item = new Word(text, translation, source, target);
                if (dataBaseHelper.isInDataBase(item)) {
                    dataBaseHelper.deleteWord(item);
                    addToFavourites.setImageResource(R.drawable.default_favourites_icon);
                    isFavourite = false;
                } else {
                    isFavourite = true;
                    dataBaseHelper.insertWord(item);
                    addToFavourites.setImageResource(R.drawable.selected_favourites_icon);
                }
                dataBaseHelper.close();
            }
        });

        changeLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sourceLng = spinner1.getSelectedItemPosition();
                int targetLng = spinner2.getSelectedItemPosition();

                spinner1.setSelection(targetLng);
                spinner2.setSelection(sourceLng);

                translate(textToTranslate.getText().toString().trim());
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });


        btnClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (translatedText.getText().toString().isEmpty()) {
                    Toast.makeText(rootView.getContext(), "Nothing to copy", Toast.LENGTH_SHORT).show();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Translated Text Copied", translatedText.getText().toString());
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(rootView.getContext(), "Translated Text copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textToTranslate.setText(result.get(0));
                }
                break;
            }

        }
    }

    public void setSpinners() {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        if (Locale.getDefault().getLanguage().equals("en")) {
            Collections.addAll(categories, Languages.getLangsEN());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner1.setAdapter(dataAdapter);
        spinner2.setAdapter(dataAdapter);
        spinner2.setSelection(1);
    }

    public void textChangedListener() {

        // Translate the text after 500 milliseconds when user ends to typing
        RxTextView.textChanges(textToTranslate).
                filter(charSequence -> charSequence.length() > 0).
                debounce(500, TimeUnit.MILLISECONDS).
                subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        translate(charSequence.toString().trim());
                    }
                });

        RxTextView.textChanges(textToTranslate).
                filter(charSequence -> charSequence.length() == 0).
                subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkIfInFavourites();
                            }
                        });
                    }
                });
    }

    private void translate(String text) {
        if (noTranslate) {
            noTranslate = false;
            return;
        }

        String language1 = String.valueOf(spinner1.getSelectedItem());
        String language2 = String.valueOf(spinner2.getSelectedItem());

        APIHelper apiHelper = RetrofitClient.getClient().create(APIHelper.class);
        Call<TranslatedText> call = apiHelper.getTranslation(NetworkConstants.API_KEY, text,
                langCode(language1) + "-" + langCode(language2));

        call.enqueue(new Callback<TranslatedText>() {
            @Override
            public void onResponse(Call<TranslatedText> call, final Response<TranslatedText> response) {
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            translatedText.setText(response.body().getText().get(0));
                            checkIfInFavourites();
                            addToHistory();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<TranslatedText> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public String langCode(String selectedLang) {
        String code = null;

        if (Locale.getDefault().getLanguage().equals("en")) {
            for (int i = 0; i < Languages.getLangsEN().length; i++) {
                if (selectedLang.equals(Languages.getLangsEN()[i])) {
                    code = Languages.getLangCodeEN(i);
                }
            }
        }
        return code;
    }
}
