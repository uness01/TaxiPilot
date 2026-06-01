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
import com.example.taxipilot.core.data.database.entity.ChauffeurEntity;
import com.example.taxipilot.core.data.database.entity.ChauffeurStatut;
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
public final class ChauffeurDao_Impl implements ChauffeurDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChauffeurEntity> __insertionAdapterOfChauffeurEntity;

  private final EntityDeletionOrUpdateAdapter<ChauffeurEntity> __deletionAdapterOfChauffeurEntity;

  private final EntityDeletionOrUpdateAdapter<ChauffeurEntity> __updateAdapterOfChauffeurEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatut;

  public ChauffeurDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChauffeurEntity = new EntityInsertionAdapter<ChauffeurEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `chauffeurs` (`id`,`nom`,`prenom`,`telephone`,`email`,`numeroPermis`,`dateEmbauche`,`statut`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChauffeurEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNom());
        statement.bindString(3, entity.getPrenom());
        statement.bindString(4, entity.getTelephone());
        statement.bindString(5, entity.getEmail());
        statement.bindString(6, entity.getNumeroPermis());
        statement.bindLong(7, entity.getDateEmbauche());
        statement.bindString(8, __ChauffeurStatut_enumToString(entity.getStatut()));
      }
    };
    this.__deletionAdapterOfChauffeurEntity = new EntityDeletionOrUpdateAdapter<ChauffeurEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `chauffeurs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChauffeurEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfChauffeurEntity = new EntityDeletionOrUpdateAdapter<ChauffeurEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `chauffeurs` SET `id` = ?,`nom` = ?,`prenom` = ?,`telephone` = ?,`email` = ?,`numeroPermis` = ?,`dateEmbauche` = ?,`statut` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChauffeurEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNom());
        statement.bindString(3, entity.getPrenom());
        statement.bindString(4, entity.getTelephone());
        statement.bindString(5, entity.getEmail());
        statement.bindString(6, entity.getNumeroPermis());
        statement.bindLong(7, entity.getDateEmbauche());
        statement.bindString(8, __ChauffeurStatut_enumToString(entity.getStatut()));
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatut = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE chauffeurs SET statut = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ChauffeurEntity chauffeur,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfChauffeurEntity.insertAndReturnId(chauffeur);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ChauffeurEntity chauffeur,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfChauffeurEntity.handle(chauffeur);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ChauffeurEntity chauffeur,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfChauffeurEntity.handle(chauffeur);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatut(final long id, final ChauffeurStatut statut,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatut.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __ChauffeurStatut_enumToString(statut));
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
  public Flow<List<ChauffeurEntity>> getAll() {
    final String _sql = "SELECT * FROM chauffeurs ORDER BY nom, prenom";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chauffeurs"}, new Callable<List<ChauffeurEntity>>() {
      @Override
      @NonNull
      public List<ChauffeurEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNom = CursorUtil.getColumnIndexOrThrow(_cursor, "nom");
          final int _cursorIndexOfPrenom = CursorUtil.getColumnIndexOrThrow(_cursor, "prenom");
          final int _cursorIndexOfTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "telephone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfNumeroPermis = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPermis");
          final int _cursorIndexOfDateEmbauche = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEmbauche");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<ChauffeurEntity> _result = new ArrayList<ChauffeurEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChauffeurEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNom;
            _tmpNom = _cursor.getString(_cursorIndexOfNom);
            final String _tmpPrenom;
            _tmpPrenom = _cursor.getString(_cursorIndexOfPrenom);
            final String _tmpTelephone;
            _tmpTelephone = _cursor.getString(_cursorIndexOfTelephone);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpNumeroPermis;
            _tmpNumeroPermis = _cursor.getString(_cursorIndexOfNumeroPermis);
            final long _tmpDateEmbauche;
            _tmpDateEmbauche = _cursor.getLong(_cursorIndexOfDateEmbauche);
            final ChauffeurStatut _tmpStatut;
            _tmpStatut = __ChauffeurStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new ChauffeurEntity(_tmpId,_tmpNom,_tmpPrenom,_tmpTelephone,_tmpEmail,_tmpNumeroPermis,_tmpDateEmbauche,_tmpStatut);
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
  public Flow<List<ChauffeurEntity>> getByStatut(final ChauffeurStatut statut) {
    final String _sql = "SELECT * FROM chauffeurs WHERE statut = ? ORDER BY nom, prenom";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __ChauffeurStatut_enumToString(statut));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"chauffeurs"}, new Callable<List<ChauffeurEntity>>() {
      @Override
      @NonNull
      public List<ChauffeurEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNom = CursorUtil.getColumnIndexOrThrow(_cursor, "nom");
          final int _cursorIndexOfPrenom = CursorUtil.getColumnIndexOrThrow(_cursor, "prenom");
          final int _cursorIndexOfTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "telephone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfNumeroPermis = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPermis");
          final int _cursorIndexOfDateEmbauche = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEmbauche");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final List<ChauffeurEntity> _result = new ArrayList<ChauffeurEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChauffeurEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNom;
            _tmpNom = _cursor.getString(_cursorIndexOfNom);
            final String _tmpPrenom;
            _tmpPrenom = _cursor.getString(_cursorIndexOfPrenom);
            final String _tmpTelephone;
            _tmpTelephone = _cursor.getString(_cursorIndexOfTelephone);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpNumeroPermis;
            _tmpNumeroPermis = _cursor.getString(_cursorIndexOfNumeroPermis);
            final long _tmpDateEmbauche;
            _tmpDateEmbauche = _cursor.getLong(_cursorIndexOfDateEmbauche);
            final ChauffeurStatut _tmpStatut;
            _tmpStatut = __ChauffeurStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _item = new ChauffeurEntity(_tmpId,_tmpNom,_tmpPrenom,_tmpTelephone,_tmpEmail,_tmpNumeroPermis,_tmpDateEmbauche,_tmpStatut);
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
  public Object getById(final long id, final Continuation<? super ChauffeurEntity> $completion) {
    final String _sql = "SELECT * FROM chauffeurs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChauffeurEntity>() {
      @Override
      @Nullable
      public ChauffeurEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNom = CursorUtil.getColumnIndexOrThrow(_cursor, "nom");
          final int _cursorIndexOfPrenom = CursorUtil.getColumnIndexOrThrow(_cursor, "prenom");
          final int _cursorIndexOfTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "telephone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfNumeroPermis = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPermis");
          final int _cursorIndexOfDateEmbauche = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEmbauche");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final ChauffeurEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNom;
            _tmpNom = _cursor.getString(_cursorIndexOfNom);
            final String _tmpPrenom;
            _tmpPrenom = _cursor.getString(_cursorIndexOfPrenom);
            final String _tmpTelephone;
            _tmpTelephone = _cursor.getString(_cursorIndexOfTelephone);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpNumeroPermis;
            _tmpNumeroPermis = _cursor.getString(_cursorIndexOfNumeroPermis);
            final long _tmpDateEmbauche;
            _tmpDateEmbauche = _cursor.getLong(_cursorIndexOfDateEmbauche);
            final ChauffeurStatut _tmpStatut;
            _tmpStatut = __ChauffeurStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _result = new ChauffeurEntity(_tmpId,_tmpNom,_tmpPrenom,_tmpTelephone,_tmpEmail,_tmpNumeroPermis,_tmpDateEmbauche,_tmpStatut);
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
  public Object getByTelephone(final String telephone,
      final Continuation<? super ChauffeurEntity> $completion) {
    final String _sql = "SELECT * FROM chauffeurs WHERE telephone = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, telephone);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChauffeurEntity>() {
      @Override
      @Nullable
      public ChauffeurEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNom = CursorUtil.getColumnIndexOrThrow(_cursor, "nom");
          final int _cursorIndexOfPrenom = CursorUtil.getColumnIndexOrThrow(_cursor, "prenom");
          final int _cursorIndexOfTelephone = CursorUtil.getColumnIndexOrThrow(_cursor, "telephone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfNumeroPermis = CursorUtil.getColumnIndexOrThrow(_cursor, "numeroPermis");
          final int _cursorIndexOfDateEmbauche = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEmbauche");
          final int _cursorIndexOfStatut = CursorUtil.getColumnIndexOrThrow(_cursor, "statut");
          final ChauffeurEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNom;
            _tmpNom = _cursor.getString(_cursorIndexOfNom);
            final String _tmpPrenom;
            _tmpPrenom = _cursor.getString(_cursorIndexOfPrenom);
            final String _tmpTelephone;
            _tmpTelephone = _cursor.getString(_cursorIndexOfTelephone);
            final String _tmpEmail;
            _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            final String _tmpNumeroPermis;
            _tmpNumeroPermis = _cursor.getString(_cursorIndexOfNumeroPermis);
            final long _tmpDateEmbauche;
            _tmpDateEmbauche = _cursor.getLong(_cursorIndexOfDateEmbauche);
            final ChauffeurStatut _tmpStatut;
            _tmpStatut = __ChauffeurStatut_stringToEnum(_cursor.getString(_cursorIndexOfStatut));
            _result = new ChauffeurEntity(_tmpId,_tmpNom,_tmpPrenom,_tmpTelephone,_tmpEmail,_tmpNumeroPermis,_tmpDateEmbauche,_tmpStatut);
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

  private String __ChauffeurStatut_enumToString(@NonNull final ChauffeurStatut _value) {
    switch (_value) {
      case ACTIF: return "ACTIF";
      case INACTIF: return "INACTIF";
      case SUSPENDU: return "SUSPENDU";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private ChauffeurStatut __ChauffeurStatut_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ACTIF": return ChauffeurStatut.ACTIF;
      case "INACTIF": return ChauffeurStatut.INACTIF;
      case "SUSPENDU": return ChauffeurStatut.SUSPENDU;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
