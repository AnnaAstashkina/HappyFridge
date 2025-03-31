package com.example.happyfridge.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.happyfridge.R;
import com.example.happyfridge.data.model.Product;
import com.example.happyfridge.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListAdapter extends BaseAdapter {

    private final Context context;
    private List<Product> productList;
    private final LayoutInflater inflater;

    public ProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = (productList != null) ? productList : new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    public void updateData(List<Product> newProducts) {
        this.productList.clear();
        if (newProducts != null) {
            this.productList.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Product getItem(int position) {
        if (position >= 0 && position < productList.size()) {
            return productList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        Product product = getItem(position);
        return (product != null) ? product.getId() : position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_product, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Product currentProduct = getItem(position);
        if (currentProduct != null) {
            holder.bind(currentProduct, context);
        }
        return convertView;
    }

    private static class ViewHolder {
        final ImageView itemImage;
        final TextView itemName;
        final TextView itemCategory;
        final TextView itemExpiry;
        final TextView itemQuantity;
        final TextView itemDaysRemaining;

        ViewHolder(View view) {
            itemImage = view.findViewById(R.id.item_image);
            itemName = view.findViewById(R.id.item_name);
            itemCategory = view.findViewById(R.id.item_category);
            itemExpiry = view.findViewById(R.id.item_expiry);
            itemQuantity = view.findViewById(R.id.item_quantity);
            itemDaysRemaining = view.findViewById(R.id.item_days_remaining);
        }

        void bind(Product product, Context context) {
            itemName.setText(product.getName());
            itemCategory.setText(product.getCategory());
            itemExpiry.setText(DateUtils.formatDate(product.getExpiryDate()));
            String quantityText = product.getQuantity() + " " + product.getUnit();
            itemQuantity.setText(quantityText);
            long daysRemaining = DateUtils.getDaysRemaining(product.getExpiryDate());
            itemDaysRemaining.setText(String.valueOf(daysRemaining));
            int colorRes;
            if (daysRemaining < 0) {
                colorRes = R.color.status_expired;
            } else if (daysRemaining <= 3) {
                colorRes = R.color.status_warning;
            } else {
                colorRes = R.color.status_ok;
            }
            itemDaysRemaining.setTextColor(ContextCompat.getColor(context, colorRes));
            int imageResId;
            switch (product.getCategory()) {
                case "Мясная продукция":
                    imageResId = R.drawable.meat;
                    break;
                case "Молочная продукция":
                    imageResId = R.drawable.dairy;
                    break;
                case "Овощи":
                    imageResId = R.drawable.vegetable;
                    break;
                case "Фрукты":
                    imageResId = R.drawable.fruit;
                    break;
                case "Морепродукты":
                    imageResId = R.drawable.seafood;
                    break;
                default:
                    imageResId = R.drawable.another;
                    break;
            }
            try {
                itemImage.setImageResource(imageResId);
            } catch (Exception e) {
                itemImage.setImageResource(R.drawable.another);
            }
        }
    }

    public interface OnItemInteractionListener {
        void onEditClick(Product product);

        void onDeleteClick(Product product);

        void onConsumeClick(Product product);
    }
}

