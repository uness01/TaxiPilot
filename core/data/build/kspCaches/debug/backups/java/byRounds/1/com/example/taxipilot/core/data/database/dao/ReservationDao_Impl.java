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
import com.example.taxipilot.core.data.database.entity.ReservationEntity;
import com.example.taxipilot.core.data.database.entity.ReservationStatut;
import com.example.taxipilot.core.data.database.entity.ReservationType;
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
public final class ReservationDao_Impl implements ReservationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReservationEntity> __insertionAdapterOfReservationEntity;

  private final EntityDeletionOrUpdateAdapter<ReservationEntity> __deletionAdapterOfReservationEntity;

  private final EntityDeletionOrUpdateAdapter<ReservationEntity> __updateAdapterOfReservationEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatut;

  private final SharedSQLiteStatement __preparedStmtOfConfirmer;

  public ReservationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReservationEntity = new EntityInsertionAdapter<ReservationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reservations` (`id`,`clientId`,`clientNom`,`clientTelephone`,`adresseDepart`,`adresseArrivee`,`dateReservation`,`datePickup`,`taxiId`,`chauffeurId`,`statut`,`type`,`prixEstime`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReservationEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getClientId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getClientId());
        }
        statement.bindString(3, entity.getClientNom());
        statement.bindString(4, entity.getClientTelephone());
        statement.bindString(5, entity.getAdresseDepart());
        statement.bindString(6, entity.getAdresseArrivee());
        statement.bindLong(7, entity.getDateReservation());
        statement.bindLong(8, entity.getDatePickup());
        if (entity.getTaxiId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getTaxiId());
        }
        if (entity.getChauffeurId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getChauffeurId());
        }
        statement.bindString(11, __ReservationStatut_enumToString(entity.getStatut()));
        statement.bindString(12, __ReservationType_enumToString(entity.getType()));
        if (entity.getPrixEstime() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getPrixEstime());
        }
        statement.bindString(14, entity.getNotes());
      }
    };
    this.__deletionAdapterOfReservationEntity = new EntityDeletionOrUpdateAdapter<ReservationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `reservations` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReservationEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfReservationEntity = new EntityDeletionOrUpdateAdapter<ReservationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reservations` SET `id` = ?,`clientId` = ?,`clientNom` = ?,`clientTelephone` = ?,`adresseDepart` = ?,`adresseArrivee` = ?,`dateReservation` = ?,`datePickup` = ?,`taxiId` = ?,`chauffeurId` = ?,`statut` = ?,`type` = ?,`prixEstime` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReservationEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getClientId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getClientId());
        }
        statement.bindString(3, entity.getClientNom());
        statement.bindString(4, entity.getClientTelephone());
        statement.bindString(5, entity.getAdresseDepart());
        statement.bindString(6, entity.getAdresseArrivee());
        statement.bindLong(7, entity.getDateReservation());
        statement.bindLong(8, entity.getDatePickup());
        if (entity.getTaxiId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getTaxiId());
        }
        if (entity.getChauffeurId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getChauffeurId());
        }
        statement.bindString(11, __ReservationStatut_enumToString(entity.getStatut()));
        statement.bindString(12, __ReservationType_enumToString(entity.getType()));
        if (entity.getPrixEstime() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getPrixEstime());
        }
        statement.bindString(14, entity.getNotes());
        statement.bindLong(15, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatut = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE reservations SET statut = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfConfirmer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE reservations SET taxiId = ?, chauffeurId = ?, statut = 'CONFIRMEE' WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ReservationEntity reservation,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReservationEntity.insertAndReturnId(reservation);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ReservationEntity reservation,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfReservationEntity.handle(reservation);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ReservationEntity reservation,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReservationEntity.handle(reservation);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatut(final long id, final ReservationStatut statut,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatut.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __ReservationStatut_enumToString(statut));
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
  public Object confirmer(final long id, final long taxiId, final long chauffeurId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfConfirmer.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, taxiId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, chauffeurId);
        _argIndex = 3;
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
          __preparedStmtOfConfirmer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReservationEntity>> getAll() {
    final String _sql = "SELECT * FROM reservations ORDER BY datePickup ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reservations"}, new Callable<List<ReservationEntity>>() {
      @Override
      @NonNull
      public List<ReservationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfClientNom = CursorUtil.getColumnIndexOrThrow(_cursor, "clientNom");
          final int _cursorIndexOfClientTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientTelephone");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateReservation = CursorUtil.getColumnIndexOrThrow(_cursor, "dateReservation");
          final int _cursorIndexOfDatePickup = CursorUtil.getColumnIndexOrThrow(_cursor, "datePickup");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPrixEstime = CursorUtil.getColumnIndexOrThrow(_cursor, "prixEstime");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<ReservationEntity> _result = new ArrayList<ReservationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReservationEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpClientId;
            if (_cursor.isNull(_cursorIndexOfClientId)) {
              _tmpClientId = null;
            } else {
              _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            }
            final String _tmpClientNom;
            _tmpClientNom = _cursor.getString(_cursorIndexOfClientNom);
            final String _tmpClientTelephone;
            _tmpClientTelephone = _cursor.getString(_cursorIndexOfClientTelephone);
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final long _tmpDateReservation;
            _tmpDateReservation = _cursor.getLong(_cursorIndexOfDateReservation);
            final long _tmpDatePickup;
            _tmpDatePickup = _cursor.getLong(_cursorIndexOfDatePickup);
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
            final ReservationStatut _tmpStatut;
            _tmpStatut = __ReservationStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            final ReservationType _tmpType;
            _tmpType = __ReservationType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final Double _tmpPrixEstime;
            if (_cursor.isNull(_cursorIndexOfPrixEstime)) {
              _tmpPrixEstime = null;
            } else {
              _tmpPrixEstime = _cursor.getDouble(_cursorIndexOfPrixEstime);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new ReservationEntity(_tmpId,_tmpClientId,_tmpClientNom,_tmpClientTelephone,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateReservation,_tmpDatePickup,_tmpTaxiId,_tmpChauffeurId,_tmpStatut,_tmpType,_tmpPrixEstime,_tmpNotes);
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
  public Flow<List<ReservationEntity>> getByStatut(final ReservationStatut statut) {
    final String _sql = "SELECT * FROM reservations WHERE statut = ? ORDER BY datePickup ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __ReservationStatut_enumToString(statut));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reservations"}, new Callable<List<ReservationEntity>>() {
      @Override
      @NonNull
      public List<ReservationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfClientNom = CursorUtil.getColumnIndexOrThrow(_cursor, "clientNom");
          final int _cursorIndexOfClientTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientTelephone");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateReservation = CursorUtil.getColumnIndexOrThrow(_cursor, "dateReservation");
          final int _cursorIndexOfDatePickup = CursorUtil.getColumnIndexOrThrow(_cursor, "datePickup");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPrixEstime = CursorUtil.getColumnIndexOrThrow(_cursor, "prixEstime");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<ReservationEntity> _result = new ArrayList<ReservationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReservationEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpClientId;
            if (_cursor.isNull(_cursorIndexOfClientId)) {
              _tmpClientId = null;
            } else {
              _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            }
            final String _tmpClientNom;
            _tmpClientNom = _cursor.getString(_cursorIndexOfClientNom);
            final String _tmpClientTelephone;
            _tmpClientTelephone = _cursor.getString(_cursorIndexOfClientTelephone);
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final long _tmpDateReservation;
            _tmpDateReservation = _cursor.getLong(_cursorIndexOfDateReservation);
            final long _tmpDatePickup;
            _tmpDatePickup = _cursor.getLong(_cursorIndexOfDatePickup);
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
            final ReservationStatut _tmpStatut;
            _tmpStatut = __ReservationStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            final ReservationType _tmpType;
            _tmpType = __ReservationType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final Double _tmpPrixEstime;
            if (_cursor.isNull(_cursorIndexOfPrixEstime)) {
              _tmpPrixEstime = null;
            } else {
              _tmpPrixEstime = _cursor.getDouble(_cursorIndexOfPrixEstime);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new ReservationEntity(_tmpId,_tmpClientId,_tmpClientNom,_tmpClientTelephone,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateReservation,_tmpDatePickup,_tmpTaxiId,_tmpChauffeurId,_tmpStatut,_tmpType,_tmpPrixEstime,_tmpNotes);
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
  public Flow<List<ReservationEntity>> getByTaxi(final long taxiId) {
    final String _sql = "SELECT * FROM reservations WHERE taxiId = ? ORDER BY datePickup ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taxiId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reservations"}, new Callable<List<ReservationEntity>>() {
      @Override
      @NonNull
      public List<ReservationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfClientNom = CursorUtil.getColumnIndexOrThrow(_cursor, "clientNom");
          final int _cursorIndexOfClientTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientTelephone");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateReservation = CursorUtil.getColumnIndexOrThrow(_cursor, "dateReservation");
          final int _cursorIndexOfDatePickup = CursorUtil.getColumnIndexOrThrow(_cursor, "datePickup");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPrixEstime = CursorUtil.getColumnIndexOrThrow(_cursor, "prixEstime");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<ReservationEntity> _result = new ArrayList<ReservationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReservationEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpClientId;
            if (_cursor.isNull(_cursorIndexOfClientId)) {
              _tmpClientId = null;
            } else {
              _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            }
            final String _tmpClientNom;
            _tmpClientNom = _cursor.getString(_cursorIndexOfClientNom);
            final String _tmpClientTelephone;
            _tmpClientTelephone = _cursor.getString(_cursorIndexOfClientTelephone);
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final long _tmpDateReservation;
            _tmpDateReservation = _cursor.getLong(_cursorIndexOfDateReservation);
            final long _tmpDatePickup;
            _tmpDatePickup = _cursor.getLong(_cursorIndexOfDatePickup);
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
            final ReservationStatut _tmpStatut;
            _tmpStatut = __ReservationStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            final ReservationType _tmpType;
            _tmpType = __ReservationType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final Double _tmpPrixEstime;
            if (_cursor.isNull(_cursorIndexOfPrixEstime)) {
              _tmpPrixEstime = null;
            } else {
              _tmpPrixEstime = _cursor.getDouble(_cursorIndexOfPrixEstime);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new ReservationEntity(_tmpId,_tmpClientId,_tmpClientNom,_tmpClientTelephone,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateReservation,_tmpDatePickup,_tmpTaxiId,_tmpChauffeurId,_tmpStatut,_tmpType,_tmpPrixEstime,_tmpNotes);
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
  public Flow<List<ReservationEntity>> getByChauffeur(final long chauffeurId) {
    final String _sql = "SELECT * FROM reservations WHERE chauffeurId = ? ORDER BY datePickup ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, chauffeurId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reservations"}, new Callable<List<ReservationEntity>>() {
      @Override
      @NonNull
      public List<ReservationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfClientNom = CursorUtil.getColumnIndexOrThrow(_cursor, "clientNom");
          final int _cursorIndexOfClientTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientTelephone");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateReservation = CursorUtil.getColumnIndexOrThrow(_cursor, "dateReservation");
          final int _cursorIndexOfDatePickup = CursorUtil.getColumnIndexOrThrow(_cursor, "datePickup");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPrixEstime = CursorUtil.getColumnIndexOrThrow(_cursor, "prixEstime");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<ReservationEntity> _result = new ArrayList<ReservationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReservationEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpClientId;
            if (_cursor.isNull(_cursorIndexOfClientId)) {
              _tmpClientId = null;
            } else {
              _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            }
            final String _tmpClientNom;
            _tmpClientNom = _cursor.getString(_cursorIndexOfClientNom);
            final String _tmpClientTelephone;
            _tmpClientTelephone = _cursor.getString(_cursorIndexOfClientTelephone);
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final long _tmpDateReservation;
            _tmpDateReservation = _cursor.getLong(_cursorIndexOfDateReservation);
            final long _tmpDatePickup;
            _tmpDatePickup = _cursor.getLong(_cursorIndexOfDatePickup);
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
            final ReservationStatut _tmpStatut;
            _tmpStatut = __ReservationStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            final ReservationType _tmpType;
            _tmpType = __ReservationType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final Double _tmpPrixEstime;
            if (_cursor.isNull(_cursorIndexOfPrixEstime)) {
              _tmpPrixEstime = null;
            } else {
              _tmpPrixEstime = _cursor.getDouble(_cursorIndexOfPrixEstime);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new ReservationEntity(_tmpId,_tmpClientId,_tmpClientNom,_tmpClientTelephone,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateReservation,_tmpDatePickup,_tmpTaxiId,_tmpChauffeurId,_tmpStatut,_tmpType,_tmpPrixEstime,_tmpNotes);
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
  public Flow<List<ReservationEntity>> getByClientId(final long clientId) {
    final String _sql = "SELECT * FROM reservations WHERE clientId = ? ORDER BY datePickup DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clientId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reservations"}, new Callable<List<ReservationEntity>>() {
      @Override
      @NonNull
      public List<ReservationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfClientNom = CursorUtil.getColumnIndexOrThrow(_cursor, "clientNom");
          final int _cursorIndexOfClientTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientTelephone");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateReservation = CursorUtil.getColumnIndexOrThrow(_cursor, "dateReservation");
          final int _cursorIndexOfDatePickup = CursorUtil.getColumnIndexOrThrow(_cursor, "datePickup");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPrixEstime = CursorUtil.getColumnIndexOrThrow(_cursor, "prixEstime");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<ReservationEntity> _result = new ArrayList<ReservationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReservationEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpClientId;
            if (_cursor.isNull(_cursorIndexOfClientId)) {
              _tmpClientId = null;
            } else {
              _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            }
            final String _tmpClientNom;
            _tmpClientNom = _cursor.getString(_cursorIndexOfClientNom);
            final String _tmpClientTelephone;
            _tmpClientTelephone = _cursor.getString(_cursorIndexOfClientTelephone);
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final long _tmpDateReservation;
            _tmpDateReservation = _cursor.getLong(_cursorIndexOfDateReservation);
            final long _tmpDatePickup;
            _tmpDatePickup = _cursor.getLong(_cursorIndexOfDatePickup);
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
            final ReservationStatut _tmpStatut;
            _tmpStatut = __ReservationStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            final ReservationType _tmpType;
            _tmpType = __ReservationType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final Double _tmpPrixEstime;
            if (_cursor.isNull(_cursorIndexOfPrixEstime)) {
              _tmpPrixEstime = null;
            } else {
              _tmpPrixEstime = _cursor.getDouble(_cursorIndexOfPrixEstime);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new ReservationEntity(_tmpId,_tmpClientId,_tmpClientNom,_tmpClientTelephone,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateReservation,_tmpDatePickup,_tmpTaxiId,_tmpChauffeurId,_tmpStatut,_tmpType,_tmpPrixEstime,_tmpNotes);
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
  public Flow<List<ReservationEntity>> getByPeriode(final long debut, final long fin) {
    final String _sql = "SELECT * FROM reservations WHERE datePickup BETWEEN ? AND ? ORDER BY datePickup ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, debut);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fin);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reservations"}, new Callable<List<ReservationEntity>>() {
      @Override
      @NonNull
      public List<ReservationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfClientNom = CursorUtil.getColumnIndexOrThrow(_cursor, "clientNom");
          final int _cursorIndexOfClientTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientTelephone");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateReservation = CursorUtil.getColumnIndexOrThrow(_cursor, "dateReservation");
          final int _cursorIndexOfDatePickup = CursorUtil.getColumnIndexOrThrow(_cursor, "datePickup");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPrixEstime = CursorUtil.getColumnIndexOrThrow(_cursor, "prixEstime");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<ReservationEntity> _result = new ArrayList<ReservationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReservationEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpClientId;
            if (_cursor.isNull(_cursorIndexOfClientId)) {
              _tmpClientId = null;
            } else {
              _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            }
            final String _tmpClientNom;
            _tmpClientNom = _cursor.getString(_cursorIndexOfClientNom);
            final String _tmpClientTelephone;
            _tmpClientTelephone = _cursor.getString(_cursorIndexOfClientTelephone);
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final long _tmpDateReservation;
            _tmpDateReservation = _cursor.getLong(_cursorIndexOfDateReservation);
            final long _tmpDatePickup;
            _tmpDatePickup = _cursor.getLong(_cursorIndexOfDatePickup);
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
            final ReservationStatut _tmpStatut;
            _tmpStatut = __ReservationStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            final ReservationType _tmpType;
            _tmpType = __ReservationType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final Double _tmpPrixEstime;
            if (_cursor.isNull(_cursorIndexOfPrixEstime)) {
              _tmpPrixEstime = null;
            } else {
              _tmpPrixEstime = _cursor.getDouble(_cursorIndexOfPrixEstime);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new ReservationEntity(_tmpId,_tmpClientId,_tmpClientNom,_tmpClientTelephone,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateReservation,_tmpDatePickup,_tmpTaxiId,_tmpChauffeurId,_tmpStatut,_tmpType,_tmpPrixEstime,_tmpNotes);
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
  public Object getById(final long id, final Continuation<? super ReservationEntity> $completion) {
    final String _sql = "SELECT * FROM reservations WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReservationEntity>() {
      @Override
      @Nullable
      public ReservationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfClientNom = CursorUtil.getColumnIndexOrThrow(_cursor, "clientNom");
          final int _cursorIndexOfClientTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientTelephone");
          final int _cursorIndexOfAdresseDepart = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseDepart");
          final int _cursorIndexOfAdresseArrivee = CursorUtil.getColumnIndexOrThrow(_cursor, "adresseArrivee");
          final int _cursorIndexOfDateReservation = CursorUtil.getColumnIndexOrThrow(_cursor, "dateReservation");
          final int _cursorIndexOfDatePickup = CursorUtil.getColumnIndexOrThrow(_cursor, "datePickup");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfChauffeurId = CursorUtil.getColumnIndexOrThrow(_cursor, "chauffeurId");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPrixEstime = CursorUtil.getColumnIndexOrThrow(_cursor, "prixEstime");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final ReservationEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Long _tmpClientId;
            if (_cursor.isNull(_cursorIndexOfClientId)) {
              _tmpClientId = null;
            } else {
              _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            }
            final String _tmpClientNom;
            _tmpClientNom = _cursor.getString(_cursorIndexOfClientNom);
            final String _tmpClientTelephone;
            _tmpClientTelephone = _cursor.getString(_cursorIndexOfClientTelephone);
            final String _tmpAdresseDepart;
            _tmpAdresseDepart = _cursor.getString(_cursorIndexOfAdresseDepart);
            final String _tmpAdresseArrivee;
            _tmpAdresseArrivee = _cursor.getString(_cursorIndexOfAdresseArrivee);
            final long _tmpDateReservation;
            _tmpDateReservation = _cursor.getLong(_cursorIndexOfDateReservation);
            final long _tmpDatePickup;
            _tmpDatePickup = _cursor.getLong(_cursorIndexOfDatePickup);
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
            final ReservationStatut _tmpStatut;
            _tmpStatut = __ReservationStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            final ReservationType _tmpType;
            _tmpType = __ReservationType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final Double _tmpPrixEstime;
            if (_cursor.isNull(_cursorIndexOfPrixEstime)) {
              _tmpPrixEstime = null;
            } else {
              _tmpPrixEstime = _cursor.getDouble(_cursorIndexOfPrixEstime);
            }
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _result = new ReservationEntity(_tmpId,_tmpClientId,_tmpClientNom,_tmpClientTelephone,_tmpAdresseDepart,_tmpAdresseArrivee,_tmpDateReservation,_tmpDatePickup,_tmpTaxiId,_tmpChauffeurId,_tmpStatut,_tmpType,_tmpPrixEstime,_tmpNotes);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __ReservationStatut_enumToString(@NonNull final ReservationStatut _value) {
    switch (_value) {
      case EN_ATTENTE: return "EN_ATTENTE";
      case CONFIRMEE: return "CONFIRMEE";
      case EN_COURS: return "EN_COURS";
      case ANNULEE: return "ANNULEE";
      case TERMINEE: return "TERMINEE";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __ReservationType_enumToString(@NonNull final ReservationType _value) {
    switch (_value) {
      case NORMALE: return "NORMALE";
      case IMMEDIATE: return "IMMEDIATE";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private ReservationStatut __ReservationStatut_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "EN_ATTENTE": return ReservationStatut.EN_ATTENTE;
      case "CONFIRMEE": return ReservationStatut.CONFIRMEE;
      case "EN_COURS": return ReservationStatut.EN_COURS;
      case "ANNULEE": return ReservationStatut.ANNULEE;
      case "TERMINEE": return ReservationStatut.TERMINEE;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private ReservationType __ReservationType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "NORMALE": return ReservationType.NORMALE;
      case "IMMEDIATE": return ReservationType.IMMEDIATE;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
