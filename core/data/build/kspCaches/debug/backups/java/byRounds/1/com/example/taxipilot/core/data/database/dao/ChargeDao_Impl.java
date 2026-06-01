package com.example.taxipilot.core.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.taxipilot.core.data.database.entity.ChargeEntity;
import com.example.taxipilot.core.data.database.entity.TypeCharge;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
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
public final class ChargeDao_Impl implements ChargeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChargeEntity> __insertionAdapterOfChargeEntity;

  private final EntityDeletionOrUpdateAdapter<ChargeEntity> __deletionAdapterOfChargeEntity;

  private final EntityDeletionOrUpdateAdapter<ChargeEntity> __updateAdapterOfChargeEntity;

  public ChargeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChargeEntity = new EntityInsertionAdapter<ChargeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `charges` (`id`,`taxiId`,`type`,`description`,`montant`,`date`,`kilometrageAuMoment`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChargeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaxiId());
        statement.bindString(3, __TypeCharge_enumToString(entity.getType()));
        statement.bindString(4, entity.getDescription());
        statement.bindDouble(5, entity.getMontant());
        statement.bindLong(6, entity.getDate());
        if (entity.getKilometrageAuMoment() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getKilometrageAuMoment());
        }
      }
    };
    this.__deletionAdapterOfChargeEntity = new EntityDeletionOrUpdateAdapter<ChargeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `charges` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChargeEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfChargeEntity = new EntityDeletionOrUpdateAdapter<ChargeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `charges` SET `id` = ?,`taxiId` = ?,`type` = ?,`description` = ?,`montant` = ?,`date` = ?,`kilometrageAuMoment` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChargeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaxiId());
        statement.bindString(3, __TypeCharge_enumToString(entity.getType()));
        statement.bindString(4, entity.getDescription());
        statement.bindDouble(5, entity.getMontant());
        statement.bindLong(6, entity.getDate());
        if (entity.getKilometrageAuMoment() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getKilometrageAuMoment());
        }
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final ChargeEntity charge, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfChargeEntity.insertAndReturnId(charge);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ChargeEntity charge, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfChargeEntity.handle(charge);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ChargeEntity charge, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfChargeEntity.handle(charge);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ChargeEntity>> getAll() {
    final String _sql = "SELECT * FROM charges ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"charges"}, new Callable<List<ChargeEntity>>() {
      @Override
      @NonNull
      public List<ChargeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfKilometrageAuMoment = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrageAuMoment");
          final List<ChargeEntity> _result = new ArrayList<ChargeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChargeEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaxiId;
            _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            final TypeCharge _tmpType;
            _tmpType = __TypeCharge_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpMontant;
            _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final Integer _tmpKilometrageAuMoment;
            if (_cursor.isNull(_cursorIndexOfKilometrageAuMoment)) {
              _tmpKilometrageAuMoment = null;
            } else {
              _tmpKilometrageAuMoment = _cursor.getInt(_cursorIndexOfKilometrageAuMoment);
            }
            _item = new ChargeEntity(_tmpId,_tmpTaxiId,_tmpType,_tmpDescription,_tmpMontant,_tmpDate,_tmpKilometrageAuMoment);
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
  public Flow<List<ChargeEntity>> getByTaxi(final long taxiId) {
    final String _sql = "SELECT * FROM charges WHERE taxiId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taxiId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"charges"}, new Callable<List<ChargeEntity>>() {
      @Override
      @NonNull
      public List<ChargeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfKilometrageAuMoment = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrageAuMoment");
          final List<ChargeEntity> _result = new ArrayList<ChargeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChargeEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaxiId;
            _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            final TypeCharge _tmpType;
            _tmpType = __TypeCharge_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpMontant;
            _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final Integer _tmpKilometrageAuMoment;
            if (_cursor.isNull(_cursorIndexOfKilometrageAuMoment)) {
              _tmpKilometrageAuMoment = null;
            } else {
              _tmpKilometrageAuMoment = _cursor.getInt(_cursorIndexOfKilometrageAuMoment);
            }
            _item = new ChargeEntity(_tmpId,_tmpTaxiId,_tmpType,_tmpDescription,_tmpMontant,_tmpDate,_tmpKilometrageAuMoment);
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
  public Flow<List<ChargeEntity>> getByType(final TypeCharge type) {
    final String _sql = "SELECT * FROM charges WHERE type = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __TypeCharge_enumToString(type));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"charges"}, new Callable<List<ChargeEntity>>() {
      @Override
      @NonNull
      public List<ChargeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfKilometrageAuMoment = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrageAuMoment");
          final List<ChargeEntity> _result = new ArrayList<ChargeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChargeEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaxiId;
            _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            final TypeCharge _tmpType;
            _tmpType = __TypeCharge_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpMontant;
            _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final Integer _tmpKilometrageAuMoment;
            if (_cursor.isNull(_cursorIndexOfKilometrageAuMoment)) {
              _tmpKilometrageAuMoment = null;
            } else {
              _tmpKilometrageAuMoment = _cursor.getInt(_cursorIndexOfKilometrageAuMoment);
            }
            _item = new ChargeEntity(_tmpId,_tmpTaxiId,_tmpType,_tmpDescription,_tmpMontant,_tmpDate,_tmpKilometrageAuMoment);
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
  public Flow<List<ChargeEntity>> getByTaxiAndType(final long taxiId, final TypeCharge type) {
    final String _sql = "SELECT * FROM charges WHERE taxiId = ? AND type = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taxiId);
    _argIndex = 2;
    _statement.bindString(_argIndex, __TypeCharge_enumToString(type));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"charges"}, new Callable<List<ChargeEntity>>() {
      @Override
      @NonNull
      public List<ChargeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfKilometrageAuMoment = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrageAuMoment");
          final List<ChargeEntity> _result = new ArrayList<ChargeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChargeEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaxiId;
            _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            final TypeCharge _tmpType;
            _tmpType = __TypeCharge_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpMontant;
            _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final Integer _tmpKilometrageAuMoment;
            if (_cursor.isNull(_cursorIndexOfKilometrageAuMoment)) {
              _tmpKilometrageAuMoment = null;
            } else {
              _tmpKilometrageAuMoment = _cursor.getInt(_cursorIndexOfKilometrageAuMoment);
            }
            _item = new ChargeEntity(_tmpId,_tmpTaxiId,_tmpType,_tmpDescription,_tmpMontant,_tmpDate,_tmpKilometrageAuMoment);
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
  public Flow<List<ChargeEntity>> getByPeriode(final long debut, final long fin) {
    final String _sql = "SELECT * FROM charges WHERE date BETWEEN ? AND ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, debut);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fin);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"charges"}, new Callable<List<ChargeEntity>>() {
      @Override
      @NonNull
      public List<ChargeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaxiId = CursorUtil.getColumnIndexOrThrow(_cursor, "taxiId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMontant = CursorUtil.getColumnIndexOrThrow(_cursor, "montant");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfKilometrageAuMoment = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrageAuMoment");
          final List<ChargeEntity> _result = new ArrayList<ChargeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChargeEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaxiId;
            _tmpTaxiId = _cursor.getLong(_cursorIndexOfTaxiId);
            final TypeCharge _tmpType;
            _tmpType = __TypeCharge_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final double _tmpMontant;
            _tmpMontant = _cursor.getDouble(_cursorIndexOfMontant);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final Integer _tmpKilometrageAuMoment;
            if (_cursor.isNull(_cursorIndexOfKilometrageAuMoment)) {
              _tmpKilometrageAuMoment = null;
            } else {
              _tmpKilometrageAuMoment = _cursor.getInt(_cursorIndexOfKilometrageAuMoment);
            }
            _item = new ChargeEntity(_tmpId,_tmpTaxiId,_tmpType,_tmpDescription,_tmpMontant,_tmpDate,_tmpKilometrageAuMoment);
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
  public Object getTotalChargesTaxi(final long taxiId, final long debut, final long fin,
      final Continuation<? super Double> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(montant), 0.0) FROM charges\n"
            + "        WHERE taxiId = ? AND date BETWEEN ? AND ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taxiId);
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
  public Object getTotalChargesPeriode(final long debut, final long fin,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT COALESCE(SUM(montant), 0.0) FROM charges WHERE date BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, debut);
    _argIndex = 2;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __TypeCharge_enumToString(@NonNull final TypeCharge _value) {
    switch (_value) {
      case DIESEL: return "DIESEL";
      case REPARATION: return "REPARATION";
      case AUTRE: return "AUTRE";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private TypeCharge __TypeCharge_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "DIESEL": return TypeCharge.DIESEL;
      case "REPARATION": return TypeCharge.REPARATION;
      case "AUTRE": return TypeCharge.AUTRE;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
