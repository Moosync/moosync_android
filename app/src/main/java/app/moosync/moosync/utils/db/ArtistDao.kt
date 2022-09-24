package app.moosync.moosync.utils.db

import androidx.room.*

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artist: ArtistEntity)

    @Transaction
    @Query("SELECT * FROM artists")
    fun getAllArtists(): List<ArtistEntity>
}
