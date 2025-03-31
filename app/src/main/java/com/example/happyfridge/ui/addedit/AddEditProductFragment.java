package com.example.happyfridge.ui.addedit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.example.happyfridge.R;
import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Arrays;

public class AddEditProductFragment extends Fragment {
    public static final String ARG_PRODUCT_ID = "product_id";
    private static final int INVALID_PRODUCT_ID = -1;
    private AddEditProductViewModel viewModel;
    private TextInputEditText editTextProductName;
    private Spinner spinnerCategory;
    private TextInputEditText editTextQuantity;
    private Spinner spinnerUnit;
    private RadioGroup radioGroupExpiryType;
    private RadioButton radioExpiresOn;
    private LinearLayout layoutExpiryDate;
    private TextView textViewExpiryDate;
    private LinearLayout layoutExpiryDuration;
    private TextView textViewManufacturingDate;
    private TextInputEditText editTextShelfLife;
    private Spinner spinnerShelfLifeUnit;
    private Button buttonOk;

    private Date selectedExpiryDate = null;
    private Date selectedManufacturingDate = null;

    private int currentProductId = INVALID_PRODUCT_ID;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_product, container, false);
        bindViews(view);
        setupSpinners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AddEditProductViewModel.class);

        if (getArguments() != null) {
            currentProductId = getArguments().getInt(ARG_PRODUCT_ID, INVALID_PRODUCT_ID);
            isEditMode = (currentProductId != INVALID_PRODUCT_ID);
        }

        requireActivity().setTitle(isEditMode ? R.string.edit_product_title : R.string.add_product_title);

        setupListeners();
        observeViewModel();

        if (isEditMode && savedInstanceState == null) {
            viewModel.loadProduct(currentProductId);
        } else if (!isEditMode && savedInstanceState == null){
            viewModel.setExpiryInputType(AddEditProductViewModel.EXPIRY_TYPE_DATE);
            radioExpiresOn.setChecked(true);
        }
    }

    private void bindViews(View view) {
        editTextProductName = view.findViewById(R.id.edit_text_product_name);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        editTextQuantity = view.findViewById(R.id.edit_text_quantity);
        spinnerUnit = view.findViewById(R.id.spinner_unit);
        radioGroupExpiryType = view.findViewById(R.id.radio_group_expiry_type);
        radioExpiresOn = view.findViewById(R.id.radio_expires_on);
        layoutExpiryDate = view.findViewById(R.id.layout_expiry_date);
        textViewExpiryDate = view.findViewById(R.id.text_view_expiry_date);
        layoutExpiryDuration = view.findViewById(R.id.layout_expiry_duration);
        textViewManufacturingDate = view.findViewById(R.id.text_view_manufacturing_date);
        editTextShelfLife = view.findViewById(R.id.edit_text_shelf_life);
        spinnerShelfLifeUnit = view.findViewById(R.id.spinner_shelf_life_unit);
        buttonOk = view.findViewById(R.id.button_ok);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.product_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.product_units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        ArrayAdapter<CharSequence> shelfLifeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.shelf_life_units, android.R.layout.simple_spinner_item);
        shelfLifeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShelfLifeUnit.setAdapter(shelfLifeAdapter);
    }

    private void setupListeners() {
        radioGroupExpiryType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_expires_on) {
                viewModel.setExpiryInputType(AddEditProductViewModel.EXPIRY_TYPE_DATE);
            } else if (checkedId == R.id.radio_expires_in) {
                viewModel.setExpiryInputType(AddEditProductViewModel.EXPIRY_TYPE_DURATION);
            }
        });

        textViewExpiryDate.setOnClickListener(v -> showDatePickerDialog(true));
        textViewManufacturingDate.setOnClickListener(v -> showDatePickerDialog(false));

        buttonOk.setOnClickListener(v -> saveProduct());
    }

    private void observeViewModel() {
        viewModel.getExpiryInputType().observe(getViewLifecycleOwner(), type -> {
            if (type != null) {
                if (type == AddEditProductViewModel.EXPIRY_TYPE_DATE) {
                    layoutExpiryDate.setVisibility(View.VISIBLE);
                    layoutExpiryDuration.setVisibility(View.GONE);
                } else {
                    layoutExpiryDate.setVisibility(View.GONE);
                    layoutExpiryDuration.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getProductToEdit().observe(getViewLifecycleOwner(), product -> {
            if (product != null && isEditMode) {
                populateFields(product);
            }
        });

        viewModel.getSaveFinished().observe(getViewLifecycleOwner(), finished -> {
            if (finished != null && finished) {
                Toast.makeText(getContext(),
                        isEditMode ? "Продукт обновлен" : "Продукт добавлен",
                        Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
                viewModel.resetSaveState();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.resetSaveState();
            }
        });
    }

    private void populateFields(Product product) {
        editTextProductName.setText(product.getName());
        editTextQuantity.setText(String.valueOf(product.getQuantity()));

        String[] categories = getResources().getStringArray(R.array.product_categories);
        int categoryPosition = Arrays.asList(categories).indexOf(product.getCategory());
        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition);
        }

        String[] units = getResources().getStringArray(R.array.product_units);
        int unitPosition = Arrays.asList(units).indexOf(product.getUnit());
        if (unitPosition >= 0) {
            spinnerUnit.setSelection(unitPosition);
        }

        if (product.getExpiryDate() != null) {
            selectedExpiryDate = product.getExpiryDate();
            textViewExpiryDate.setText(DateUtils.formatDate(selectedExpiryDate));
            radioExpiresOn.setChecked(true);
            viewModel.setExpiryInputType(AddEditProductViewModel.EXPIRY_TYPE_DATE);
        } else {
            selectedExpiryDate = null;
            textViewExpiryDate.setText("");
        }

        selectedManufacturingDate = null;
        textViewManufacturingDate.setText("");
        editTextShelfLife.setText("");
        spinnerShelfLifeUnit.setSelection(0);
    }

    private void showDatePickerDialog(final boolean isExpiryDate) {
        Calendar calendar = Calendar.getInstance();
        Date initialDate = isExpiryDate ? selectedExpiryDate : selectedManufacturingDate;
        if (initialDate != null) {
            calendar.setTime(initialDate);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth);
                    Date date = DateUtils.getStartOfDay(selectedCalendar.getTime());

                    if (isExpiryDate) {
                        selectedExpiryDate = date;
                        textViewExpiryDate.setText(DateUtils.formatDate(date));
                    } else {
                        selectedManufacturingDate = date;
                        textViewManufacturingDate.setText(DateUtils.formatDate(date));
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveProduct() {
        String name = editTextProductName.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String quantityStr = editTextQuantity.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        String shelfLifeStr = editTextShelfLife.getText().toString().trim();
        boolean shelfLifeIsMonths = spinnerShelfLifeUnit.getSelectedItemPosition() == 1;

        viewModel.saveProduct(
                name, category, quantityStr, unit,
                selectedExpiryDate,
                selectedManufacturingDate, shelfLifeStr, shelfLifeIsMonths
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        editTextProductName = null;
        spinnerCategory = null;
        editTextQuantity = null;
        spinnerUnit = null;
        radioGroupExpiryType = null;
        radioExpiresOn = null;
        layoutExpiryDate = null;
        textViewExpiryDate = null;
        layoutExpiryDuration = null;
        textViewManufacturingDate = null;
        editTextShelfLife = null;
        spinnerShelfLifeUnit = null;
        buttonOk = null;
    }
}
