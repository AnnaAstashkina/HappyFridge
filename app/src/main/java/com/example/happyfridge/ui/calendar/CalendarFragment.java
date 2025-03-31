package com.example.happyfridge.ui.calendar;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.happyfridge.ui.SharedStatisticsViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.example.happyfridge.R;
import com.example.happyfridge.adapter.ProductListAdapter;
import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.ui.addedit.AddEditProductFragment;
import com.example.happyfridge.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarFragment extends Fragment implements ProductListAdapter.OnItemInteractionListener {

    private CalendarViewModel calendarViewModel;
    private SharedStatisticsViewModel sharedStatisticsViewModel;
    private CalendarView calendarView;
    private ListView listView;
    private ProductListAdapter adapter;
    private TextView headerTextView;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        listView = view.findViewById(R.id.list_view_calendar_products);
        headerTextView = view.findViewById(R.id.text_view_expiring_header);
        emptyView = view.findViewById(R.id.text_view_empty_calendar_list);

        adapter = new ProductListAdapter(requireContext(), new ArrayList<>());
        listView.setAdapter(adapter);

        listView.setEmptyView(emptyView);

        registerForContextMenu(listView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedStatisticsViewModel = new ViewModelProvider(requireActivity()).get(SharedStatisticsViewModel.class);
        calendarViewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
        calendarViewModel.getProductsForSelectedDate().observe(getViewLifecycleOwner(), products -> {
            adapter.updateData(products);
        });

        calendarViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                headerTextView.setText(getString(R.string.calendar_expiring_on, DateUtils.formatDateForCalendarTitle(date)));
                if (calendarView.getDate() != date.getTime()) {
                    calendarView.setDate(date.getTime());
                }
            }
        });

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            calendarViewModel.setSelectedDate(year, month, dayOfMonth);
        });

        Date initialDate = calendarViewModel.getSelectedDate().getValue();
        if (initialDate != null) {
            calendarView.setDate(initialDate.getTime());
            headerTextView.setText(getString(R.string.calendar_expiring_on, DateUtils.formatDateForCalendarTitle(initialDate)));
        } else {
            calendarView.setDate(System.currentTimeMillis());
            calendarViewModel.setSelectedDate(Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        }
    }


    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.product_context_menu, menu);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info != null) {
            Product selectedProduct = adapter.getItem(info.position);
            if (selectedProduct != null) {
                menu.setHeaderTitle(selectedProduct.getName());
            }
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);
        Product selectedProduct = adapter.getItem(info.position);
        if (selectedProduct == null) return super.onContextItemSelected(item);

        int itemId = item.getItemId();
        if (itemId == R.id.context_action_consume) {
            onConsumeClick(selectedProduct);
            return true;
        } else if (itemId == R.id.context_action_edit) {
            onEditClick(selectedProduct);
            return true;
        } else if (itemId == R.id.context_action_delete) {
            onDeleteClick(selectedProduct);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onEditClick(Product product) {
        Bundle args = new Bundle();
        args.putInt(AddEditProductFragment.ARG_PRODUCT_ID, product.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_addEditProductFragment, args);
    }

    @Override
    public void onDeleteClick(Product product) {
        sharedStatisticsViewModel.deleteProduct(product);
        Snackbar.make(requireView(), "Продукт \"" + product.getName() + "\" удален", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConsumeClick(Product product) {
        sharedStatisticsViewModel.consumeProduct(product);
        Snackbar.make(requireView(), "Продукт \"" + product.getName() + "\" потрачен", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listView = null;
        emptyView = null;
        calendarView = null;
        headerTextView = null;
    }
}
