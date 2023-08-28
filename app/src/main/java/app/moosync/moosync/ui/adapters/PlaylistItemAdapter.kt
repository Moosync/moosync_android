package app.moosync.moosync.ui.adapters

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import app.moosync.moosync.R
import app.moosync.moosync.databinding.PlaylistListItemBinding
import app.moosync.moosync.utils.models.Playlist
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PlaylistItemAdapter(private val onClick: (playlist: Playlist) -> Unit) : BaseListAdapter<PlaylistListItemBinding, Playlist>(PlaylistDiffCallback) {

    override val layoutId: Int
        get() = R.layout.playlist_list_item

    object PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            Log.d("TAG", "areItemsTheSame: $oldItem, $newItem")
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun bind(binding: PlaylistListItemBinding, item: Playlist) {
        binding.title.text = item.name

        Glide
            .with(binding.root.context)
            .load(item.coverImage)
            .placeholder(R.drawable.ic_playlists)
            .transform(CenterCrop(), RoundedCorners(16))
            .into(binding.coverImage)


        binding.root.setOnClickListener {
            onClick(item)
        }

        binding.executePendingBindings()
    }
}