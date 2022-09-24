package app.moosync.moosync.ui.adapters

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import app.moosync.moosync.R
import app.moosync.moosync.databinding.SongListItemBinding
import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.glide.GlideApp
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.models.Song
import com.bumptech.glide.signature.MediaStoreSignature

class SongItemAdapter(private val onClick: (song: Song) -> Unit) : BaseListAdapter<SongListItemBinding, Song>(SongDiffCallback) {

    override val layoutId: Int
        get() = R.layout.song_list_item

    object SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem._id == newItem._id
        }
    }

    override fun bind(binding: SongListItemBinding, item: Song) {
        binding.textView1.text = item.title
        binding.textView2.text = item.artist?.toArtistString() ?: ""

        GlideApp
            .with(binding.root.context)
            .load(AudioCover(item._id))
            .placeholder(R.drawable.songs)
            .signature(MediaStoreSignature("", item.modified, 0))
            .into(binding.coverImage)

        binding.root.setOnClickListener {
            onClick(item)
        }

        binding.executePendingBindings()
    }
}