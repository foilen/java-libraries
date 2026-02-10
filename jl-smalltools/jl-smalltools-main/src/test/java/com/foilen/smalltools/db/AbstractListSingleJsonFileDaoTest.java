package com.foilen.smalltools.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tools.ThreadTools;

public class AbstractListSingleJsonFileDaoTest extends AbstractBasics {

    public static class TestListSingleDao extends AbstractListSingleJsonFileDao<TestDbEntity, String> {

        private File dbFile;
        private File stagingFile;

        public TestListSingleDao(File dbFile) {
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

        @Override
        protected boolean isEntity(String key, TestDbEntity entity) {
            return StringTools.safeEquals(key, entity.getId());
        }

    }

    @Test
    public void test() throws Exception {

        File dbFile = File.createTempFile("junit", ".json");
        Assertions.assertTrue(dbFile.delete());

        TestListSingleDao firstDao = new TestListSingleDao(dbFile);
        firstDao.init();

        // Add some
        for (int i = 1; i <= 5; ++i) {
            firstDao.add(new TestDbEntity("id" + i, i));
        }
        Assertions.assertEquals(5, firstDao.count());

        // Retrieve one
        Optional<TestDbEntity> optional = firstDao.findOne("id2");
        Assertions.assertTrue(optional.isPresent());
        TestDbEntity entity = optional.get();
        entity.assertValue("id2", 2);

        // Retrieve one that does not exist
        optional = firstDao.findOne("id7");
        Assertions.assertFalse(optional.isPresent());

        // Changing the entity does not affect the DB
        entity.setNumber(55);
        entity.assertValue("id2", 55);
        entity = firstDao.findOne("id2").get();
        entity.assertValue("id2", 2);

        // Delete one
        Assertions.assertTrue(firstDao.delete("id1"));
        Assertions.assertEquals(4, firstDao.count());
        Assertions.assertFalse(firstDao.delete("id1"));

        // Update
        entity.setNumber(55);
        entity.assertValue("id2", 55);
        firstDao.update("id2", entity);
        Assertions.assertEquals(4, firstDao.count());
        entity = firstDao.findOne("id2").get();
        entity.assertValue("id2", 55);

        // Add more with list
        List<TestDbEntity> entities = new ArrayList<>();
        for (int i = 6; i <= 10; ++i) {
            entities.add(new TestDbEntity("id" + i, i));
        }
        firstDao.add(entities);
        Assertions.assertEquals(9, firstDao.count());

        // List all
        List<String> expectedIds = Arrays.asList("id3", "id4", "id5", "id2", "id6", "id7", "id8", "id9", "id10");
        List<String> actualIds = firstDao.findAllAsList().stream().map(it -> it.getId()).collect(Collectors.toList());
        Assertions.assertEquals(expectedIds, actualIds);

        // List some
        expectedIds = Arrays.asList("id2", "id7", "id8", "id9", "id10");
        actualIds = firstDao.findAllAsList(it -> it.getNumber() >= 7).stream().map(it -> it.getId()).collect(Collectors.toList());
        Assertions.assertEquals(expectedIds, actualIds);

        // Flush and get from new dao
        firstDao.flush();
        TestListSingleDao secondDao = new TestListSingleDao(dbFile);
        secondDao.init();
        expectedIds = Arrays.asList("id3", "id4", "id5", "id2", "id6", "id7", "id8", "id9", "id10");
        actualIds = secondDao.findAllAsList().stream().map(it -> it.getId()).collect(Collectors.toList());
        Assertions.assertEquals(expectedIds, actualIds);

        // Try saving slowly
        ThreadTools.sleep(2100);
        long modifiedTime = dbFile.lastModified();
        secondDao.add(new TestDbEntity("idSlow", 0));
        while (dbFile.lastModified() == modifiedTime) {
            ThreadTools.sleep(500);
        }

        // Loading
        TestListSingleDao thirdDao = new TestListSingleDao(dbFile);
        thirdDao.init();
        expectedIds = Arrays.asList("id3", "id4", "id5", "id2", "id6", "id7", "id8", "id9", "id10", "idSlow");
        actualIds = thirdDao.findAllAsList().stream().map(it -> it.getId()).collect(Collectors.toList());
        Assertions.assertEquals(expectedIds, actualIds);

        // Changing the value and reverting back before saving won't save to the file
        thirdDao.add(new TestDbEntity("idExtra", 10));
        Assertions.assertEquals(11, thirdDao.count());
        thirdDao.delete("idExtra");
        Assertions.assertEquals(10, thirdDao.count());

        modifiedTime = dbFile.lastModified();
        for (int i = 0; i < 8 && dbFile.lastModified() == modifiedTime; ++i) {
            ThreadTools.sleep(500);
        }
        Assertions.assertEquals(dbFile.lastModified(), modifiedTime);

    }

}
