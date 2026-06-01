package com.example.taxipilot.core.data.database.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.taxipilot.core.data.database.entity.TripEntity;
import com.example.taxipilot.core.data.database.entity.TripStatus;
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
public final class TripDao_Impl implements TripDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TripEntity> __insertionAdapterOfTripEntity;

  private final EntityDeletionOrUpdateAdapter<TripEntity> __deletionAdapterOfTripEntity;

  private final EntityDeletionOrUpdateAdapter<TripEntity> __updateAdapterOfTripEntity;

  public TripDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTripEntity = new EntityInsertionAdapter<TripEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `trips` (`id`,`clientId`,`driverId`,`originAddress`,`destinationAddress`,`status`,`fare`,`requestedAt`,`completedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TripEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClientId());
        if (entity.getDriverId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getDriverId());
        }
        statement.bindString(4, entity.getOriginAddress());
        statement.bindString(5, entity.getDestinationAddress());
        statement.bindString(6, __TripStatus_enumToString(entity.getStatus()));
        if (entity.getFare() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getFare());
        }
        statement.bindLong(8, entity.getRequestedAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getCompletedAt());
        }
      }
    };
    this.__deletionAdapterOfTripEntity = new EntityDeletionOrUpdateAdapter<TripEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `trips` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TripEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTripEntity = new EntityDeletionOrUpdateAdapter<TripEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `trips` SET `id` = ?,`clientId` = ?,`driverId` = ?,`originAddress` = ?,`destinationAddress` = ?,`status` = ?,`fare` = ?,`requestedAt` = ?,`completedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TripEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClientId());
        if (entity.getDriverId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getDriverId());
        }
        statement.bindString(4, entity.getOriginAddress());
        statement.bindString(5, entity.getDestinationAddress());
        statement.bindString(6, __TripStatus_enumToString(entity.getStatus()));
        if (entity.getFare() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getFare());
        }
        statement.bindLong(8, entity.getRequestedAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getCompletedAt());
        }
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insertTrip(final TripEntity trip, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTripEntity.insertAndReturnId(trip);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTrip(final TripEntity trip, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTripEntity.handle(trip);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTrip(final TripEntity trip, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTripEntity.handle(trip);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TripEntity>> getAllTrips() {
    final String _sql = "SELECT * FROM trips ORDER BY requestedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trips"}, new Callable<List<TripEntity>>() {
      @Override
      @NonNull
      public List<TripEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfDriverId = CursorUtil.getColumnIndexOrThrow(_cursor, "driverId");
          final int _cursorIndexOfOriginAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "originAddress");
          final int _cursorIndexOfDestinationAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "destinationAddress");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfFare = CursorUtil.getColumnIndexOrThrow(_cursor, "fare");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<TripEntity> _result = new ArrayList<TripEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TripEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpDriverId;
            if (_cursor.isNull(_cursorIndexOfDriverId)) {
              _tmpDriverId = null;
            } else {
              _tmpDriverId = _cursor.getLong(_cursorIndexOfDriverId);
            }
            final String _tmpOriginAddress;
            _tmpOriginAddress = _cursor.getString(_cursorIndexOfOriginAddress);
            final String _tmpDestinationAddress;
            _tmpDestinationAddress = _cursor.getString(_cursorIndexOfDestinationAddress);
            final TripStatus _tmpStatus;
            _tmpStatus = __TripStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Double _tmpFare;
            if (_cursor.isNull(_cursorIndexOfFare)) {
              _tmpFare = null;
            } else {
              _tmpFare = _cursor.getDouble(_cursorIndexOfFare);
            }
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new TripEntity(_tmpId,_tmpClientId,_tmpDriverId,_tmpOriginAddress,_tmpDestinationAddress,_tmpStatus,_tmpFare,_tmpRequestedAt,_tmpCompletedAt);
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
  public Flow<List<TripEntity>> getTripsByClient(final long clientId) {
    final String _sql = "SELECT * FROM trips WHERE clientId = ? ORDER BY requestedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clientId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trips"}, new Callable<List<TripEntity>>() {
      @Override
      @NonNull
      public List<TripEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfDriverId = CursorUtil.getColumnIndexOrThrow(_cursor, "driverId");
          final int _cursorIndexOfOriginAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "originAddress");
          final int _cursorIndexOfDestinationAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "destinationAddress");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfFare = CursorUtil.getColumnIndexOrThrow(_cursor, "fare");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<TripEntity> _result = new ArrayList<TripEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TripEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpDriverId;
            if (_cursor.isNull(_cursorIndexOfDriverId)) {
              _tmpDriverId = null;
            } else {
              _tmpDriverId = _cursor.getLong(_cursorIndexOfDriverId);
            }
            final String _tmpOriginAddress;
            _tmpOriginAddress = _cursor.getString(_cursorIndexOfOriginAddress);
            final String _tmpDestinationAddress;
            _tmpDestinationAddress = _cursor.getString(_cursorIndexOfDestinationAddress);
            final TripStatus _tmpStatus;
            _tmpStatus = __TripStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Double _tmpFare;
            if (_cursor.isNull(_cursorIndexOfFare)) {
              _tmpFare = null;
            } else {
              _tmpFare = _cursor.getDouble(_cursorIndexOfFare);
            }
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new TripEntity(_tmpId,_tmpClientId,_tmpDriverId,_tmpOriginAddress,_tmpDestinationAddress,_tmpStatus,_tmpFare,_tmpRequestedAt,_tmpCompletedAt);
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
  public Flow<List<TripEntity>> getTripsByDriver(final long driverId) {
    final String _sql = "SELECT * FROM trips WHERE driverId = ? ORDER BY requestedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, driverId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trips"}, new Callable<List<TripEntity>>() {
      @Override
      @NonNull
      public List<TripEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfDriverId = CursorUtil.getColumnIndexOrThrow(_cursor, "driverId");
          final int _cursorIndexOfOriginAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "originAddress");
          final int _cursorIndexOfDestinationAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "destinationAddress");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfFare = CursorUtil.getColumnIndexOrThrow(_cursor, "fare");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<TripEntity> _result = new ArrayList<TripEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TripEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpDriverId;
            if (_cursor.isNull(_cursorIndexOfDriverId)) {
              _tmpDriverId = null;
            } else {
              _tmpDriverId = _cursor.getLong(_cursorIndexOfDriverId);
            }
            final String _tmpOriginAddress;
            _tmpOriginAddress = _cursor.getString(_cursorIndexOfOriginAddress);
            final String _tmpDestinationAddress;
            _tmpDestinationAddress = _cursor.getString(_cursorIndexOfDestinationAddress);
            final TripStatus _tmpStatus;
            _tmpStatus = __TripStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Double _tmpFare;
            if (_cursor.isNull(_cursorIndexOfFare)) {
              _tmpFare = null;
            } else {
              _tmpFare = _cursor.getDouble(_cursorIndexOfFare);
            }
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new TripEntity(_tmpId,_tmpClientId,_tmpDriverId,_tmpOriginAddress,_tmpDestinationAddress,_tmpStatus,_tmpFare,_tmpRequestedAt,_tmpCompletedAt);
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
  public Flow<List<TripEntity>> getTripsByStatus(final TripStatus status) {
    final String _sql = "SELECT * FROM trips WHERE status = ? ORDER BY requestedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __TripStatus_enumToString(status));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trips"}, new Callable<List<TripEntity>>() {
      @Override
      @NonNull
      public List<TripEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientId");
          final int _cursorIndexOfDriverId = CursorUtil.getColumnIndexOrThrow(_cursor, "driverId");
          final int _cursorIndexOfOriginAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "originAddress");
          final int _cursorIndexOfDestinationAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "destinationAddress");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfFare = CursorUtil.getColumnIndexOrThrow(_cursor, "fare");
          final int _cursorIndexOfRequestedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "requestedAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<TripEntity> _result = new ArrayList<TripEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TripEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClientId;
            _tmpClientId = _cursor.getLong(_cursorIndexOfClientId);
            final Long _tmpDriverId;
            if (_cursor.isNull(_cursorIndexOfDriverId)) {
              _tmpDriverId = null;
            } else {
              _tmpDriverId = _cursor.getLong(_cursorIndexOfDriverId);
            }
            final String _tmpOriginAddress;
            _tmpOriginAddress = _cursor.getString(_cursorIndexOfOriginAddress);
            final String _tmpDestinationAddress;
            _tmpDestinationAddress = _cursor.getString(_cursorIndexOfDestinationAddress);
            final TripStatus _tmpStatus;
            _tmpStatus = __TripStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Double _tmpFare;
            if (_cursor.isNull(_cursorIndexOfFare)) {
              _tmpFare = null;
            } else {
              _tmpFare = _cursor.getDouble(_cursorIndexOfFare);
            }
            final long _tmpRequestedAt;
            _tmpRequestedAt = _cursor.getLong(_cursorIndexOfRequestedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            _item = new TripEntity(_tmpId,_tmpClientId,_tmpDriverId,_tmpOriginAddress,_tmpDestinationAddress,_tmpStatus,_tmpFare,_tmpRequestedAt,_tmpCompletedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __TripStatus_enumToString(@NonNull final TripStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case ACCEPTED: return "ACCEPTED";
      case IN_PROGRESS: return "IN_PROGRESS";
      case COMPLETED: return "COMPLETED";
      case CANCELLED: return "CANCELLED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private TripStatus __TripStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return TripStatus.PENDING;
      case "ACCEPTED": return TripStatus.ACCEPTED;
      case "IN_PROGRESS": return TripStatus.IN_PROGRESS;
      case "COMPLETED": return TripStatus.COMPLETED;
      case "CANCELLED": return TripStatus.CANCELLED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
