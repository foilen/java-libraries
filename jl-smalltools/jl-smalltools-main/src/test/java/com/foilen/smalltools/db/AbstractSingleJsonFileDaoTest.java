/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.db;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

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

}
