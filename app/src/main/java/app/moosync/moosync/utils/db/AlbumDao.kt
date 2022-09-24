package app.moosync.moosync.utils.db

import androidx.room.*

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(album: AlbumEntity)

    @Transaction
    @Query("SELECT * FROM albums")
    fun getAllAlbums(): List<AlbumEntity>
}
