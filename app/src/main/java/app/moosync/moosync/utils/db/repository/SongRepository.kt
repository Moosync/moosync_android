package app.moosync.moosync.utils.db.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Transaction
import app.moosync.moosync.utils.db.*

class SongRepository(mContext: Context) {
    private val database = SongDatabase(context = mContext)
    private val songAndArtistDao = database.songAndArtistDao()
    private val songAndGenreDao = database.songAndGenreDao()
    private val songDao = database.songDao()
    private val artistDao = database.artistDao()
    private val genreDao = database.genreDao()
    private val albumDao = database.albumDao()

    @Transaction
    fun insert(vararg roomSongItems: RoomSongItem) {
        for (roomSongItem in roomSongItems) {
            if (roomSongItem.albums != null) {
                albumDao.insert(roomSongItem.albums!!)
            }

            songDao.insert(roomSongItem.song)

            if (roomSongItem.artists != null) {
                roomSongItem.artists!!.forEach {
                    artistDao.insert(it)
                    songAndArtistDao.insert(SongAndArtistEntity(roomSongItem.song._id, it._id))
                }
            }

            if (roomSongItem.genres != null) {
                roomSongItem.genres!!.forEach {
                    genreDao.insert(it)
                    songAndGenreDao.insert(SongAndGenreEntity(roomSongItem.song._id, it._id))
                }
            }
        }
    }

    fun fetchAllSongs(): LiveData<List<RoomSongItem>> {
        return songDao.getAllSongs()
    }

    fun fetchSongById(id: Long): RoomSongItem {
        return songDao.getById(id)
    }
}