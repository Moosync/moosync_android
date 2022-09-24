package app.moosync.moosync.utils.db

import androidx.room.*

@Dao
interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(genre: GenreEntity)

    @Transaction
    @Query("SELECT * FROM genres")
    fun getAllGenres(): List<GenreEntity>
}
