package com.mdiaf.notify.store;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Map;

        import org.rocksdb.ColumnFamilyDescriptor;
        import org.rocksdb.ColumnFamilyHandle;
        import org.rocksdb.ColumnFamilyOptions;
        import org.rocksdb.DBOptions;
        import org.rocksdb.Options;
        import org.rocksdb.RocksDB;
        import org.rocksdb.RocksDBException;
        import org.rocksdb.RocksIterator;

/**
 * Created by Eason on 2016/11/10.
 */
public class RocksDBTest {

        private static final String dbPath = "/Users/Eason/workspace/.rocksdb";
        static {
            RocksDB.loadLibrary();
        }

        RocksDB rocksDB;

        //	RocksDB.DEFAULT_COLUMN_FAMILY
        public void testDefaultColumnFamily() throws RocksDBException {
            Options options = new Options();
            options.setCreateIfMissing(true);

            rocksDB = RocksDB.open(options, dbPath);
            byte[] key = "Hello".getBytes();
            byte[] value = "World".getBytes();
            rocksDB.put(key, value);

            List<byte[]> cfs = RocksDB.listColumnFamilies(options, dbPath);
            for(byte[] cf : cfs) {
                System.out.println(new String(cf));
            }

            byte[] getValue = rocksDB.get(key);
            System.out.println(new String(getValue));

            rocksDB.put("SecondKey".getBytes(), "SecondValue".getBytes());

            List<byte[]> keys = new ArrayList<>();
            keys.add(key);
            keys.add("SecondKey".getBytes());

            Map<byte[], byte[]> valueMap = rocksDB.multiGet(keys);
            for(Map.Entry<byte[], byte[]> entry : valueMap.entrySet()) {
                System.out.println(new String(entry.getKey()) + ":" + new String(entry.getValue()));
            }

            RocksIterator iter = rocksDB.newIterator();
            for(iter.seekToFirst(); iter.isValid(); iter.next()) {
                System.out.println("iter key:" + new String(iter.key()) + ", iter value:" + new String(iter.value()));
            }

            rocksDB.remove(key);
            System.out.println("after remove key:" + new String(key));

            iter = rocksDB.newIterator();
            for(iter.seekToFirst(); iter.isValid(); iter.next()) {
                System.out.println("iter key:" + new String(iter.key()) + ", iter value:" + new String(iter.value()));
            }

        }

        public void testCertainColumnFamily() throws RocksDBException {
            String table = "CertainColumnFamilyTest";
            String key = "certainKey1";
            String value = "certainValue1";

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
            Options options = new Options();    //启动的时候加载已经有的family，如果是没有的后面创建。重启之后 又会自动加载进来
            options.setCreateIfMissing(true);

            List<byte[]> cfs = RocksDB.listColumnFamilies(options, dbPath);
            if(cfs.size() > 0) {
                for(byte[] cf : cfs) {
                    ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor(cf, new ColumnFamilyOptions());
                    columnFamilyDescriptors.add(columnFamilyDescriptor);
                }
            } else {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
            }

//            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(table.getBytes()));
//            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));

            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

            DBOptions dbOptions = new DBOptions();
            dbOptions.setCreateIfMissing(true);
            dbOptions.setCreateMissingColumnFamilies(true);

//            Options options = new Options();
//            options.setCreateIfMissing(true);
//            options.setCreateMissingColumnFamilies(true);


            rocksDB = RocksDB.open(dbOptions, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            ColumnFamilyHandle columnFamilyHandle = null;

            for(int i = 0; i < columnFamilyDescriptors.size(); i++) {
                if(new String(columnFamilyDescriptors.get(i).columnFamilyName()).equals(table)) {
//                    rocksDB.dropColumnFamily(columnFamilyHandles.get(i));
                    columnFamilyHandle = columnFamilyHandles.get(i);
                }
            }

            if (columnFamilyHandle == null) {
                columnFamilyHandle = rocksDB.createColumnFamily(new ColumnFamilyDescriptor(table.getBytes(), new ColumnFamilyOptions()));
            }

//            ColumnFamilyHandle columnFamilyHandle;
//
//            if (columnFamilyHandles.size() == 0) {
//                columnFamilyHandle = rocksDB.createColumnFamily(new ColumnFamilyDescriptor(table.getBytes(), new ColumnFamilyOptions()));
//            } else {
//                columnFamilyHandle = columnFamilyHandles.get(0);
//            }

            rocksDB.put(columnFamilyHandle, key.getBytes(), value.getBytes());

            byte[] getValue = rocksDB.get(columnFamilyHandle, key.getBytes());
            System.out.println("get Value : " + new String(getValue));

            rocksDB.put(columnFamilyHandle, "SecondKey1".getBytes(), "SecondValue1".getBytes());

            List<byte[]> keys = new ArrayList<byte[]>();
            keys.add(key.getBytes());
            keys.add("SecondKey".getBytes());

            List<ColumnFamilyHandle> handleList = new ArrayList<>();
            handleList.add(columnFamilyHandle);
            handleList.add(columnFamilyHandle);

            Map<byte[], byte[]> multiGet = rocksDB.multiGet(handleList, keys);
            for(Map.Entry<byte[], byte[]> entry : multiGet.entrySet()) {
                System.out.println(new String(entry.getKey()) + "--" + new String(entry.getValue()));
            }

            rocksDB.remove(columnFamilyHandle, key.getBytes());

            RocksIterator iter = rocksDB.newIterator(columnFamilyHandle);
            for(iter.seekToFirst(); iter.isValid(); iter.next()) {
                System.out.println(new String(iter.key()) + ":" + new String(iter.value()));
            }
        }

        public static void main(String[] args) throws RocksDBException {
            RocksDBTest test = new RocksDBTest();
//		test.testDefaultColumnFamily();
            test.testCertainColumnFamily();
        }

}
