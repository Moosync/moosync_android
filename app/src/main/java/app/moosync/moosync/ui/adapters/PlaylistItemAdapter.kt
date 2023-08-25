package app.moosync.moosync.ui.adapters

import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import app.moosync.moosync.R
import app.moosync.moosync.databinding.PlaylistListItemBinding
import app.moosync.moosync.utils.models.Playlist

class PlaylistItemAdapter(private val onClick: (playlist: Playlist) -> Unit) : BaseListAdapter<PlaylistListItemBinding, Playlist>(SongDiffCallback) {

    override val layoutId: Int
        get() = R.layout.playlist_list_item

    object SongDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun bind(binding: PlaylistListItemBinding, item: Playlist) {
        binding.title.text = item.name
        binding.coverImage.setImageDrawable(AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_playlists))

//        Glide
//            .with(binding.root.context)
//            .load(AudioCover(item))
//            .placeholder(R.drawable.songs)
//            .transform(CenterCrop(), RoundedCorners(16))
//            .signature(MediaStoreSignature("", item.modified, 0))
//            .into(binding.coverImage)

        binding.root.setOnClickListener {
            onClick(item)
        }

        binding.executePendingBindings()
    }
}