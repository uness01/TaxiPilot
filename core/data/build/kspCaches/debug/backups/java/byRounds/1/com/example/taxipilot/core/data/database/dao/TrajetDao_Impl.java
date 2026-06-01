package com.example.taxipilot.core.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.taxipilot.core.data.database.entity.TrajetEntity;
import com.example.taxipilot.core.data.database.entity.TrajetStatut;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TrajetDao_Impl implements TrajetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TrajetEntity> __insertionAdapterOfTrajetEntity;

  private final EntityDeletionOrUpdateAdapter<TrajetEntity> __deletionAdapterOfTrajetEntity;

  private final EntityDeletionOrUpdateAdapter<TrajetEntity> __updateAdapterOfTrajetEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatut;

  public TrajetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTrajetEntity = new EntityInsertionAdapter<TrajetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `trajets` (`id`,`taxiId`,`chauffeurId`,`reservationId`,`adresseDepart`,`adresseArrivee`,`dateDebut`,`dateFin`,`distanceKm`,`montant`,`statut`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrajetEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTaxiId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getTaxiId());
        }
        if (entity.getChauffeurId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getChauffeurId());
        }
        if (entity.getReservationId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getReservationId());
        }
        statement.bindString(5, entity.getAdresseDepart());
        statement.bindString(6, entity.getAdresseArrivee());
        if (entity.getDateDebut() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getDateDebut());
        }
        if (entity.getDateFin() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDateFin());
        }
        if (entity.getDistanceKm() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getDistanceKm());
        }
        if (entity.getMontant() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getMontant());
        }
        statement.bindString(11, __TrajetStatut_enumToString(entity.getStatut()));
      }
    };
    this.__deletionAdapterOfTrajetEntity = new EntityDeletionOrUpdateAdapter<TrajetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `trajets` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrajetEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTrajetEntity = new EntityDeletionOrUpdateAdapter<TrajetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `trajets` SET `id` = ?,`taxiId` = ?,`chauffeurId` = ?,`reservationId` = ?,`adresseDepart` = ?,`adresseArrivee` = ?,`dateDebut` = ?,`dateFin` = ?,`distanceKm` = ?,`montant` = ?,`statut` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrajetEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTaxiId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getTaxiId());
        }
        if (entity.getChauffeurId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getChauffeurId());
        }
        if (entity.getReservationId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getReservationId());
        }
        statement.bindString(5, entity.getAdresseDepart());
        statement.bindString(6, entity.getAdresseArrivee());
        if (entity.getDateDebut() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getDateDebut());
        }
        if (entity.getDateFin() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDateFin());
        }
        if (entity.getDistanceKm() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getDistanceKm());
        }
        if (entity.getMontant() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getMontant());
        }
        statement.bindString(11, __TrajetStatut_enumToString(entity.getStatut()));
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatut = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE trajets SET statut = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TrajetEntity trajet, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTrajetEntity.insertAndReturnId(trajet);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final TrajetEntity trajet, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTrajetEntity.handle(trajet);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final TrajetEntity trajet, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTrajetEntity.handle(trajet);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatut(final long id, final TrajetStatut statut,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatut.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __TrajetStatut_enumToString(statut));
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateStatut.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TrajetEntity>> getAll() {
    final String _sql = "SELECT * FROM trajets ORDER BY dateDebut DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trajets"}, new Callable<List<TrajetEntity>>() {
      @Override
      @NonNull
      public List<TrajetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfReservationId = CursorUtil.getColumnIndexOrThrow(_cursor, "reservationId");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateDebut = CursorUtil.getColumnIndexOrThrow(_cursor, "dateDebut");
          final int _cursorIndexOfDateFin = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFin");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<TrajetEntity> _result = new ArrayList<TrajetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrajetEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpTaxiId;
            if (_cursor.isNull(_cursorIndexOfTaxiId)) {
              _tmpTaxiId = null;
            } else {
              _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            }
            final Long _tmpChauffeurId;
            if (_cursor.isNull(_cursorIndexOfChauffeurId)) {
              _tmpChauffeurId = null;
            } else {
              _tmpChauffeurId = _cursor.getLong(_cursorIndexOfChauffeurId);
            }
            final Long _tmpReservationId;
            if (_cursor.isNull(_cursorIndexOfReservationId)) {
              _tmpReservationId = null;
            } else {
              _tmpReservationId = _cursor.getLong(_cursorIndexOfReservationId);
            }
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final Long _tmpDateDebut;
            if (_cursor.isNull(_cursorIndexOfDateDebut)) {
              _tmpDateDebut = null;
            } else {
              _tmpDateDebut = _cursor.getLong(_cursorIndexOfDateDebut);
            }
            final Long _tmpDateFin;
            if (_cursor.isNull(_cursorIndexOfDateFin)) {
              _tmpDateFin = null;
            } else {
              _tmpDateFin = _cursor.getLong(_cursorIndexOfDateFin);
            }
            final Double _tmpDistanceKm;
            if (_cursor.isNull(_cursorIndexOfDistanceKm)) {
              _tmpDistanceKm = null;
            } else {
              _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            }
            final Double _tmpMontant;
            if (_cursor.isNull(_cursorIndexOfMontant)) {
              _tmpMontant = null;
            } else {
              _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            }
            final TrajetStatut _tmpStatut;
            _tmpStatut = __TrajetStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new TrajetEntity(_tmpId,_tmpTaxiId,_tmpChauffeurId,_tmpReservationId,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateDebut,_tmpDateFin,_tmpDistanceKm,_tmpMontant,_tmpStatut);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TrajetEntity>> getByTaxi(final long taxiId) {
    final String _sql = "SELECT * FROM trajets WHERE taxiId = ? ORDER BY dateDebut DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taxiId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trajets"}, new Callable<List<TrajetEntity>>() {
      @Override
      @NonNull
      public List<TrajetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfReservationId = CursorUtil.getColumnIndexOrThrow(_cursor, "reservationId");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateDebut = CursorUtil.getColumnIndexOrThrow(_cursor, "dateDebut");
          final int _cursorIndexOfDateFin = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFin");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<TrajetEntity> _result = new ArrayList<TrajetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrajetEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpTaxiId;
            if (_cursor.isNull(_cursorIndexOfTaxiId)) {
              _tmpTaxiId = null;
            } else {
              _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            }
            final Long _tmpChauffeurId;
            if (_cursor.isNull(_cursorIndexOfChauffeurId)) {
              _tmpChauffeurId = null;
            } else {
              _tmpChauffeurId = _cursor.getLong(_cursorIndexOfChauffeurId);
            }
            final Long _tmpReservationId;
            if (_cursor.isNull(_cursorIndexOfReservationId)) {
              _tmpReservationId = null;
            } else {
              _tmpReservationId = _cursor.getLong(_cursorIndexOfReservationId);
            }
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final Long _tmpDateDebut;
            if (_cursor.isNull(_cursorIndexOfDateDebut)) {
              _tmpDateDebut = null;
            } else {
              _tmpDateDebut = _cursor.getLong(_cursorIndexOfDateDebut);
            }
            final Long _tmpDateFin;
            if (_cursor.isNull(_cursorIndexOfDateFin)) {
              _tmpDateFin = null;
            } else {
              _tmpDateFin = _cursor.getLong(_cursorIndexOfDateFin);
            }
            final Double _tmpDistanceKm;
            if (_cursor.isNull(_cursorIndexOfDistanceKm)) {
              _tmpDistanceKm = null;
            } else {
              _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            }
            final Double _tmpMontant;
            if (_cursor.isNull(_cursorIndexOfMontant)) {
              _tmpMontant = null;
            } else {
              _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            }
            final TrajetStatut _tmpStatut;
            _tmpStatut = __TrajetStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new TrajetEntity(_tmpId,_tmpTaxiId,_tmpChauffeurId,_tmpReservationId,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateDebut,_tmpDateFin,_tmpDistanceKm,_tmpMontant,_tmpStatut);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TrajetEntity>> getByChauffeur(final long chauffeurId) {
    final String _sql = "SELECT * FROM trajets WHERE chauffeurId = ? ORDER BY dateDebut DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, chauffeurId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trajets"}, new Callable<List<TrajetEntity>>() {
      @Override
      @NonNull
      public List<TrajetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfReservationId = CursorUtil.getColumnIndexOrThrow(_cursor, "reservationId");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateDebut = CursorUtil.getColumnIndexOrThrow(_cursor, "dateDebut");
          final int _cursorIndexOfDateFin = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFin");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<TrajetEntity> _result = new ArrayList<TrajetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrajetEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpTaxiId;
            if (_cursor.isNull(_cursorIndexOfTaxiId)) {
              _tmpTaxiId = null;
            } else {
              _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            }
            final Long _tmpChauffeurId;
            if (_cursor.isNull(_cursorIndexOfChauffeurId)) {
              _tmpChauffeurId = null;
            } else {
              _tmpChauffeurId = _cursor.getLong(_cursorIndexOfChauffeurId);
            }
            final Long _tmpReservationId;
            if (_cursor.isNull(_cursorIndexOfReservationId)) {
              _tmpReservationId = null;
            } else {
              _tmpReservationId = _cursor.getLong(_cursorIndexOfReservationId);
            }
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final Long _tmpDateDebut;
            if (_cursor.isNull(_cursorIndexOfDateDebut)) {
              _tmpDateDebut = null;
            } else {
              _tmpDateDebut = _cursor.getLong(_cursorIndexOfDateDebut);
            }
            final Long _tmpDateFin;
            if (_cursor.isNull(_cursorIndexOfDateFin)) {
              _tmpDateFin = null;
            } else {
              _tmpDateFin = _cursor.getLong(_cursorIndexOfDateFin);
            }
            final Double _tmpDistanceKm;
            if (_cursor.isNull(_cursorIndexOfDistanceKm)) {
              _tmpDistanceKm = null;
            } else {
              _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            }
            final Double _tmpMontant;
            if (_cursor.isNull(_cursorIndexOfMontant)) {
              _tmpMontant = null;
            } else {
              _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            }
            final TrajetStatut _tmpStatut;
            _tmpStatut = __TrajetStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new TrajetEntity(_tmpId,_tmpTaxiId,_tmpChauffeurId,_tmpReservationId,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateDebut,_tmpDateFin,_tmpDistanceKm,_tmpMontant,_tmpStatut);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TrajetEntity>> getByStatut(final TrajetStatut statut) {
    final String _sql = "SELECT * FROM trajets WHERE statut = ? ORDER BY dateDebut DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __TrajetStatut_enumToString(statut));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trajets"}, new Callable<List<TrajetEntity>>() {
      @Override
      @NonNull
      public List<TrajetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfReservationId = CursorUtil.getColumnIndexOrThrow(_cursor, "reservationId");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateDebut = CursorUtil.getColumnIndexOrThrow(_cursor, "dateDebut");
          final int _cursorIndexOfDateFin = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFin");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<TrajetEntity> _result = new ArrayList<TrajetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrajetEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpTaxiId;
            if (_cursor.isNull(_cursorIndexOfTaxiId)) {
              _tmpTaxiId = null;
            } else {
              _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            }
            final Long _tmpChauffeurId;
            if (_cursor.isNull(_cursorIndexOfChauffeurId)) {
              _tmpChauffeurId = null;
            } else {
              _tmpChauffeurId = _cursor.getLong(_cursorIndexOfChauffeurId);
            }
            final Long _tmpReservationId;
            if (_cursor.isNull(_cursorIndexOfReservationId)) {
              _tmpReservationId = null;
            } else {
              _tmpReservationId = _cursor.getLong(_cursorIndexOfReservationId);
            }
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final Long _tmpDateDebut;
            if (_cursor.isNull(_cursorIndexOfDateDebut)) {
              _tmpDateDebut = null;
            } else {
              _tmpDateDebut = _cursor.getLong(_cursorIndexOfDateDebut);
            }
            final Long _tmpDateFin;
            if (_cursor.isNull(_cursorIndexOfDateFin)) {
              _tmpDateFin = null;
            } else {
              _tmpDateFin = _cursor.getLong(_cursorIndexOfDateFin);
            }
            final Double _tmpDistanceKm;
            if (_cursor.isNull(_cursorIndexOfDistanceKm)) {
              _tmpDistanceKm = null;
            } else {
              _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            }
            final Double _tmpMontant;
            if (_cursor.isNull(_cursorIndexOfMontant)) {
              _tmpMontant = null;
            } else {
              _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            }
            final TrajetStatut _tmpStatut;
            _tmpStatut = __TrajetStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new TrajetEntity(_tmpId,_tmpTaxiId,_tmpChauffeurId,_tmpReservationId,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateDebut,_tmpDateFin,_tmpDistanceKm,_tmpMontant,_tmpStatut);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByReservation(final long reservationId,
      final Continuation<? super TrajetEntity> $completion) {
    final String _sql = "SELECT * FROM trajets WHERE reservationId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, reservationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TrajetEntity>() {
      @Override
      @Nullable
      public TrajetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfReservationId = CursorUtil.getColumnIndexOrThrow(_cursor, "reservationId");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateDebut = CursorUtil.getColumnIndexOrThrow(_cursor, "dateDebut");
          final int _cursorIndexOfDateFin = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFin");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final TrajetEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpTaxiId;
            if (_cursor.isNull(_cursorIndexOfTaxiId)) {
              _tmpTaxiId = null;
            } else {
              _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            }
            final Long _tmpChauffeurId;
            if (_cursor.isNull(_cursorIndexOfChauffeurId)) {
              _tmpChauffeurId = null;
            } else {
              _tmpChauffeurId = _cursor.getLong(_cursorIndexOfChauffeurId);
            }
            final Long _tmpReservationId;
            if (_cursor.isNull(_cursorIndexOfReservationId)) {
              _tmpReservationId = null;
            } else {
              _tmpReservationId = _cursor.getLong(_cursorIndexOfReservationId);
            }
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final Long _tmpDateDebut;
            if (_cursor.isNull(_cursorIndexOfDateDebut)) {
              _tmpDateDebut = null;
            } else {
              _tmpDateDebut = _cursor.getLong(_cursorIndexOfDateDebut);
            }
            final Long _tmpDateFin;
            if (_cursor.isNull(_cursorIndexOfDateFin)) {
              _tmpDateFin = null;
            } else {
              _tmpDateFin = _cursor.getLong(_cursorIndexOfDateFin);
            }
            final Double _tmpDistanceKm;
            if (_cursor.isNull(_cursorIndexOfDistanceKm)) {
              _tmpDistanceKm = null;
            } else {
              _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            }
            final Double _tmpMontant;
            if (_cursor.isNull(_cursorIndexOfMontant)) {
              _tmpMontant = null;
            } else {
              _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            }
            final TrajetStatut _tmpStatut;
            _tmpStatut = __TrajetStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _result = new TrajetEntity(_tmpId,_tmpTaxiId,_tmpChauffeurId,_tmpReservationId,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateDebut,_tmpDateFin,_tmpDistanceKm,_tmpMontant,_tmpStatut);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long id, final Continuation<? super TrajetEntity> $completion) {
    final String _sql = "SELECT * FROM trajets WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TrajetEntity>() {
      @Override
      @Nullable
      public TrajetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfReservationId = CursorUtil.getColumnIndexOrThrow(_cursor, "reservationId");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateDebut = CursorUtil.getColumnIndexOrThrow(_cursor, "dateDebut");
          final int _cursorIndexOfDateFin = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFin");
          final int _cursorIndexOfDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "distanceKm");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final TrajetEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpTaxiId;
            if (_cursor.isNull(_cursorIndexOfTaxiId)) {
              _tmpTaxiId = null;
            } else {
              _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            }
            final Long _tmpChauffeurId;
            if (_cursor.isNull(_cursorIndexOfChauffeurId)) {
              _tmpChauffeurId = null;
            } else {
              _tmpChauffeurId = _cursor.getLong(_cursorIndexOfChauffeurId);
            }
            final Long _tmpReservationId;
            if (_cursor.isNull(_cursorIndexOfReservationId)) {
              _tmpReservationId = null;
            } else {
              _tmpReservationId = _cursor.getLong(_cursorIndexOfReservationId);
            }
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final Long _tmpDateDebut;
            if (_cursor.isNull(_cursorIndexOfDateDebut)) {
              _tmpDateDebut = null;
            } else {
              _tmpDateDebut = _cursor.getLong(_cursorIndexOfDateDebut);
            }
            final Long _tmpDateFin;
            if (_cursor.isNull(_cursorIndexOfDateFin)) {
              _tmpDateFin = null;
            } else {
              _tmpDateFin = _cursor.getLong(_cursorIndexOfDateFin);
            }
            final Double _tmpDistanceKm;
            if (_cursor.isNull(_cursorIndexOfDistanceKm)) {
              _tmpDistanceKm = null;
            } else {
              _tmpDistanceKm = _cursor.getDouble(_cursorIndexOfDistanceKm);
            }
            final Double _tmpMontant;
            if (_cursor.isNull(_cursorIndexOfMontant)) {
              _tmpMontant = null;
            } else {
              _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            }
            final TrajetStatut _tmpStatut;
            _tmpStatut = __TrajetStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _result = new TrajetEntity(_tmpId,_tmpTaxiId,_tmpChauffeurId,_tmpReservationId,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateDebut,_tmpDateFin,_tmpDistanceKm,_tmpMontant,_tmpStatut);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalRecettesChauffeur(final long chauffeurId, final long debut, final long fin,
      final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(montant), 0.0) FROM trajets\n"
            + "        WHERE chauffeurId = ?\n"
            + "          AND statut = 'TERMINE'\n"
            + "          AND dateFin BETWEEN ? AND ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, chauffeurId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, debut);
    _argIndex = 3;
    _statement.bindLong(_argIndex, fin);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalRecettesTaxi(final long taxiId,
      final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(montant), 0.0) FROM trajets\n"
            + "        WHERE taxiId = ? AND statut = 'TERMINE'\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taxiId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __TrajetStatut_enumToString(@NonNull final TrajetStatut _value) {
    switch (_value) {
      case EN_ATTENTE: return "EN_ATTENTE";
      case EN_COURS: return "EN_COURS";
      case TERMINE: return "TERMINE";
      case ANNULE: return "ANNULE";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private TrajetStatut __TrajetStatut_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "EN_ATTENTE": return TrajetStatut.EN_ATTENTE;
      case "EN_COURS": return TrajetStatut.EN_COURS;
      case "TERMINE": return TrajetStatut.TERMINE;
      case "ANNULE": return TrajetStatut.ANNULE;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
