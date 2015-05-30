package org.nhnnext.guinness.controller;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.nhnnext.guinness.exception.GroupUpdateException;
import org.nhnnext.guinness.exception.GroupUpdateExceptionIllegalPage;
import org.nhnnext.guinness.exception.UnpermittedAccessGroupException;
import org.nhnnext.guinness.model.Group;
import org.nhnnext.guinness.model.User;
import org.nhnnext.guinness.service.GroupService;
import org.nhnnext.guinness.util.JsonResult;
import org.nhnnext.guinness.util.ServletRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/groups")
public class GroupController {
	private static final Logger logger = LoggerFactory.getLogger(GroupController.class);
	@Resource
	private GroupService groupService;

	@RequestMapping("/form")
	public String list() throws IOException {
		return "groups";
	}

	@RequestMapping("")
	protected @ResponseBody JsonResult list(HttpSession session) throws IOException {
		String userId = ServletRequestUtil.getUserIdFromSession(session);
		return new JsonResult().setSuccess(true).setMapValues(groupService.readGroups(userId));
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	protected @ResponseBody JsonResult create(@RequestParam String status, @RequestParam String groupName,
			HttpSession session, Model model) throws IOException {
		String groupCaptainUserId = ServletRequestUtil.getUserIdFromSession(session);
		Group group = groupService.create(groupName, groupCaptainUserId, status);
		return new JsonResult().setSuccess(true).setObject(group);
	}

	@RequestMapping(value = "/{groupId}", method = RequestMethod.DELETE)
	protected @ResponseBody JsonResult delete(@PathVariable String groupId, HttpSession session, Model model) throws IOException {
		String sessionUserId = ServletRequestUtil.getUserIdFromSession(session);
		groupService.delete(groupId, sessionUserId);
		return new JsonResult().setSuccess(true);
	}

	@RequestMapping(value = "/members/invite", method = RequestMethod.POST)
	protected @ResponseBody JsonResult inviteGroupMember(@RequestParam String userId, @RequestParam String groupId,
			@RequestParam String sessionUserId) {
		try {
			groupService.inviteGroupMember(sessionUserId, userId, groupId);
		} catch (UnpermittedAccessGroupException e) {
			return new JsonResult().setSuccess(false).setMessage(e.getMessage());
		}
		return new JsonResult().setSuccess(true);
	}
	
	@RequestMapping(value = "/members/join", method = RequestMethod.POST)
	protected @ResponseBody JsonResult joinGroupMember(@RequestParam String groupId,
			@RequestParam String sessionUserId) {
		try {
			groupService.joinGroupMember(sessionUserId, groupId);
		} catch (UnpermittedAccessGroupException e) {
			return new JsonResult().setSuccess(false).setMessage(e.getMessage());
		}
		return new JsonResult().setSuccess(true);
	}
	

	@RequestMapping(value = "/members/accept", method = RequestMethod.POST)
	protected @ResponseBody JsonResult acceptGroupMember(@RequestParam String userId, @RequestParam String groupId) {
		User user = groupService.addGroupMember(userId, groupId);
		return new JsonResult().setSuccess(true).setObject(user);
	}

	@RequestMapping(value = "/members/leave", method = RequestMethod.POST)
	protected @ResponseBody JsonResult leave(@RequestParam String sessionUserId, @RequestParam String groupId) {
		groupService.leaveGroup(sessionUserId, groupId);
		return new JsonResult().setSuccess(true);
	}

	@RequestMapping(value = "/members/delete", method = RequestMethod.POST)
	protected @ResponseBody JsonResult delete(@RequestParam String sessionUserId, @RequestParam String userId,
			@RequestParam String groupId) {
		groupService.deleteGroupMember(sessionUserId, userId, groupId);
		return new JsonResult().setSuccess(true);
	}

	@RequestMapping("/members/{groupId}")
	protected @ResponseBody JsonResult listGroupMember(@PathVariable String groupId) {
		return new JsonResult().setSuccess(true).setMapValues(groupService.groupMembers(groupId));
	}

	@RequestMapping("/update/form/{groupId}")
	protected String updateForm(@PathVariable String groupId, Model model, HttpSession session) throws IOException {
		Group group = groupService.readGroup(groupId);
		String sessionUserId = ServletRequestUtil.getUserIdFromSession(session);
		if (!sessionUserId.equals(group.getGroupCaptainUserId())) {
			throw new GroupUpdateExceptionIllegalPage("그룹장만이 그룹설정이 가능합니다.");
		}
		model.addAttribute("group", group);
		model.addAttribute("members", groupService.groupMembers(groupId));
		return "updateGroup";
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	protected String updateUser(@RequestParam String sessionUserId, @RequestParam("backgroundImage") MultipartFile backgroundImage, HttpSession session,Group group) {
		if (group.getGroupName().equals("")) {
			throw new GroupUpdateException("그룹명이 공백입니다.");
		}
		String rootPath = session.getServletContext().getRealPath("/");
		groupService.update(sessionUserId, group, rootPath, backgroundImage);
		return "redirect:/g/" + group.getGroupId();
	}
}
