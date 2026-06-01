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
import com.example.taxipilot.core.data.database.entity.TaxiEntity;
import com.example.taxipilot.core.data.database.entity.TaxiStatut;
import java.lang.Class;
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
public final class TaxiDao_Impl implements TaxiDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TaxiEntity> __insertionAdapterOfTaxiEntity;

  private final EntityDeletionOrUpdateAdapter<TaxiEntity> __deletionAdapterOfTaxiEntity;

  private final EntityDeletionOrUpdateAdapter<TaxiEntity> __updateAdapterOfTaxiEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatut;

  private final SharedSQLiteStatement __preparedStmtOfUpdateKilometrage;

  public TaxiDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTaxiEntity = new EntityInsertionAdapter<TaxiEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `taxis` (`id`,`marque`,`modele`,`immatriculation`,`annee`,`couleur`,`kilometrage`,`statut`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaxiEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getMarque());
        statement.bindString(3, entity.getModele());
        statement.bindString(4, entity.getImmatriculation());
        statement.bindLong(5, entity.getAnnee());
        statement.bindString(6, entity.getCouleur());
        statement.bindLong(7, entity.getKilometrage());
        statement.bindString(8, __TaxiStatut_enumToString(entity.getStatut()));
      }
    };
    this.__deletionAdapterOfTaxiEntity = new EntityDeletionOrUpdateAdapter<TaxiEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `taxis` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaxiEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTaxiEntity = new EntityDeletionOrUpdateAdapter<TaxiEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `taxis` SET `id` = ?,`marque` = ?,`modele` = ?,`immatriculation` = ?,`annee` = ?,`couleur` = ?,`kilometrage` = ?,`statut` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaxiEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getMarque());
        statement.bindString(3, entity.getModele());
        statement.bindString(4, entity.getImmatriculation());
        statement.bindLong(5, entity.getAnnee());
        statement.bindString(6, entity.getCouleur());
        statement.bindLong(7, entity.getKilometrage());
        statement.bindString(8, __TaxiStatut_enumToString(entity.getStatut()));
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatut = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE taxis SET statut = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateKilometrage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE taxis SET kilometrage = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TaxiEntity taxi, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTaxiEntity.insertAndReturnId(taxi);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final TaxiEntity taxi, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTaxiEntity.handle(taxi);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final TaxiEntity taxi, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTaxiEntity.handle(taxi);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatut(final long id, final TaxiStatut statut,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatut.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __TaxiStatut_enumToString(statut));
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
  public Object updateKilometrage(final long id, final int km,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateKilometrage.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, km);
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
          __preparedStmtOfUpdateKilometrage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TaxiEntity>> getAll() {
    final String _sql = "SELECT * FROM taxis ORDER BY marque, modele";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"taxis"}, new Callable<List<TaxiEntity>>() {
      @Override
      @NonNull
      public List<TaxiEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMarque = CursorUtil.getColumnIndexOrThrow(_cursor, "marque");
          final int _cursorIndexOfModele = CursorUtil.getColumnIndexOrThrow(_cursor, "modele");
          final int _cursorIndexOfImmatriculation = CursorUtil.getColumnIndexOrThrow(_cursor, "immatriculation");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfCouleur = CursorUtil.getColumnIndexOrThrow(_cursor, "couleur");
          final int _cursorIndexOfKilometrage = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrage");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<TaxiEntity> _result = new ArrayList<TaxiEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaxiEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMarque;
            _tmpMarque = _cursor.getString(_cursorIndexOfMarque);
            final String _tmpModele;
            _tmpModele = _cursor.getString(_cursorIndexOfModele);
            final String _tmpImmatriculation;
            _tmpImmatriculation = _cursor.getString(_cursorIndexOfImmatriculation);
            final int _tmpAnnee;
            _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            final String _tmpCouleur;
            _tmpCouleur = _cursor.getString(_cursorIndexOfCouleur);
            final int _tmpKilometrage;
            _tmpKilometrage = _cursor.getInt(_cursorIndexOfKilometrage);
            final TaxiStatut _tmpStatut;
            _tmpStatut = __TaxiStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new TaxiEntity(_tmpId,_tmpMarque,_tmpModele,_tmpImmatriculation,_tmpAnnee,_tmpCouleur,_tmpKilometrage,_tmpStatut);
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
  public Flow<List<TaxiEntity>> getByStatut(final TaxiStatut statut) {
    final String _sql = "SELECT * FROM taxis WHERE statut = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __TaxiStatut_enumToString(statut));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"taxis"}, new Callable<List<TaxiEntity>>() {
      @Override
      @NonNull
      public List<TaxiEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMarque = CursorUtil.getColumnIndexOrThrow(_cursor, "marque");
          final int _cursorIndexOfModele = CursorUtil.getColumnIndexOrThrow(_cursor, "modele");
          final int _cursorIndexOfImmatriculation = CursorUtil.getColumnIndexOrThrow(_cursor, "immatriculation");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfCouleur = CursorUtil.getColumnIndexOrThrow(_cursor, "couleur");
          final int _cursorIndexOfKilometrage = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrage");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<TaxiEntity> _result = new ArrayList<TaxiEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaxiEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMarque;
            _tmpMarque = _cursor.getString(_cursorIndexOfMarque);
            final String _tmpModele;
            _tmpModele = _cursor.getString(_cursorIndexOfModele);
            final String _tmpImmatriculation;
            _tmpImmatriculation = _cursor.getString(_cursorIndexOfImmatriculation);
            final int _tmpAnnee;
            _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            final String _tmpCouleur;
            _tmpCouleur = _cursor.getString(_cursorIndexOfCouleur);
            final int _tmpKilometrage;
            _tmpKilometrage = _cursor.getInt(_cursorIndexOfKilometrage);
            final TaxiStatut _tmpStatut;
            _tmpStatut = __TaxiStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new TaxiEntity(_tmpId,_tmpMarque,_tmpModele,_tmpImmatriculation,_tmpAnnee,_tmpCouleur,_tmpKilometrage,_tmpStatut);
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
  public Object getById(final long id, final Continuation<? super TaxiEntity> $completion) {
    final String _sql = "SELECT * FROM taxis WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TaxiEntity>() {
      @Override
      @Nullable
      public TaxiEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMarque = CursorUtil.getColumnIndexOrThrow(_cursor, "marque");
          final int _cursorIndexOfModele = CursorUtil.getColumnIndexOrThrow(_cursor, "modele");
          final int _cursorIndexOfImmatriculation = CursorUtil.getColumnIndexOrThrow(_cursor, "immatriculation");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfCouleur = CursorUtil.getColumnIndexOrThrow(_cursor, "couleur");
          final int _cursorIndexOfKilometrage = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrage");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final TaxiEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMarque;
            _tmpMarque = _cursor.getString(_cursorIndexOfMarque);
            final String _tmpModele;
            _tmpModele = _cursor.getString(_cursorIndexOfModele);
            final String _tmpImmatriculation;
            _tmpImmatriculation = _cursor.getString(_cursorIndexOfImmatriculation);
            final int _tmpAnnee;
            _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            final String _tmpCouleur;
            _tmpCouleur = _cursor.getString(_cursorIndexOfCouleur);
            final int _tmpKilometrage;
            _tmpKilometrage = _cursor.getInt(_cursorIndexOfKilometrage);
            final TaxiStatut _tmpStatut;
            _tmpStatut = __TaxiStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _result = new TaxiEntity(_tmpId,_tmpMarque,_tmpModele,_tmpImmatriculation,_tmpAnnee,_tmpCouleur,_tmpKilometrage,_tmpStatut);
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
  public Object getByImmatriculation(final String immatriculation,
      final Continuation<? super TaxiEntity> $completion) {
    final String _sql = "SELECT * FROM taxis WHERE immatriculation = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, immatriculation);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TaxiEntity>() {
      @Override
      @Nullable
      public TaxiEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMarque = CursorUtil.getColumnIndexOrThrow(_cursor, "marque");
          final int _cursorIndexOfModele = CursorUtil.getColumnIndexOrThrow(_cursor, "modele");
          final int _cursorIndexOfImmatriculation = CursorUtil.getColumnIndexOrThrow(_cursor, "immatriculation");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfCouleur = CursorUtil.getColumnIndexOrThrow(_cursor, "couleur");
          final int _cursorIndexOfKilometrage = CursorUtil.getColumnIndexOrThrow(_cursor, "kilometrage");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final TaxiEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMarque;
            _tmpMarque = _cursor.getString(_cursorIndexOfMarque);
            final String _tmpModele;
            _tmpModele = _cursor.getString(_cursorIndexOfModele);
            final String _tmpImmatriculation;
            _tmpImmatriculation = _cursor.getString(_cursorIndexOfImmatriculation);
            final int _tmpAnnee;
            _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            final String _tmpCouleur;
            _tmpCouleur = _cursor.getString(_cursorIndexOfCouleur);
            final int _tmpKilometrage;
            _tmpKilometrage = _cursor.getInt(_cursorIndexOfKilometrage);
            final TaxiStatut _tmpStatut;
            _tmpStatut = __TaxiStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _result = new TaxiEntity(_tmpId,_tmpMarque,_tmpModele,_tmpImmatriculation,_tmpAnnee,_tmpCouleur,_tmpKilometrage,_tmpStatut);
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

  private String __TaxiStatut_enumToString(@NonNull final TaxiStatut _value) {
    switch (_value) {
      case DISPONIBLE: return "DISPONIBLE";
      case ASSIGNE: return "ASSIGNE";
      case EN_COURSE: return "EN_COURSE";
      case EN_MAINTENANCE: return "EN_MAINTENANCE";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private TaxiStatut __TaxiStatut_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "DISPONIBLE": return TaxiStatut.DISPONIBLE;
      case "ASSIGNE": return TaxiStatut.ASSIGNE;
      case "EN_COURSE": return TaxiStatut.EN_COURSE;
      case "EN_MAINTENANCE": return TaxiStatut.EN_MAINTENANCE;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
