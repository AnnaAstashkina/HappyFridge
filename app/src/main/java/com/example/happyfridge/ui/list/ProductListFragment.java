package com.example.happyfridge.ui.list;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.happyfridge.ui.SharedStatisticsViewModel;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.example.happyfridge.R;
import com.example.happyfridge.adapter.ProductListAdapter;
import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.ui.addedit.AddEditProductFragment;

import java.util.ArrayList;

public class ProductListFragment extends Fragment implements ProductListAdapter.OnItemInteractionListener {
    private ProductListViewModel productListViewModel;
    private SharedStatisticsViewModel sharedStatisticsViewModel;
    private ListView listView;
    private ProductListAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        listView = view.findViewById(R.id.list_view_products);
        emptyView = view.findViewById(R.id.text_view_empty_list);
        adapter = new ProductListAdapter(requireContext(), new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);
        registerForContextMenu(listView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productListViewModel = new ViewModelProvider(this).get(ProductListViewModel.class);
        productListViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.updateData(products);
        });
        sharedStatisticsViewModel = new ViewModelProvider(requireActivity()).get(SharedStatisticsViewModel.class);
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
        if (info == null) {
            return super.onContextItemSelected(item);
        }
        Product selectedProduct = adapter.getItem(info.position);
        if (selectedProduct == null) {
            return super.onContextItemSelected(item);
        }
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
    }
}
