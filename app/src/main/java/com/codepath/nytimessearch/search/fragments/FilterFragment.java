package com.codepath.nytimessearch.search.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.search.models.Filter;

import java.util.HashMap;
import java.util.Map;

public class FilterFragment extends DialogFragment {

    private DatePicker beginDate;
    private Spinner sortOrder;
    private CheckBox deskValueArts;
    private CheckBox deskValueFashion;
    private CheckBox deskValueSports;
    private Button btnSave;

    public FilterFragment() {
        // Required empty public constructor
    }

    public static FilterFragment newInstance(Filter filter) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        if (filter != null) {
            args.putInt("beginYear", filter.getYear());
            args.putInt("beginMonth", filter.getMonth());
            args.putInt("beginDay", filter.getDay());
            args.putString("sortOrder", filter.getSortOrder());
            args.putInt("isArts", filter.getIsArts());
            args.putInt("isFashion", filter.getIsFashion());
            args.putInt("isSports", filter.getIsSports());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        beginDate = (DatePicker) view.findViewById(R.id.datePicker);
        sortOrder = (Spinner) view.findViewById(R.id.sortOder);
        deskValueArts = (CheckBox) view.findViewById(R.id.cbArts);
        deskValueFashion = (CheckBox) view.findViewById(R.id.cbFashion);
        deskValueSports = (CheckBox) view.findViewById(R.id.cbSports);
        setFilterIfPossible();
        btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Filter filter = getFilter();
                OnFragmentInteractionListener listener = (OnFragmentInteractionListener) getActivity();
                listener.onFragmentInteraction(filter);
                dismiss();
            }
        });
    }

    private Filter getFilter() {
        Map<String, Integer> params = new HashMap<>();
        params.put("year", beginDate.getYear());
        params.put("month", beginDate.getMonth());
        params.put("day", beginDate.getDayOfMonth());
        params.put("isArts", deskValueArts.isChecked()? 1: 0);
        params.put("isFashion", deskValueFashion.isChecked()? 1: 0);
        params.put("isSports", deskValueSports.isChecked()? 1: 0);

        return Filter.fromMap(params, sortOrder.getSelectedItem().toString());
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Filter filter);
    }

    private void setFilterIfPossible() {
        if (getArguments().getInt("beginYear", -1) != -1) {
            beginDate.updateDate(
                    getArguments().getInt("beginYear"),
                    getArguments().getInt("beginMonth"),
                    getArguments().getInt("beginDay")
            );
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    getActivity(), R.array.sort_order_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sortOrder.setAdapter(adapter);
            sortOrder.setSelection(adapter.getPosition(getArguments().getString("sortOrder")));
            deskValueArts.setChecked(getArguments().getInt("isArts") != 0);
            deskValueFashion.setChecked(getArguments().getInt("isFashion") != 0);
            deskValueSports.setChecked(getArguments().getInt("isSports") != 0);
        }
    }
}
