package app.moosync.moosync.utils.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.moosync.moosync.BuildConfig

@Database(
    version = 3,
    entities = [
        SongEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        GenreEntity::class,
        PlaylistEntity::class,
        SongAndGenreEntity::class,
        SongAndArtistEntity::class,
    ]
)
abstract class SongDatabase : RoomDatabase() {

    abstract fun songAndArtistDao(): SongAndArtistDao
    abstract fun songAndGenreDao(): SongAndGenreDao
    abstract fun songDao(): SongDao
    abstract fun artistDao(): ArtistDao
    abstract fun genreDao(): GenreDao
    abstract fun albumDao(): AlbumDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        private lateinit var INSTANCE: SongDatabase

        operator fun invoke(context: Context): SongDatabase {
            if (!::INSTANCE.isInitialized) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    SongDatabase::class.java, "song_database"
                ).allowMainThreadQueries()
                if (BuildConfig.DEBUG) {
                    builder.fallbackToDestructiveMigration()
                }

                INSTANCE = builder.build()
            }
            return INSTANCE
        }
    }
}