package ar.com.unlpam.colectivos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParadasListFragment extends Fragment {

    private final ArrayList<Parada> cacheParadas = new ArrayList<>();

    // ⚠️ NUEVO: Factory method sin static Bundle
    public static ParadasListFragment newInstance(Bundle args) {
        ParadasListFragment fragment = new ParadasListFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false
        );

        loadParadasData();
        setupRecyclerView(recyclerView);

        return recyclerView;
    }

    private void loadParadasData() {
        Bundle args = getArguments();
        if (args == null) return;

        String dataJson = args.getString("data");
        if (dataJson == null) return;

        try {
            JSONArray paradas = new JSONArray(dataJson);

            for (int i = 0; i < paradas.length(); i++) {
                JSONObject each = paradas.getJSONObject(i);

                Parada parada = new Parada(
                        each.getInt("id"),
                        each.getDouble("la"),
                        each.getDouble("lo"),
                        each.getString("de"),
                        each.getString("di"),
                        each.getJSONArray("hs"),
                        each.toString()
                );

                cacheParadas.add(parada);
            }
        } catch (JSONException e) {
            Log.e("TRANSPORTE UNLPAM", "Error parseando JSON de paradas", e);
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        ContentAdapter adapter = new ContentAdapter(requireContext(), cacheParadas);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    // ===== ViewHolder =====

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public TextView denominacion;
        public TextView direccion;
        public TextView Lu, Ma, Mi, Ju, Vi, Sa, Do;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_parada, parent, false));

            img = itemView.findViewById(R.id.item_parada_img);
            denominacion = itemView.findViewById(R.id.item_parada_denominacion);
            direccion = itemView.findViewById(R.id.item_parada_direccion);

            Lu = itemView.findViewById(R.id.item_parada_Lu);
            Ma = itemView.findViewById(R.id.item_parada_Ma);
            Mi = itemView.findViewById(R.id.item_parada_Mi);
            Ju = itemView.findViewById(R.id.item_parada_Ju);
            Vi = itemView.findViewById(R.id.item_parada_Vi);
            Sa = itemView.findViewById(R.id.item_parada_Sa);
            Do = itemView.findViewById(R.id.item_parada_Do);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(v.getContext(), ParadaShowActivity.class);
                    intent.putExtra("parada", cacheParadas.get(position).getJSONDefinition());
                    startActivity(intent);
                }
            });
        }
    }

    // ===== Adapter =====

    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final ArrayList<Parada> paradas;
        private final Context context;

        public ContentAdapter(Context context, ArrayList<Parada> paradas) {
            this.context = context;
            this.paradas = paradas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Parada parada = paradas.get(position);

            holder.denominacion.setText(parada.getDenominacion().toUpperCase());
            holder.direccion.setText(parada.getDireccion());
            holder.img.setImageResource(R.drawable.ic_bus_stop);


            setDayColors(holder, parada);
        }

        private void setDayColors(ViewHolder holder, Parada parada) {

            int grayColor = ContextCompat.getColor(context, R.color.gray_500);
            int accentColor = ContextCompat.getColor(context, R.color.black);


            TextView[] dayViews = {
                    holder.Do, holder.Lu, holder.Ma, holder.Mi,
                    holder.Ju, holder.Vi, holder.Sa
            };

            // Inicializar todos en gris
            for (TextView dayView : dayViews) {
                dayView.setTextColor(grayColor);
            }


            for (Horario h : parada.getHorarios()) {
                boolean[] diasActivos = {
                        h.is_domingo,
                        h.is_lunes,
                        h.is_martes,
                        h.is_miercoles,
                        h.is_jueves,
                        h.is_viernes,
                        h.is_sabado
                };

                for (int i = 0; i < diasActivos.length; i++) {
                    if (diasActivos[i]) {
                        dayViews[i].setTextColor(accentColor);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return paradas.size();
        }
    }
}