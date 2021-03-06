package org.nhnnext.guinness.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.nhnnext.guinness.dao.GroupDao;
import org.nhnnext.guinness.dao.NoteDao;
import org.nhnnext.guinness.util.JsonResult;
import org.nhnnext.guinness.util.ServletRequestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/search")
public class SearchController {
	//TODO 코트리뷰 서비스로 레이어를 분리해야할지?
	@Resource
	private GroupDao groupDao;
	@Resource
	private NoteDao noteDao;
	
	@RequestMapping(value="", method=RequestMethod.GET)
	private @ResponseBody JsonResult getSearchResult(@RequestParam String words, HttpSession session) throws IOException {
		String sessionUserId = ServletRequestUtil.getUserIdFromSession(session);
		String [] splitWords = words.split(" ");
		Map<String, List<Map<String, Object>>> listValues = new HashMap<String, List<Map<String, Object>>>();
		listValues.put("notes", noteDao.searchQueryForMap(sessionUserId, splitWords));
		listValues.put("groups", groupDao.searchQueryForMap(splitWords));
		return new JsonResult().setSuccess(true).setListValues(listValues);
	}
	
	@RequestMapping("/n/{noteId}")
	private String init(@PathVariable String noteId, HttpSession session, Model model) throws IOException {
		String sessionUserId = ServletRequestUtil.getUserIdFromSession(session);
		model.addAttribute("functionSelect", "showNote.js");
		model.addAttribute("sessionUserId", sessionUserId);
		model.addAttribute("noteId", noteId);
		return "search";
	}
}
