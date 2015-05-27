package org.nhnnext.guinness.dao;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nhnnext.guinness.model.TempNote;
import org.nhnnext.guinness.model.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/applicationContext.xml")
public class TempNoteDaoTest {
	@Resource
	TempNoteDao tempNoteDao;

	@Test
	public void create() {
		String createDate = LocalDate.now().toString();
		long tempNoteId = tempNoteDao.create((new TempNote("test", createDate, new User("y@y.y"))));
		System.out.println(tempNoteId);
		assertNotNull(tempNoteId);
	}
	
	@Test
	public void read() {
		List<TempNote> tempNoteList = tempNoteDao.read("y@y.y");
		for (TempNote note : tempNoteList) {
			System.out.println(note);
		}
		assertNotNull(tempNoteList);
	}
	
	@Test
	public void update() {
		TempNote beforeUpdateTempNote = tempNoteDao.readByNoteId(9);
		System.out.println(beforeUpdateTempNote);
		
		String createDate = LocalDate.now().toString();
		assertNotEquals(0, tempNoteDao.update(new TempNote(9, "수정된 내용", createDate)));
		
		TempNote afterUpdateTempNote = tempNoteDao.readByNoteId(9);
		System.out.println(afterUpdateTempNote);
		
		assertNotEquals(beforeUpdateTempNote, afterUpdateTempNote);
	}
}