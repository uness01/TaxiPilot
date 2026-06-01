package com.example.taxipilot.core.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.taxipilot.core.data.database.dao.ChargeDao;
import com.example.taxipilot.core.data.database.dao.ChargeDao_Impl;
import com.example.taxipilot.core.data.database.dao.ChauffeurDao;
import com.example.taxipilot.core.data.database.dao.ChauffeurDao_Impl;
import com.example.taxipilot.core.data.database.dao.ReservationDao;
import com.example.taxipilot.core.data.database.dao.ReservationDao_Impl;
import com.example.taxipilot.core.data.database.dao.TaxiDao;
import com.example.taxipilot.core.data.database.dao.TaxiDao_Impl;
import com.example.taxipilot.core.data.database.dao.TrajetDao;
import com.example.taxipilot.core.data.database.dao.TrajetDao_Impl;
import com.example.taxipilot.core.data.database.dao.TripDao;
import com.example.taxipilot.core.data.database.dao.TripDao_Impl;
import com.example.taxipilot.core.data.database.dao.UserDao;
import com.example.taxipilot.core.data.database.dao.UserDao_Impl;
import com.example.taxipilot.core.data.database.dao.VehicleDao;
import com.example.taxipilot.core.data.database.dao.VehicleDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TaxiPilotDatabase_Impl extends TaxiPilotDatabase {
  private volatile TaxiDao _taxiDao;

  private volatile ChauffeurDao _chauffeurDao;

  private volatile TrajetDao _trajetDao;

  private volatile ChargeDao _chargeDao;

  private volatile ReservationDao _reservationDao;

  private volatile UserDao _userDao;

  private volatile VehicleDao _vehicleDao;

  private volatile TripDao _tripDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `taxis` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `marque` TEXT NOT NULL, `modele` TEXT NOT NULL, `immatriculation` TEXT NOT NULL, `annee` INTEGER NOT NULL, `couleur` TEXT NOT NULL, `kilometrage` INTEGER NOT NULL, `statut` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `chauffeurs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nom` TEXT NOT NULL, `prenom` TEXT NOT NULL, `telephone` TEXT NOT NULL, `email` TEXT NOT NULL, `numeroPermis` TEXT NOT NULL, `dateEmbauche` INTEGER NOT NULL, `statut` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `trajets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taxiId` INTEGER, `chauffeurId` INTEGER, `reservationId` INTEGER, `adresseDepart` TEXT NOT NULL, `adresseArrivee` TEXT NOT NULL, `dateDebut` INTEGER, `dateFin` INTEGER, `distanceKm` REAL, `montant` REAL, `statut` TEXT NOT NULL, FOREIGN KEY(`taxiId`) REFERENCES `taxis`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`chauffeurId`) REFERENCES `chauffeurs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`reservationId`) REFERENCES `reservations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trajets_taxiId` ON `trajets` (`taxiId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trajets_chauffeurId` ON `trajets` (`chauffeurId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trajets_reservationId` ON `trajets` (`reservationId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `charges` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taxiId` INTEGER NOT NULL, `type` TEXT NOT NULL, `description` TEXT NOT NULL, `montant` REAL NOT NULL, `date` INTEGER NOT NULL, `kilometrageAuMoment` INTEGER, FOREIGN KEY(`taxiId`) REFERENCES `taxis`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_charges_taxiId` ON `charges` (`taxiId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reservations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientId` INTEGER, `clientNom` TEXT NOT NULL, `clientTelephone` TEXT NOT NULL, `adresseDepart` TEXT NOT NULL, `adresseArrivee` TEXT NOT NULL, `dateReservation` INTEGER NOT NULL, `datePickup` INTEGER NOT NULL, `taxiId` INTEGER, `chauffeurId` INTEGER, `statut` TEXT NOT NULL, `type` TEXT NOT NULL, `prixEstime` REAL, `notes` TEXT NOT NULL, FOREIGN KEY(`taxiId`) REFERENCES `taxis`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`chauffeurId`) REFERENCES `chauffeurs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reservations_taxiId` ON `reservations` (`taxiId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reservations_chauffeurId` ON `reservations` (`chauffeurId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `role` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vehicles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ownerId` INTEGER NOT NULL, `brand` TEXT NOT NULL, `model` TEXT NOT NULL, `licensePlate` TEXT NOT NULL, `isAvailable` INTEGER NOT NULL, FOREIGN KEY(`ownerId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_vehicles_ownerId` ON `vehicles` (`ownerId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `trips` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clientId` INTEGER NOT NULL, `driverId` INTEGER, `originAddress` TEXT NOT NULL, `destinationAddress` TEXT NOT NULL, `status` TEXT NOT NULL, `fare` REAL, `requestedAt` INTEGER NOT NULL, `completedAt` INTEGER, FOREIGN KEY(`clientId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`driverId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trips_clientId` ON `trips` (`clientId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trips_driverId` ON `trips` (`driverId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '29706fa8523804a7a29339f599002fc7')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `taxis`");
        db.execSQL("DROP TABLE IF EXISTS `chauffeurs`");
        db.execSQL("DROP TABLE IF EXISTS `trajets`");
        db.execSQL("DROP TABLE IF EXISTS `charges`");
        db.execSQL("DROP TABLE IF EXISTS `reservations`");
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `vehicles`");
        db.execSQL("DROP TABLE IF EXISTS `trips`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTaxis = new HashMap<String, TableInfo.Column>(8);
        _columnsTaxis.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaxis.put("marque", new TableInfo.Column("marque", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaxis.put("modele", new TableInfo.Column("modele", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaxis.put("immatriculation", new TableInfo.Column("immatriculation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaxis.put("annee", new TableInfo.Column("annee", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaxis.put("couleur", new TableInfo.Column("couleur", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaxis.put("kilometrage", new TableInfo.Column("kilometrage", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaxis.put("statut", new TableInfo.Column("statut", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTaxis = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTaxis = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTaxis = new TableInfo("taxis", _columnsTaxis, _foreignKeysTaxis, _indicesTaxis);
        final TableInfo _existingTaxis = TableInfo.read(db, "taxis");
        if (!_infoTaxis.equals(_existingTaxis)) {
          return new RoomOpenHelper.ValidationResult(false, "taxis(com.example.taxipilot.core.data.database.entity.TaxiEntity).\n"
                  + " Expected:\n" + _infoTaxis + "\n"
                  + " Found:\n" + _existingTaxis);
        }
        final HashMap<String, TableInfo.Column> _columnsChauffeurs = new HashMap<String, TableInfo.Column>(8);
        _columnsChauffeurs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChauffeurs.put("nom", new TableInfo.Column("nom", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChauffeurs.put("prenom", new TableInfo.Column("prenom", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChauffeurs.put("telephone", new TableInfo.Column("telephone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChauffeurs.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChauffeurs.put("numeroPermis", new TableInfo.Column("numeroPermis", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChauffeurs.put("dateEmbauche", new TableInfo.Column("dateEmbauche", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChauffeurs.put("statut", new TableInfo.Column("statut", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChauffeurs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChauffeurs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoChauffeurs = new TableInfo("chauffeurs", _columnsChauffeurs, _foreignKeysChauffeurs, _indicesChauffeurs);
        final TableInfo _existingChauffeurs = TableInfo.read(db, "chauffeurs");
        if (!_infoChauffeurs.equals(_existingChauffeurs)) {
          return new RoomOpenHelper.ValidationResult(false, "chauffeurs(com.example.taxipilot.core.data.database.entity.ChauffeurEntity).\n"
                  + " Expected:\n" + _infoChauffeurs + "\n"
                  + " Found:\n" + _existingChauffeurs);
        }
        final HashMap<String, TableInfo.Column> _columnsTrajets = new HashMap<String, TableInfo.Column>(11);
        _columnsTrajets.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("taxiId", new TableInfo.Column("taxiId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("chauffeurId", new TableInfo.Column("chauffeurId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("reservationId", new TableInfo.Column("reservationId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("adresseDepart", new TableInfo.Column("adresseDepart", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("adresseArrivee", new TableInfo.Column("adresseArrivee", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("dateDebut", new TableInfo.Column("dateDebut", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("dateFin", new TableInfo.Column("dateFin", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("distanceKm", new TableInfo.Column("distanceKm", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("montant", new TableInfo.Column("montant", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrajets.put("statut", new TableInfo.Column("statut", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrajets = new HashSet<TableInfo.ForeignKey>(3);
        _foreignKeysTrajets.add(new TableInfo.ForeignKey("taxis", "SET NULL", "NO ACTION", Arrays.asList("taxiId"), Arrays.asList("id")));
        _foreignKeysTrajets.add(new TableInfo.ForeignKey("chauffeurs", "SET NULL", "NO ACTION", Arrays.asList("chauffeurId"), Arrays.asList("id")));
        _foreignKeysTrajets.add(new TableInfo.ForeignKey("reservations", "SET NULL", "NO ACTION", Arrays.asList("reservationId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTrajets = new HashSet<TableInfo.Index>(3);
        _indicesTrajets.add(new TableInfo.Index("index_trajets_taxiId", false, Arrays.asList("taxiId"), Arrays.asList("ASC")));
        _indicesTrajets.add(new TableInfo.Index("index_trajets_chauffeurId", false, Arrays.asList("chauffeurId"), Arrays.asList("ASC")));
        _indicesTrajets.add(new TableInfo.Index("index_trajets_reservationId", false, Arrays.asList("reservationId"), Arrays.asList("ASC")));
        final TableInfo _infoTrajets = new TableInfo("trajets", _columnsTrajets, _foreignKeysTrajets, _indicesTrajets);
        final TableInfo _existingTrajets = TableInfo.read(db, "trajets");
        if (!_infoTrajets.equals(_existingTrajets)) {
          return new RoomOpenHelper.ValidationResult(false, "trajets(com.example.taxipilot.core.data.database.entity.TrajetEntity).\n"
                  + " Expected:\n" + _infoTrajets + "\n"
                  + " Found:\n" + _existingTrajets);
        }
        final HashMap<String, TableInfo.Column> _columnsCharges = new HashMap<String, TableInfo.Column>(7);
        _columnsCharges.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharges.put("taxiId", new TableInfo.Column("taxiId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharges.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharges.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharges.put("montant", new TableInfo.Column("montant", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharges.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharges.put("kilometrageAuMoment", new TableInfo.Column("kilometrageAuMoment", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCharges = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCharges.add(new TableInfo.ForeignKey("taxis", "CASCADE", "NO ACTION", Arrays.asList("taxiId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesCharges = new HashSet<TableInfo.Index>(1);
        _indicesCharges.add(new TableInfo.Index("index_charges_taxiId", false, Arrays.asList("taxiId"), Arrays.asList("ASC")));
        final TableInfo _infoCharges = new TableInfo("charges", _columnsCharges, _foreignKeysCharges, _indicesCharges);
        final TableInfo _existingCharges = TableInfo.read(db, "charges");
        if (!_infoCharges.equals(_existingCharges)) {
          return new RoomOpenHelper.ValidationResult(false, "charges(com.example.taxipilot.core.data.database.entity.ChargeEntity).\n"
                  + " Expected:\n" + _infoCharges + "\n"
                  + " Found:\n" + _existingCharges);
        }
        final HashMap<String, TableInfo.Column> _columnsReservations = new HashMap<String, TableInfo.Column>(14);
        _columnsReservations.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("clientId", new TableInfo.Column("clientId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("clientNom", new TableInfo.Column("clientNom", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("clientTelephone", new TableInfo.Column("clientTelephone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("adresseDepart", new TableInfo.Column("adresseDepart", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("adresseArrivee", new TableInfo.Column("adresseArrivee", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("dateReservation", new TableInfo.Column("dateReservation", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("datePickup", new TableInfo.Column("datePickup", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("taxiId", new TableInfo.Column("taxiId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("chauffeurId", new TableInfo.Column("chauffeurId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("statut", new TableInfo.Column("statut", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("prixEstime", new TableInfo.Column("prixEstime", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReservations.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReservations = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysReservations.add(new TableInfo.ForeignKey("taxis", "SET NULL", "NO ACTION", Arrays.asList("taxiId"), Arrays.asList("id")));
        _foreignKeysReservations.add(new TableInfo.ForeignKey("chauffeurs", "SET NULL", "NO ACTION", Arrays.asList("chauffeurId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesReservations = new HashSet<TableInfo.Index>(2);
        _indicesReservations.add(new TableInfo.Index("index_reservations_taxiId", false, Arrays.asList("taxiId"), Arrays.asList("ASC")));
        _indicesReservations.add(new TableInfo.Index("index_reservations_chauffeurId", false, Arrays.asList("chauffeurId"), Arrays.asList("ASC")));
        final TableInfo _infoReservations = new TableInfo("reservations", _columnsReservations, _foreignKeysReservations, _indicesReservations);
        final TableInfo _existingReservations = TableInfo.read(db, "reservations");
        if (!_infoReservations.equals(_existingReservations)) {
          return new RoomOpenHelper.ValidationResult(false, "reservations(com.example.taxipilot.core.data.database.entity.ReservationEntity).\n"
                  + " Expected:\n" + _infoReservations + "\n"
                  + " Found:\n" + _existingReservations);
        }
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(5);
        _columnsUsers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.example.taxipilot.core.data.database.entity.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsVehicles = new HashMap<String, TableInfo.Column>(6);
        _columnsVehicles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVehicles.put("ownerId", new TableInfo.Column("ownerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVehicles.put("brand", new TableInfo.Column("brand", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVehicles.put("model", new TableInfo.Column("model", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVehicles.put("licensePlate", new TableInfo.Column("licensePlate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVehicles.put("isAvailable", new TableInfo.Column("isAvailable", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVehicles = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysVehicles.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("ownerId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesVehicles = new HashSet<TableInfo.Index>(1);
        _indicesVehicles.add(new TableInfo.Index("index_vehicles_ownerId", false, Arrays.asList("ownerId"), Arrays.asList("ASC")));
        final TableInfo _infoVehicles = new TableInfo("vehicles", _columnsVehicles, _foreignKeysVehicles, _indicesVehicles);
        final TableInfo _existingVehicles = TableInfo.read(db, "vehicles");
        if (!_infoVehicles.equals(_existingVehicles)) {
          return new RoomOpenHelper.ValidationResult(false, "vehicles(com.example.taxipilot.core.data.database.entity.VehicleEntity).\n"
                  + " Expected:\n" + _infoVehicles + "\n"
                  + " Found:\n" + _existingVehicles);
        }
        final HashMap<String, TableInfo.Column> _columnsTrips = new HashMap<String, TableInfo.Column>(9);
        _columnsTrips.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("clientId", new TableInfo.Column("clientId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("driverId", new TableInfo.Column("driverId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("originAddress", new TableInfo.Column("originAddress", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("destinationAddress", new TableInfo.Column("destinationAddress", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("fare", new TableInfo.Column("fare", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("requestedAt", new TableInfo.Column("requestedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrips.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrips = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysTrips.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("clientId"), Arrays.asList("id")));
        _foreignKeysTrips.add(new TableInfo.ForeignKey("users", "SET NULL", "NO ACTION", Arrays.asList("driverId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTrips = new HashSet<TableInfo.Index>(2);
        _indicesTrips.add(new TableInfo.Index("index_trips_clientId", false, Arrays.asList("clientId"), Arrays.asList("ASC")));
        _indicesTrips.add(new TableInfo.Index("index_trips_driverId", false, Arrays.asList("driverId"), Arrays.asList("ASC")));
        final TableInfo _infoTrips = new TableInfo("trips", _columnsTrips, _foreignKeysTrips, _indicesTrips);
        final TableInfo _existingTrips = TableInfo.read(db, "trips");
        if (!_infoTrips.equals(_existingTrips)) {
          return new RoomOpenHelper.ValidationResult(false, "trips(com.example.taxipilot.core.data.database.entity.TripEntity).\n"
                  + " Expected:\n" + _infoTrips + "\n"
                  + " Found:\n" + _existingTrips);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "29706fa8523804a7a29339f599002fc7", "44665ad8d50cad960ced8449ed2cbc8d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "taxis","chauffeurs","trajets","charges","reservations","users","vehicles","trips");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `taxis`");
      _db.execSQL("DELETE FROM `chauffeurs`");
      _db.execSQL("DELETE FROM `trajets`");
      _db.execSQL("DELETE FROM `charges`");
      _db.execSQL("DELETE FROM `reservations`");
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `vehicles`");
      _db.execSQL("DELETE FROM `trips`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TaxiDao.class, TaxiDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChauffeurDao.class, ChauffeurDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrajetDao.class, TrajetDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChargeDao.class, ChargeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReservationDao.class, ReservationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VehicleDao.class, VehicleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TripDao.class, TripDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TaxiDao taxiDao() {
    if (_taxiDao != null) {
      return _taxiDao;
    } else {
      synchronized(this) {
        if(_taxiDao == null) {
          _taxiDao = new TaxiDao_Impl(this);
        }
        return _taxiDao;
      }
    }
  }

  @Override
  public ChauffeurDao chauffeurDao() {
    if (_chauffeurDao != null) {
      return _chauffeurDao;
    } else {
      synchronized(this) {
        if(_chauffeurDao == null) {
          _chauffeurDao = new ChauffeurDao_Impl(this);
        }
        return _chauffeurDao;
      }
    }
  }

  @Override
  public TrajetDao trajetDao() {
    if (_trajetDao != null) {
      return _trajetDao;
    } else {
      synchronized(this) {
        if(_trajetDao == null) {
          _trajetDao = new TrajetDao_Impl(this);
        }
        return _trajetDao;
      }
    }
  }

  @Override
  public ChargeDao chargeDao() {
    if (_chargeDao != null) {
      return _chargeDao;
    } else {
      synchronized(this) {
        if(_chargeDao == null) {
          _chargeDao = new ChargeDao_Impl(this);
        }
        return _chargeDao;
      }
    }
  }

  @Override
  public ReservationDao reservationDao() {
    if (_reservationDao != null) {
      return _reservationDao;
    } else {
      synchronized(this) {
        if(_reservationDao == null) {
          _reservationDao = new ReservationDao_Impl(this);
        }
        return _reservationDao;
      }
    }
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public VehicleDao vehicleDao() {
    if (_vehicleDao != null) {
      return _vehicleDao;
    } else {
      synchronized(this) {
        if(_vehicleDao == null) {
          _vehicleDao = new VehicleDao_Impl(this);
        }
        return _vehicleDao;
      }
    }
  }

  @Override
  public TripDao tripDao() {
    if (_tripDao != null) {
      return _tripDao;
    } else {
      synchronized(this) {
        if(_tripDao == null) {
          _tripDao = new TripDao_Impl(this);
        }
        return _tripDao;
      }
    }
  }
}
