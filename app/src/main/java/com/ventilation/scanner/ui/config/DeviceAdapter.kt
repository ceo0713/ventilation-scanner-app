package com.ventilation.scanner.ui.config

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ventilation.scanner.R
import com.ventilation.scanner.data.OpeningType
import com.ventilation.scanner.data.VentilationOpening

class DeviceAdapter(
    private val onEdit: (VentilationOpening, Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    private val items = mutableListOf<VentilationOpening>()

    fun submitList(newItems: List<VentilationOpening>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.iconView.setImageResource(getIconForType(item.type))
        holder.cardView.setCardBackgroundColor(
            holder.itemView.context.getColor(getColorForType(item.type))
        )

        holder.nameText.text = item.name.ifEmpty { getKoreanName(item.type) }

        val detail = buildString {
            append("${formatFloat(item.width)}m × ${formatFloat(item.height)}m")
            if (item.type.hasCMH && item.cmh > 0) {
                append(" | ${item.cmh.toInt()}CMH")
            }
            if (item.velocity > 0 && !item.type.isDevice) {
                append(" | ${formatFloat(item.velocity)}m/s")
            }
        }
        holder.detailText.text = detail

        holder.editButton.setOnClickListener {
            onEdit(item, position)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.icon_view)
        val nameText: TextView = view.findViewById(R.id.name_text)
        val detailText: TextView = view.findViewById(R.id.detail_text)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
        val cardView: MaterialCardView = view as MaterialCardView
    }

    companion object {
        fun getIconForType(type: OpeningType): Int {
            return when (type) {
                OpeningType.DOOR -> android.R.drawable.ic_menu_directions
                OpeningType.WINDOW -> android.R.drawable.ic_menu_gallery
                OpeningType.VENT -> android.R.drawable.ic_menu_compass
                OpeningType.AC_UNIT -> android.R.drawable.ic_menu_manage
                OpeningType.VENTILATOR -> android.R.drawable.ic_menu_rotate
                OpeningType.AIR_PURIFIER -> android.R.drawable.ic_menu_view
                OpeningType.AIR_STERILIZER -> android.R.drawable.ic_menu_add
            }
        }

        fun getColorForType(type: OpeningType): Int {
            return when (type) {
                OpeningType.DOOR -> android.R.color.holo_green_light
                OpeningType.WINDOW -> android.R.color.holo_blue_light
                OpeningType.VENT -> android.R.color.holo_orange_light
                OpeningType.AC_UNIT -> android.R.color.holo_blue_bright
                OpeningType.VENTILATOR -> android.R.color.holo_purple
                OpeningType.AIR_PURIFIER -> android.R.color.holo_green_dark
                OpeningType.AIR_STERILIZER -> android.R.color.holo_red_light
            }
        }

        fun getKoreanName(type: OpeningType): String {
            return when (type) {
                OpeningType.DOOR -> "출입문"
                OpeningType.WINDOW -> "창문"
                OpeningType.VENT -> "환기구"
                OpeningType.AC_UNIT -> "에어컨"
                OpeningType.VENTILATOR -> "환기장치"
                OpeningType.AIR_PURIFIER -> "공기청정기"
                OpeningType.AIR_STERILIZER -> "공기살균기"
            }
        }

        private fun formatFloat(value: Float): String {
            return if (value == value.toInt().toFloat()) {
                value.toInt().toString()
            } else {
                "%.1f".format(value)
            }
        }
    }
}
