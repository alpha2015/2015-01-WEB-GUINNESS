package org.nhnnext.guinness.service;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nhnnext.guinness.model.Group;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/applicationContext.xml")
public class GroupServiceTest {
	@Resource
	GroupService groupService;

	@Test
	public void update() {
		String sessionUserId = "a@a.a";
		Group group = new Group("cneih", "public", "d@d.d", "T", null);
//		String rootPath = (new HttpSession()).getServletContext().getRealPath("/");
		groupService.update(sessionUserId, group, null, null);
	}
	
	@Test
	public void 그룹장_탈퇴시도() throws Exception {
		groupService.leaveGroup("d@d.d", "cneih");
	}
	
	@Test
	public void 그룹멤버_아닐때_탈퇴시도() throws Exception {
		groupService.leaveGroup("a@a.a", "cneih");
	}

}
