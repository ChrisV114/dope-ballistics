package za.co.dope.ballistics

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.db.DopeDatabase
import za.co.dope.ballistics.data.db.RifleEntity
import za.co.dope.ballistics.data.db.ScopeTemplates
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ProfileDatabaseTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private var database: DopeDatabase? = null

    @After
    fun closeDatabase() {
        database?.close()
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun roomCrudArchivesReferencedProfilesInsteadOfDeleting() =
        runBlocking {
            database =
                Room
                    .inMemoryDatabaseBuilder(context, DopeDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            val repository = ProfileRepository(requireNotNull(database))
            val rifle = sampleRifle()

            repository.saveRifle(rifle)
            assertEquals(rifle, requireNotNull(database).profileDao().rifle(rifle.id))

            repository.saveRifle(rifle.copy(profileName = "Updated rifle", revision = 2))
            assertEquals("Updated rifle", requireNotNull(database).profileDao().rifle(rifle.id)?.profileName)

            assertTrue(repository.archiveRifle(rifle.id, 2L))
            assertTrue(repository.observeRifles().first().isEmpty())
            assertTrue(repository.deleteRifleIfUnreferenced(rifle.id))
        }

    @Test
    fun migrationOneToTwoCreatesValidatedSchemaAndBuiltInTemplates() {
        createVersionOneDatabase()
        database =
            Room
                .databaseBuilder(context, DopeDatabase::class.java, TEST_DATABASE)
                .addMigrations(DopeDatabase.MIGRATION_1_2)
                .allowMainThreadQueries()
                .build()

        val writable = requireNotNull(database).openHelper.writableDatabase
        assertTrue(writable.isOpen)
        val family =
            requireNotNull(
                runBlocking { requireNotNull(database).profileDao().scopeFamily(ScopeTemplates.DNT_FAMILY_ID) },
            )
        assertNotNull(family)
        assertTrue(family.builtInImmutable)
        val bdc =
            requireNotNull(
                runBlocking {
                    requireNotNull(database).profileDao().scopeVariant(ScopeTemplates.ARKEN_BDC_VARIANT_ID)
                },
            )
        assertEquals("BDC", bdc.reticleSystem)
        assertEquals("REQUIRES_USER_VERIFICATION", bdc.verificationStatus)
    }

    private fun createVersionOneDatabase() {
        context.deleteDatabase(TEST_DATABASE)
        val configuration =
            SupportSQLiteOpenHelper.Configuration
                .builder(context)
                .name(TEST_DATABASE)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(1) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL("CREATE TABLE legacy_profile_metadata (id INTEGER NOT NULL PRIMARY KEY)")
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) = Unit
                    },
                ).build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        helper.writableDatabase.close()
        helper.close()
    }

    private fun sampleRifle() =
        RifleEntity(
            id = "rifle-test",
            profileName = "Field rifle",
            manufacturer = "Example",
            model = "Model",
            calibreLabel = ".308 Winchester",
            barrelLengthMetres = 0.508,
            twistRateMetres = 0.254,
            twistDirection = "RIGHT",
            createdAtEpochMillis = 1L,
            modifiedAtEpochMillis = 1L,
        )

    private companion object {
        const val TEST_DATABASE = "dope-migration-test.db"
    }
}
