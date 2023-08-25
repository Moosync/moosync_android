package app.moosync.moosync.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import app.moosync.moosync.R
import app.moosync.moosync.databinding.SongListItemBinding
import app.moosync.moosync.utils.helpers.ColorStyles
import app.moosync.moosync.utils.helpers.getColor
import app.moosync.moosync.utils.helpers.toArtistString
import app.moosync.moosync.utils.models.Song
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.MediaStoreSignature


class SongItemAdapter(private val onClick: (song: Song) -> Unit) : BaseListAdapter<SongListItemBinding, Song>(SongDiffCallback) {

    private val selectedItems = ArrayList<String>()
    private var selectMode = false

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

    private fun cardSelected(binding: SongListItemBinding) {
        binding.background.setCardBackgroundColor(ColorStyles.PRIMARY.getColor())
    }

    private fun cardDeselected(binding: SongListItemBinding) {
        binding.background.setCardBackgroundColor(ColorStyles.TRANSPARENT.getColor())
    }

    private fun toggleItemSelected(binding: SongListItemBinding, item: Song) {
        if (!selectedItems.contains(item._id)) {
            selectedItems.add(item._id)
            cardSelected(binding)
        } else {
            selectedItems.remove(item._id)
            cardDeselected(binding)
        }
    }

    override fun bind(binding: SongListItemBinding, item: Song) {
        binding.textView1.text = item.title
        binding.textView2.text = item.artist?.toArtistString() ?: ""

        if (selectedItems.contains(item._id)) {
            cardSelected(binding)
        } else {
            cardDeselected(binding)
        }

        Glide
            .with(binding.root.context)
            .load(item.coverImage)
            .placeholder(R.drawable.songs)
            .transform(CenterCrop(), RoundedCorners(16))
            .signature(MediaStoreSignature("", item.modified, 0))
            .into(binding.coverImage)

        binding.root.setOnClickListener {
            selectMode = selectedItems.size > 0

            if (!selectMode) onClick(item)
            else toggleItemSelected(binding, item)
        }

        binding.root.setOnLongClickListener {
            toggleItemSelected(binding, item)
            true
        }

        binding.executePendingBindings()
    }
}