package com.foilen.smalltools.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.ThreadTools;

public class AbstractSingleJsonFileDaoTest {

    public static class TestSingleDao extends AbstractSingleJsonFileDao<TestDbEntity> {

        private File dbFile;
        private File stagingFile;

        public TestSingleDao(File dbFile) {
            this.dbFile = dbFile;
            this.stagingFile = new File(dbFile.getAbsolutePath() + "_tmp");
        }

        @Override
        protected File getFinalFile() {
            return dbFile;
        }

        @Override
        protected File getStagingFile() {
            return stagingFile;
        }

        @Override
        protected Class<TestDbEntity> getType() {
            return TestDbEntity.class;
        }

    }

    @Test(timeout = 30000)
    public void test() throws Exception {
        File dbFile = File.createTempFile("junit", ".json");
        Assert.assertTrue(dbFile.delete());

        TestSingleDao firstDao = new TestSingleDao(dbFile);

        // Created an empty one
        TestDbEntity entity = firstDao.load();
        entity.assertValue(null, 0);

        // Changing the entity does not affect the DB
        entity.setId("anId");
        Assert.assertEquals("anId", entity.getId());
        entity = firstDao.load();
        entity.assertValue(null, 0);

        // Changing and save
        entity.setId("id1");
        entity.setNumber(2);
        firstDao.save(entity);

        // Changing the entity does not affect the DB
        entity.setId("anId");
        Assert.assertEquals("anId", entity.getId());
        entity = firstDao.load();
        entity.assertValue("id1", 2);

        // Change again and flush
        entity.setId("id2");
        entity.setNumber(3);
        firstDao.save(entity);
        firstDao.flush();

        // Get from new dao
        TestSingleDao secondDao = new TestSingleDao(dbFile);
        entity = secondDao.load();
        entity.assertValue("id2", 3);

        // Try saving slowly
        ThreadTools.sleep(2100);
        long modifiedTime = dbFile.lastModified();
        entity.setId("id3");
        entity.setNumber(4);
        secondDao.save(entity);
        while (dbFile.lastModified() == modifiedTime) {
            ThreadTools.sleep(500);
        }

        // Loading
        TestSingleDao thirdDao = new TestSingleDao(dbFile);
        entity = thirdDao.load();
        entity.assertValue("id3", 4);

        // Changing the value and reverting back before saving won't save to the file
        thirdDao.save(new TestDbEntity("id4", 4));
        thirdDao.save(new TestDbEntity("id3", 4));

        modifiedTime = dbFile.lastModified();
        for (int i = 0; i < 8 && dbFile.lastModified() == modifiedTime; ++i) {
            ThreadTools.sleep(500);
        }
        Assert.assertEquals(dbFile.lastModified(), modifiedTime);

    }

    @Test(timeout = 30000)
    public void test_not_transaction() throws Exception {

        int loop = 1000;

        File dbFile = File.createTempFile("junit", ".json");
        Assert.assertTrue(dbFile.delete());

        TestSingleDao dao = new TestSingleDao(dbFile);

        // Created an empty one
        dao.load().assertValue(null, 0);

        // Update in multiple threads
        CountDownLatch countDownLatch = new CountDownLatch(loop);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < loop; ++i) {
            futures.add(ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    countDownLatch.countDown();
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                TestDbEntity entity = dao.load();
                entity.setNumber(entity.getNumber() + 1);
                dao.save(entity);
            }));
        }

        // Wait for all the changes to be completed
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Assert
        Assert.assertTrue(dao.load().getNumber() < (loop * .9));

    }

    @Test()
    public void test_savedNothing_notNull() throws Exception {

        File dbFile = File.createTempFile("junit", ".json");
        FileTools.writeFile("null", dbFile);

        TestSingleDao dao = new TestSingleDao(dbFile);
        TestDbEntity entity = dao.load();
        Assert.assertNotNull(entity);

    }

    @Test(timeout = 30000)
    public void test_transaction() throws Exception {

        int loop = 1000;

        File dbFile = File.createTempFile("junit", ".json");
        Assert.assertTrue(dbFile.delete());

        TestSingleDao dao = new TestSingleDao(dbFile);

        // Created an empty one
        dao.load().assertValue(null, 0);

        // Update in multiple threads
        CountDownLatch countDownLatch = new CountDownLatch(loop);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < loop; ++i) {
            futures.add(ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    countDownLatch.countDown();
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // In transaction
                dao.loadInTransaction(entity -> {
                    entity.setNumber(entity.getNumber() + 1);
                });
            }));
        }

        // Wait for all the changes to be completed
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Assert
        dao.load().assertValue(null, loop);

    }

    @Test(timeout = 30000)
    public void test_transactions_nested_not_allowed() throws Exception {

        File dbFile = File.createTempFile("junit", ".json");
        Assert.assertTrue(dbFile.delete());

        TestSingleDao dao = new TestSingleDao(dbFile);

        // Created an empty one
        dao.load().assertValue(null, 0);

        // In transaction
        try {
            dao.loadInTransaction(entity -> {
                entity.setNumber(entity.getNumber() + 1);
                dao.loadInTransaction(entitye -> {
                    // Expect crash
                });
            });
            Assert.fail("Expecting an exception");
        } catch (Exception e) {
            Assert.assertEquals("Nested transactions are not supported", e.getMessage());
        }

    }

}
