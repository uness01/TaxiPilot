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
import com.example.taxipilot.core.data.database.entity.VehicleEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class VehicleDao_Impl implements VehicleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VehicleEntity> __insertionAdapterOfVehicleEntity;

  private final EntityDeletionOrUpdateAdapter<VehicleEntity> __deletionAdapterOfVehicleEntity;

  private final EntityDeletionOrUpdateAdapter<VehicleEntity> __updateAdapterOfVehicleEntity;

  public VehicleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVehicleEntity = new EntityInsertionAdapter<VehicleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `vehicles` (`id`,`ownerId`,`brand`,`model`,`licensePlate`,`isAvailable`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VehicleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getOwnerId());
        statement.bindString(3, entity.getBrand());
        statement.bindString(4, entity.getModel());
        statement.bindString(5, entity.getLicensePlate());
        final int _tmp = entity.isAvailable() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfVehicleEntity = new EntityDeletionOrUpdateAdapter<VehicleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `vehicles` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VehicleEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfVehicleEntity = new EntityDeletionOrUpdateAdapter<VehicleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `vehicles` SET `id` = ?,`ownerId` = ?,`brand` = ?,`model` = ?,`licensePlate` = ?,`isAvailable` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VehicleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getOwnerId());
        statement.bindString(3, entity.getBrand());
        statement.bindString(4, entity.getModel());
        statement.bindString(5, entity.getLicensePlate());
        final int _tmp = entity.isAvailable() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public Object insertVehicle(final VehicleEntity vehicle,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfVehicleEntity.insertAndReturnId(vehicle);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteVehicle(final VehicleEntity vehicle,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfVehicleEntity.handle(vehicle);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateVehicle(final VehicleEntity vehicle,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfVehicleEntity.handle(vehicle);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VehicleEntity>> getAllVehicles() {
    final String _sql = "SELECT * FROM vehicles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vehicles"}, new Callable<List<VehicleEntity>>() {
      @Override
      @NonNull
      public List<VehicleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfLicensePlate = CursorUtil.getColumnIndexOrThrow(_cursor, "licensePlate");
          final int _cursorIndexOfIsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "isAvailable");
          final List<VehicleEntity> _result = new ArrayList<VehicleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VehicleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpOwnerId;
            _tmpOwnerId = _cursor.getLong(_cursorIndexOfOwnerId);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpLicensePlate;
            _tmpLicensePlate = _cursor.getString(_cursorIndexOfLicensePlate);
            final boolean _tmpIsAvailable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAvailable);
            _tmpIsAvailable = _tmp != 0;
            _item = new VehicleEntity(_tmpId,_tmpOwnerId,_tmpBrand,_tmpModel,_tmpLicensePlate,_tmpIsAvailable);
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
  public Flow<List<VehicleEntity>> getVehiclesByOwner(final long ownerId) {
    final String _sql = "SELECT * FROM vehicles WHERE ownerId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, ownerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vehicles"}, new Callable<List<VehicleEntity>>() {
      @Override
      @NonNull
      public List<VehicleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfLicensePlate = CursorUtil.getColumnIndexOrThrow(_cursor, "licensePlate");
          final int _cursorIndexOfIsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "isAvailable");
          final List<VehicleEntity> _result = new ArrayList<VehicleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VehicleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpOwnerId;
            _tmpOwnerId = _cursor.getLong(_cursorIndexOfOwnerId);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpLicensePlate;
            _tmpLicensePlate = _cursor.getString(_cursorIndexOfLicensePlate);
            final boolean _tmpIsAvailable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAvailable);
            _tmpIsAvailable = _tmp != 0;
            _item = new VehicleEntity(_tmpId,_tmpOwnerId,_tmpBrand,_tmpModel,_tmpLicensePlate,_tmpIsAvailable);
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
  public Flow<List<VehicleEntity>> getAvailableVehicles() {
    final String _sql = "SELECT * FROM vehicles WHERE isAvailable = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vehicles"}, new Callable<List<VehicleEntity>>() {
      @Override
      @NonNull
      public List<VehicleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOwnerId = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerId");
          final int _cursorIndexOfBrand = CursorUtil.getColumnIndexOrThrow(_cursor, "brand");
          final int _cursorIndexOfModel = CursorUtil.getColumnIndexOrThrow(_cursor, "model");
          final int _cursorIndexOfLicensePlate = CursorUtil.getColumnIndexOrThrow(_cursor, "licensePlate");
          final int _cursorIndexOfIsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "isAvailable");
          final List<VehicleEntity> _result = new ArrayList<VehicleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VehicleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpOwnerId;
            _tmpOwnerId = _cursor.getLong(_cursorIndexOfOwnerId);
            final String _tmpBrand;
            _tmpBrand = _cursor.getString(_cursorIndexOfBrand);
            final String _tmpModel;
            _tmpModel = _cursor.getString(_cursorIndexOfModel);
            final String _tmpLicensePlate;
            _tmpLicensePlate = _cursor.getString(_cursorIndexOfLicensePlate);
            final boolean _tmpIsAvailable;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAvailable);
            _tmpIsAvailable = _tmp != 0;
            _item = new VehicleEntity(_tmpId,_tmpOwnerId,_tmpBrand,_tmpModel,_tmpLicensePlate,_tmpIsAvailable);
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
}
