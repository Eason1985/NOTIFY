package com.mdiaf.notify.store.rocksdb;

import org.rocksdb.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Eason on 2016/11/14.
 */
public enum  RocksDBInstance {
    INSTANCE;

    private final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();

    private final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

    private RocksDB rocksDB;

    private static final String STORE_LOCAL_PATH = System.getProperty("user.home") + "/.ROCKSDB";

    private final AtomicBoolean isInit = new AtomicBoolean(false);

    RocksDBInstance() throws RocksDBException {
        if (isInit.compareAndSet(false, true)) {
            init();
        }
    }

    private void init() throws RocksDBException {
        checkAndCreateStorePath();
        initColumnFamilyDescriptors();
        initRocksDB();
    }

    private void checkAndCreateStorePath() {
        File file = new File(STORE_LOCAL_PATH);
        if (file.exists()) {
            return;
        }

        if (!file.mkdir()) {
            throw new RuntimeException("Init store path fault. path:" + STORE_LOCAL_PATH);
        }
    }

    private void initColumnFamilyDescriptors() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);

        List<byte[]> cfs = RocksDB.listColumnFamilies(options, STORE_LOCAL_PATH);
        if(cfs.size() > 0) {
            for(byte[] cf : cfs) {
                ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor(cf, new ColumnFamilyOptions());
                columnFamilyDescriptors.add(columnFamilyDescriptor);
            }
        } else {
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
        }
    }

    private void initRocksDB() throws RocksDBException {
        DBOptions dbOptions = new DBOptions();
        dbOptions.setCreateIfMissing(true);
        dbOptions.setCreateMissingColumnFamilies(true);

        rocksDB = RocksDB.open(dbOptions, STORE_LOCAL_PATH, columnFamilyDescriptors, columnFamilyHandles);
    }

    public RocksDB getRocksDB() {
        return rocksDB;
    }

    public synchronized ColumnFamilyHandle getAndCreateColumnFamilyHandle(String columnFamilyName) throws RocksDBException {
        ColumnFamilyHandle columnFamilyHandle;

        for(int i = 0; i < columnFamilyDescriptors.size(); i++) {
            if(new String(columnFamilyDescriptors.get(i).columnFamilyName()).equals(columnFamilyName)) {
                return columnFamilyHandles.get(i);
            }
        }

        ColumnFamilyDescriptor descriptor = new ColumnFamilyDescriptor(columnFamilyName.getBytes(), new ColumnFamilyOptions());
        columnFamilyHandle = rocksDB.createColumnFamily(descriptor);
        columnFamilyHandles.add(columnFamilyHandle);

        return columnFamilyHandle;
    }
}
