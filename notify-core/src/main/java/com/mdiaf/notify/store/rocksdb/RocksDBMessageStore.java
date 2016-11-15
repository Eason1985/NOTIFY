package com.mdiaf.notify.store.rocksdb;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.store.IBlockMessageStore;
import com.mdiaf.notify.store.StoreException;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Eason on 2016/11/10.
 */
public class RocksDBMessageStore implements IBlockMessageStore {


    private final RocksDB rocksDB;

    private final ColumnFamilyHandle handle;

    private final LinkedBlockingQueue<IMessage> msgQueue = new LinkedBlockingQueue<>();
    //去重
    private final HashSet<IMessage> messageHashSet = new HashSet<>();

    public RocksDBMessageStore(RocksDBInstance instance, String columnFamilyName) throws RocksDBException {
        rocksDB = instance.getRocksDB();
        handle = instance.getAndCreateColumnFamilyHandle(columnFamilyName);
    }

    @Override
    public void saveOrUpdate(IMessage message) throws StoreException {
        try {
            rocksDB.put(handle, message.getHeader().getUniqueId().getBytes(), message.toBytes());
        } catch (RocksDBException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public void deleteByUniqueId(String uniqueId) throws StoreException {
        try {
            rocksDB.remove(handle, uniqueId.getBytes());
        } catch (RocksDBException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public List<IMessage> findMomentBefore(long seconds) throws StoreException {
        throw new RuntimeException("No support this method.");
    }

    @Override
    public List<IMessage> findMessages(String topic, String msgType, String groupId) throws StoreException {
        throw new RuntimeException("No support this method.");
    }

    @Override
    public IMessage takeBeforeSeconds(long seconds) {
        return null;
    }
}
