package app.moosync.moosync.utils.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SongAndArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(song: SongAndArtistEntity)
}

@Dao
interface SongAndGenreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(song: SongAndGenreEntity)
}

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(song: SongEntity)

    @Transaction
    @Query("SELECT * FROM all_songs")
    fun getAllSongs(): LiveData<List<RoomSongItem>>

    @Query("SELECT * FROM all_songs WHERE song_id = :id")
    fun getById(id: Long): RoomSongItem
}
