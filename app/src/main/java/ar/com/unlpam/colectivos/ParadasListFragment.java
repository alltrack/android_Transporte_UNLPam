package ar.com.unlpam.colectivos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ParadasListFragment extends Fragment {
    private static Bundle argument;
    ArrayList<Parada> CACHE_PARADAS = new ArrayList<Parada>();
    private SimpleDateFormat sdfEs = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat sdfHs = new SimpleDateFormat("HH:mm:ss");


    public static ParadasListFragment newInstance(Bundle args){
        ParadasListFragment f = new ParadasListFragment();
        if(args != null){
            f.setArguments(args);
            argument = args;
        }
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        Bundle bundle = argument;

        try {
            JSONArray paradas = new JSONArray(bundle.getString("data"));

            for(int i=0; i<paradas.length(); i++){

                JSONObject each = null;

                each = paradas.getJSONObject(i);
                CACHE_PARADAS.add(new Parada(each.getInt("id"), each.getDouble("la"), each.getDouble("lo"), each.getString("de"), each.getString("di"), each.getJSONArray("hs"),each.toString()));
            }
        } catch (JSONException e) {
                e.printStackTrace();
        }

        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext(), CACHE_PARADAS);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public TextView denominacion;
        public TextView direccion;
        public TextView Lu;
        public TextView Ma;
        public TextView Mi;
        public TextView Ju;
        public TextView Vi;
        public TextView Sa;
        public TextView Do;


        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_list_parada, parent, false));
            img = (ImageView) itemView.findViewById(R.id.item_parada_img);
            denominacion = (TextView) itemView.findViewById(R.id.item_parada_denominacion);
            direccion = (TextView) itemView.findViewById(R.id.item_parada_direccion);
            itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Intent paradaShowActivity = new Intent(arg0.getContext(), ParadaShowActivity.class);
                        paradaShowActivity.putExtra("parada",CACHE_PARADAS.get(getPosition()).getJSONDefinition());
                        startActivity(paradaShowActivity);
                    }
                }
            );

            Lu = (TextView) itemView.findViewById(R.id.item_parada_Lu);
            Ma = (TextView) itemView.findViewById(R.id.item_parada_Ma);
            Mi = (TextView) itemView.findViewById(R.id.item_parada_Mi);
            Ju = (TextView) itemView.findViewById(R.id.item_parada_Ju);
            Vi = (TextView) itemView.findViewById(R.id.item_parada_Vi);
            Sa = (TextView) itemView.findViewById(R.id.item_parada_Sa);
            Do = (TextView) itemView.findViewById(R.id.item_parada_Do);
        }

      }

    /**
     * Adapter to display recycler view.
     */
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder>{
        private ArrayList<Parada> paradas = new ArrayList<>();
        final private Context ctx;

        public ContentAdapter(Context context, ArrayList<Parada> paradas) {

            this.ctx = context;
            this.paradas = paradas;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final SimpleDateFormat sdfEs = new SimpleDateFormat("dd-MM-yyyy");
            final SimpleDateFormat sdfHs = new SimpleDateFormat("HH:mm:ss");

            holder.denominacion.setText(paradas.get(position).getDenominacion().toUpperCase());
            holder.direccion.setText(paradas.get(position).getDireccion());
            holder.img.setImageResource(R.drawable.ic_bus_stop);


            holder.Lu.setTextColor(ctx.getResources().getColor(R.color.Gray));
            holder.Ma.setTextColor(ctx.getResources().getColor(R.color.Gray));
            holder.Mi.setTextColor(ctx.getResources().getColor(R.color.Gray));
            holder.Ju.setTextColor(ctx.getResources().getColor(R.color.Gray));
            holder.Vi.setTextColor(ctx.getResources().getColor(R.color.Gray));
            holder.Sa.setTextColor(ctx.getResources().getColor(R.color.Gray));
            holder.Do.setTextColor(ctx.getResources().getColor(R.color.Gray));

            for(Horario h:paradas.get(position).getHorarios()){
                if(h.is_lunes) holder.Lu.setTextColor(ctx.getResources().getColor(R.color.colorAccent));
                if(h.is_martes) holder.Ma.setTextColor(ctx.getResources().getColor(R.color.colorAccent));
                if(h.is_miercoles) holder.Mi.setTextColor(ctx.getResources().getColor(R.color.colorAccent));
                if(h.is_jueves) holder.Ju.setTextColor(ctx.getResources().getColor(R.color.colorAccent));
                if(h.is_viernes) holder.Vi.setTextColor(ctx.getResources().getColor(R.color.colorAccent));
                if(h.is_sabado) holder.Sa.setTextColor(ctx.getResources().getColor(R.color.colorAccent));
                if(h.is_domingo) holder.Do.setTextColor(ctx.getResources().getColor(R.color.colorAccent));
            }

        }

        @Override
        public int getItemCount() {
            return paradas.size();
        }

    }
}
