package com.sippy.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val species: String,
    val growthPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dayKey: String,
    val amountMl: Int,
    val timestamp: Long,
    val friendId: Long
)

@Entity(tableName = "day_stats")
data class DayStatsEntity(
    @PrimaryKey val dayKey: String,
    val totalMl: Int,
    val targetHit: Boolean
)

@Dao
interface SippyDao {
    @Query("SELECT * FROM friends ORDER BY sortOrder ASC, id ASC")
    fun observeFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun friend(id: Long): FriendEntity?

    @Query("SELECT COUNT(*) FROM friends")
    suspend fun friendCount(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM friends")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteFriend(id: Long)

    @Query("SELECT * FROM drinks WHERE dayKey = :day ORDER BY timestamp DESC, id DESC")
    fun drinksForDay(day: String): Flow<List<DrinkEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drinks WHERE dayKey = :day")
    fun totalForDay(day: String): Flow<Int>

    @Query("SELECT * FROM drinks WHERE dayKey = :day ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun lastDrink(day: String): DrinkEntity?

    @Insert
    suspend fun insertDrink(drink: DrinkEntity): Long

    @Query("DELETE FROM drinks WHERE id = :id")
    suspend fun deleteDrink(id: Long)

    @Query("SELECT * FROM day_stats WHERE dayKey = :day")
    suspend fun dayStats(day: String): DayStatsEntity?

    @Query("SELECT * FROM day_stats ORDER BY dayKey DESC LIMIT :limit")
    fun recentStats(limit: Int): Flow<List<DayStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayStats(stats: DayStatsEntity)

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drinks WHERE dayKey = :day")
    suspend fun sumForDay(day: String): Int

    @Query("DELETE FROM day_stats")
    suspend fun clearStats()

    @Query("DELETE FROM drinks")
    suspend fun clearDrinks()

    @Query("DELETE FROM friends")
    suspend fun clearFriends()
}

/** Version 1 is the stable baseline; exportSchema keeps migrations explicit in future releases. */
@Database(
    entities = [FriendEntity::class, DrinkEntity::class, DayStatsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SippyDatabase : RoomDatabase() {
    abstract fun dao(): SippyDao
}
